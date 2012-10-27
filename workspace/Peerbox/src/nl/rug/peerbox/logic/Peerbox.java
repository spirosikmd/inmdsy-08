package nl.rug.peerbox.logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.rug.peerbox.logic.messaging.Message;
import nl.rug.peerbox.logic.messaging.Message.Command;
import nl.rug.peerbox.logic.messaging.Message.Key;
import nl.rug.peerbox.logic.messaging.MessageHandler;
import nl.rug.peerbox.logic.messaging.UnsupportedCommandException;
import nl.rug.peerbox.middleware.MessageListener;
import nl.rug.peerbox.middleware.Multicast;
import nl.rug.peerbox.middleware.ReliableMulticast;

import org.apache.log4j.Logger;

public class Peerbox implements MessageListener, Context {

	private static final String PEERBOX_PROPERTIES_FILE = "peerbox.properties";
	private static final String DEFAULT_PROPERTIES_FILE = "default.properties";
	private final static Logger logger = Logger.getLogger(Peerbox.class);

	private Multicast group;
	private String path;
	private String datafile;
	private ExecutorService pool = Executors.newFixedThreadPool(5);
	private Peer peer;
	private VirtualFileSystem fs;

	private static class Holder {
		private static final Context INSTANCE = Peerbox.createInstance();
	}

	public static synchronized Context getInstance() {
		return Holder.INSTANCE;
	}

	private Peerbox() {
		Properties defaultProperties = new Properties();
		createDefaults(defaultProperties);

		Properties properties = new Properties(defaultProperties);
		if (new File(PEERBOX_PROPERTIES_FILE).exists()) {
			try (FileInputStream in = new FileInputStream(
					PEERBOX_PROPERTIES_FILE)) {
				properties.load(in);
			} catch (IOException e) {
				logger.error(e);
			}
		}		
	
		path = properties.getProperty(Property.PATH);
		datafile = properties.getProperty(Property.DATAFILE_NAME);
		String address = properties.getProperty(Property.MULTICAST_ADDRESS);
		int port = Integer.parseInt(properties
				.getProperty(Property.MULTICAST_PORT));
		group = ReliableMulticast.createPeer(address, port);
		
		byte[] ip = getLocalAddress();
		int serverPort = Integer.parseInt(properties
				.getProperty(Property.SERVER_PORT));
		String name = properties.getProperty(Property.NAME);
		peer = Peer.createPeer(ip, serverPort, name);
	}

	private static Peerbox createInstance() {
		Peerbox peerbox = new Peerbox();
		peerbox.group.addMessageListener(peerbox);
		peerbox.fs = VirtualFileSystem.initVirtualFileSystem(peerbox);
		Thread server = new Thread(new FileServer(peerbox));
		server.setDaemon(true);
		server.start();
		return peerbox;
	}

	private static byte[] getLocalAddress() {
		byte[] ip = new byte[] {};
		try {
			ip = InetAddress.getLocalHost().getAddress();
		} catch (UnknownHostException e) {
			logger.error(e);
		}
		return ip;
	}

	@Override
	public void join() {
		Message message = new Message();
		message.put(Key.Command, "JOIN");
		group.announce(message.serialize());
	}

	@Override
	public void requestFiles(boolean initFilelist) {
		Message message = new Message();
		message.put(Key.Command, Command.Request.List);
		if (initFilelist) {
			message.put(Key.Files, fs.getFileList());
		}
		group.announce(message.serialize());
	}

	@Override
	public void leave() {
		group.announce("I left".getBytes());
		group.shutdown();
		pool.shutdownNow();
	}

	@Override
	public void receivedMessage(byte[] data) {

		Message message = Message.deserialize(data);

		if (message != null) {
			try {
				MessageHandler.process(message, this);
			} catch (UnsupportedCommandException e) {
				logger.error("Unsupported command " + e.getUnsupportedCommand());
				logger.error(e);
			}
		}
	}

	@Override
	public Multicast getMulticastGroup() {
		return group;
	}

	@Override
	public String getPathToPeerbox() {
		return path;
	}

	@Override
	public VirtualFileSystem getVirtualFilesystem() {
		return fs;
	}

	@Override
	public Peer getLocalPeer() {
		return peer;
	}

	@Override
	public String getDatafileName() {
		return datafile;
	}

	private static void createDefaults(Properties properties) {
		String homeDirectory = System.getProperty("user.home");
		String computerName = System.getProperty("user.name");

		properties.setProperty(Property.PATH,
				homeDirectory + System.getProperty("file.separator")
						+ "Peerbox");
		properties.setProperty(Property.MULTICAST_ADDRESS, "239.1.2.4");
		properties.setProperty(Property.MULTICAST_PORT, "1567");
		properties.setProperty(Property.SERVER_PORT, "6666");
		properties.setProperty(Property.NAME, computerName);
		properties.setProperty(Property.DATAFILE_NAME, "data.pbx");
		try (FileOutputStream out = new FileOutputStream(
				DEFAULT_PROPERTIES_FILE)) {
			properties.store(out, "");
		} catch (IOException e) {
			logger.error(e);
		}
	}

}

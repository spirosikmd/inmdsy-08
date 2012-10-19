package nl.rug.peerbox.logic;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.rug.peerbox.logic.Message.Command;
import nl.rug.peerbox.logic.Message.Key;
import nl.rug.peerbox.logic.handler.MessageHandler;
import nl.rug.peerbox.logic.handler.UnsupportedCommandException;
import nl.rug.peerbox.middleware.MessageListener;
import nl.rug.peerbox.middleware.Multicast;
import nl.rug.peerbox.middleware.ReliableMulticast;

import org.apache.log4j.Logger;

public class Peerbox implements MessageListener, Context {

	private final Multicast group;
	private final String path;
	private final static Logger logger = Logger.getLogger(Peerbox.class);

	private final ExecutorService pool;
	private final Peer peer;
	private final VirtualFileSystem fs;

	public Peerbox(Properties properties) {

		String address = properties.getProperty(Property.MULTICAST_ADDRESS);
		int port = Integer.parseInt(properties
				.getProperty(Property.MULTICAST_PORT));
		int serverPort = Integer.parseInt(properties
				.getProperty(Property.SERVER_PORT));
		path = properties.getProperty(Property.PATH);

		String name = properties.getProperty(Property.NAME);

		group = ReliableMulticast.createPeer(address, port);
		group.addMessageListener(this);

		pool = Executors.newFixedThreadPool(5);

		// check if folder exists
		File folder = new File(path);
		if (!folder.exists()) {
			folder.mkdirs();
		}

		byte[] ip = new byte[] {};
		try {
			ip = InetAddress.getLocalHost().getAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		peer = Peer.createPeer(ip, serverPort, name);

		fs = VirtualFileSystem.initVirtualFileSystem(this);

		pool.execute(new FileServer(this));

	}

	public void join() {
		Message message = new Message();
		message.put(Key.Command, "JOIN");
		group.announce(message.serialize());
	}

	public void listFiles() {
		System.out.println("===FILE LIST BEGIN===");
		for (Entry<String, PeerboxFile> entry : fs.getFileList().entrySet()) {
			Peer h = entry.getValue().getOwner();
			String f = entry.getKey();
			System.out.println(f + "  @" + h.getName() + " \t\t(" + h + ")");
		}
		System.out.println("===FILE LIST END===");
	}

	public void requestFiles() {
		Message message = new Message();
		message.put(Key.Command, Command.Request.List);
		group.announce(message.serialize());
	}

	public void getFile(final String filename) {

		final Peer h = findHostThatServesTheFileHelper(filename);
		pool.submit(new FileRequestTask(h, filename, this));
		// Future<File> future =
		// submit future to future observer to create a process list
	}

	private Peer findHostThatServesTheFileHelper(String filename) {
		for (Entry<String, PeerboxFile> entry : fs.getFileList().entrySet()) {
			String f = entry.getKey();
			if (filename.equals(f)) {
				return entry.getValue().getOwner();
			}
		}
		return null;
	}

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

}

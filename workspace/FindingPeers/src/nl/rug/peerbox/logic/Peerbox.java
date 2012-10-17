package nl.rug.peerbox.logic;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.rug.peerbox.logic.handler.MessageHandler;
import nl.rug.peerbox.middleware.Message;
import nl.rug.peerbox.middleware.MessageListener;
import nl.rug.peerbox.middleware.MulticastGroup;
import nl.rug.peerbox.middleware.RMulticastGroup;

import org.apache.log4j.Logger;

public class Peerbox implements MessageListener, Context {

	public static final String KEY_COMMAND = "COMMAND";
	private final MulticastGroup group;
	private final String path;
	private final static Logger logger = Logger.getLogger(Peerbox.class);
	private Map<Host, String[]> filelist;

	private final ExecutorService pool;

	final int serverPort;
	private byte[] ip;

	public Peerbox(Properties properties) {
		
		String address = properties.getProperty(Property.MULTICAST_ADDRESS);
		int port = Integer.parseInt(properties
				.getProperty(Property.MULTICAST_PORT));
		this.serverPort = Integer.parseInt(properties
				.getProperty(Property.MULTICAST_PORT));
		this.path = properties.getProperty(Property.PATH);
		
		group = RMulticastGroup.createPeer(address, port);
		group.addMessageListener(this);

		

		filelist = new HashMap<Host, String[]>();
		pool = Executors.newFixedThreadPool(5);

		// check if folder exists
		File folder = new File(path);
		if (!folder.exists()) {
			folder.mkdirs();
		}

		try {
			this.ip = InetAddress.getLocalHost().getAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		pool.execute(new FileServer(this));

	}

	public void join() {
		PeerboxMessage message = new PeerboxMessage();
		message.put(KEY_COMMAND, "JOIN");
		group.announce(message.serialize());
	}

	public void listFiles() {
		System.out.println("===FILE LIST BEGIN===");
		for (Entry<Host, String[]> entry : filelist.entrySet()) {
			Host h = entry.getKey();
			for (String f : entry.getValue()) {
				System.out.println(f + "  @" + h);
			}
		}
		System.out.println("===FILE LIST END===");
	}

	public void requestFiles() {
		PeerboxMessage message = new PeerboxMessage();
		message.put(KEY_COMMAND, "LIST");
		group.announce(message.serialize());
	}

	public void getFile(final String filename) {

		final Host h = findHostThatServesTheFileHelper(filename);
		pool.submit(new FileRequestTask(h, filename));
		// Future<File> future =
		// submit future to future observer to create a process list
	}

	private Host findHostThatServesTheFileHelper(String filename) {
		for (Entry<Host, String[]> entry : filelist.entrySet()) {
			Host h = entry.getKey();
			for (String f : entry.getValue()) {
				if (filename.equals(f)) {
					return h;
				}
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
	public void receivedMessage(Message m) {

		PeerboxMessage message = PeerboxMessage.deserialize(m.getPayload());

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
	public MulticastGroup getMulticastGroup() {
		return group;
	}

	@Override
	public String getPathToPeerbox() {
		return path;
	}

	@Override
	public byte[] getIP() {
		return ip;
	}

	@Override
	public int getPort() {
		return serverPort;
	}

	@Override
	public Map<Host, String[]> getVirtualFilesystem() {
		return filelist;
	}

}

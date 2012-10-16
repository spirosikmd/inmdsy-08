package nl.rug.peerbox.logic;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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

	final int serverPort = 6666;
	private byte[] ip;

	public Peerbox(InetAddress address, int port, final String path) {
		group = RMulticastGroup.createPeer(address, port);
		group.addMessageListener(this);
		this.path = path;

		filelist = new HashMap<Host, String[]>();
		pool = Executors.newFixedThreadPool(5);

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
		pool.submit(new FileRequest(h, filename));
		//Future<File> future = 
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
			logger.info("Received a message in logic "
					+ message.get(KEY_COMMAND));
			MessageHandler.process(message, this);
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

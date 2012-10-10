package nl.rug.peerbox.middleware;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

public class RMulticastGroup implements MulticastGroup {
	
	static final int MAX_MESSAGE_SIZE = 4069;
	static final int MAX_PAYLOAD_SIZE = MAX_MESSAGE_SIZE - Message.HEADER_SIZE;

	static Logger logger = Logger.getLogger(RMulticastGroup.class);

	private final int id = new Random().nextInt(Integer.MAX_VALUE);
	
	private final int port;
	private final InetAddress address;
	private final MulticastSocket socket;
	private final HashMap<Integer, Peer> peers = new HashMap<Integer, Peer>();
	private Sender sender;
	private Receiver receiver;
	
	private final AtomicInteger messageCounter = new AtomicInteger(0);

	private RMulticastGroup(InetAddress address, int port, MulticastSocket socket) {
		this.port = port;
		this.address = address;
		this.socket = socket;
	}

	public static RMulticastGroup createPeer(InetAddress address, int port) {
		try {
			MulticastSocket socket = new MulticastSocket(port);
			socket.setTimeToLive(5);
			socket.joinGroup(address);

			final RMulticastGroup group = new RMulticastGroup(address, port, socket);
			group.sender = new Sender(group);
			group.receiver = new Receiver(group);
			
						
			Thread senderThread = new Thread(group.sender);
			senderThread.setName("Sender");
			senderThread.setDaemon(true);
			senderThread.start();
			
			Thread receiverThread = new Thread(group.receiver);
			receiverThread.setName("Receiver");
			receiverThread.setDaemon(true);
			receiverThread.start();
			
			Thread listener = new Thread(new Listener(group));
			listener.setName("Listener");
			listener.setDaemon(true);
			listener.start();
			
			return group;

		} catch (IOException e) {
			logger.error(e);
		}
		return null;
	}

	
	public void sendMessage(byte[] payload) {
		
		if (payload.length > MAX_PAYLOAD_SIZE) {
			throw new RuntimeException("Payload too large");
		}
		Message outgoing = Message.send(id, messageCounter.incrementAndGet(),
				payload);
		sendMessage(outgoing);
	}

	void sendMessage(Message outgoing) {
		sender.pushMessage(outgoing);
	}
	
	void sendMiss(int peer, int message_id) {
		Message miss = Message.miss(peer, message_id);
		sendMessage(miss);
	}


	void rdeliver(Message m) {
		logger.debug("Consumed: " + m.toString());
	}

	public MulticastSocket getSocket() {
		return socket;
	}
	
	public InetAddress getAddress() {
		return address;
	}
	
	public int getPort() {
		return port;
	}

	public  int getId() {
		return id;
	}
	
	public Sender getSender() {
		return sender;
	}
	
	public Receiver getReceiver() {
		return receiver;
	}
	
	public HashMap<Integer, Peer> getPeers() {
		return peers;
	}
}

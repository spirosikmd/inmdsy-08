package nl.rug.peerbox.middleware;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

public class RMulticastGroup implements MulticastGroup {
	
	static Logger logger = Logger.getLogger(RMulticastGroup.class);

	private final int id = new Random().nextInt(Integer.MAX_VALUE);
	
	private final int port;
	private final InetAddress address;
	
	private final HashMap<Integer, Peer> peers = new HashMap<Integer, Peer>();
	
	private Sender sender;
	private MulticastSocket socket;
	private Receiver receiver;
	private Listener listener;
	
	private ArrayList<MessageListener> observer = new ArrayList<MessageListener>();
	
	private final AtomicInteger messageCounter = new AtomicInteger(0);

	private RMulticastGroup(InetAddress address, int port, MulticastSocket socket) {
		this.port = port;
		this.address = address;
		this.socket = socket;
	}

	public static MulticastGroup createPeer(InetAddress address, int port) {
		try {
			MulticastSocket socket = new MulticastSocket(port);
			socket.setTimeToLive(5);
			socket.joinGroup(address);

			final RMulticastGroup group = new RMulticastGroup(address, port, socket);
			group.sender = new Sender(group);
			group.receiver = new Receiver(group);
			group.listener = new Listener(group);
			
						
			group.sender.start();
			group.receiver.start();
			group.listener.start();
			
			return group;

		} catch (IOException e) {
			logger.error(e);
		}
		return null;
	}

	
	public void announce(byte[] payload) {
		
		if (payload.length > MulticastMessage.MAX_PAYLOAD_SIZE) {
			throw new RuntimeException("Payload too large");
		}
		MulticastMessage outgoing = MulticastMessage.send(id, messageCounter.incrementAndGet(),
				payload);
		sendMessage(outgoing);
	}

	void sendMessage(MulticastMessage outgoing) {
		sender.pushMessage(outgoing);
	}
	
	void rdeliver(MulticastMessage m) {
		notifyListener(m);
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

	public  int getPeerId() {
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

	@Override
	public void shutdown() {
		logger.debug("Shutdown Multicast group");
		receiver.shutdown();
		sender.shutdown();
		
		try {
			socket.leaveGroup(address);
		} catch (IOException e) {
			logger.error(e);
		} finally {
			socket.close();
		}
		
	}
	
	ExecutorService pool = Executors.newSingleThreadExecutor();
	private void notifyListener(final Message about) {		
		pool.submit(new Runnable() {
			@Override
			public void run() {
				for (MessageListener ml : observer) {
					ml.receivedMessage(about);
				}
			}
		});
	}

	@Override
	public void addMessageListener(MessageListener ml) {
		observer.add(ml);
	}
}

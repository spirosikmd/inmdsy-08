package nl.rug.ds.reliable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.concurrent.TimedSemaphore;
import org.apache.log4j.Logger;

public class RMulticast implements Observer {

	static final int MAX_MESSAGE_SIZE = 4069;
	static final int MAX_PAYLOAD_SIZE = MAX_MESSAGE_SIZE - Message.HEADER_SIZE;

	static Logger logger = Logger.getLogger(RMulticast.class);

	private static final int id = new Random().nextInt(Integer.MAX_VALUE);
	private final MulticastSocket socket;
	private MulticastListener listener;
	private final AtomicInteger messageCounter = new AtomicInteger(0);
	private final int port;
	private final InetAddress group;

	private HashMap<Integer, Peer> peers = new HashMap<Integer, Peer>();
	private List<Message> deliveryQueue = new CopyOnWriteArrayList<Message>();
	private List<Message> holdbackQueue = new CopyOnWriteArrayList<Message>();
	private final BlockingQueue<Message> sendQueue = new ArrayBlockingQueue<Message>(
			1024);

	private RMulticast(InetAddress group, int port, MulticastSocket socket) {
		this.port = port;
		this.group = group;
		this.socket = socket;
	}

	public static RMulticast createPeer(InetAddress group, int port) {
		try {
			MulticastSocket socket = new MulticastSocket(port);
			socket.setTimeToLive(5);
			socket.joinGroup(group);

			final RMulticast peer = new RMulticast(group, port, socket);

			MulticastListener listener = MulticastListener
					.createListener(socket);
			peer.setListener(listener);

			Thread sender = new Thread(new Runnable() {
				
				TimedSemaphore semaphore = new TimedSemaphore(100	, TimeUnit.MILLISECONDS, 1);
				
				@Override
				public void run() {
					while (true) {
						try {
							semaphore.acquire();
							Message message = peer.sendQueue.take();
							byte[] data = message.toByte();
							DatagramPacket outgoingPacket = new DatagramPacket(
									data, data.length, peer.group, peer.port);
							logger.debug("Send: " + message);
							peer.socket.send(outgoingPacket);
							if (message.getCommand() == Message.MESSAGE) {
								if (!peer.deliveryQueue.contains(message)) {
									peer.deliveryQueue.add(message);
								}
							}
						} catch (IOException | InterruptedException e) {
							logger.error(e);
						}
					}

				}
			});
			sender.setName("Sender");
			sender.setDaemon(true);
			sender.start();
			
			return peer;

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

	private void sendMessage(Message outgoing) {
		try {
			sendQueue.put(outgoing);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void receiveMessage(byte[] bytes) {
		Message m = null;
		try {
			m = Message.fromByte(bytes);
			receiveMessage(m);
		} catch (ChecksumFailedException e) {
			logger.warn("Checksum failed");
		}
	}

	private void receiveMessage(Message m) {

		switch (m.getCommand()) {
		case Message.MESSAGE:

			if (m.getSource() == id)
				return;

			logger.debug("Received: " + m.toString());

			Peer p = null;
			if (!peers.containsKey(m.getSource())) {
				logger.debug("Detected peer " + m.getSource()
						+ " with piggyback " + (m.getMessageID() - 1));
				p = new Peer();
				p.setHostID(m.getSource());
				p.setSeenMessageID(m.getMessageID() - 1);
				p.setReceivedMessageID(m.getMessageID() - 1);
				peers.put(m.getSource(), p);
			} else {
				p = peers.get(m.getSource());
			}

			int r = p.getReceivedMessageID();
			int s = m.getMessageID();
			if (s > p.getSeenMessageID()) {
				p.setSeenMessageID(s);
			}

			if (s == r + 1) {
				p.setReceivedMessageID(++r);
				sendMessage(m);
				rdeliver(m);

				Message stored = findMessageInHoldbackQueue(p.getHostID(),
						s + 1);
				if (stored != null) {
					holdbackQueue.remove(stored);
					receiveMessage(stored);
				}

			} else if (s > r + 1) {
				logger.debug("Missed message " + (r + 1)
						+ " detected from peer " + m.getSource());
				holdbackQueue.add(m);
				for (int missedID = r + 1; missedID < p.getSeenMessageID(); missedID++) {
					if (findMessageInHoldbackQueue(p.getHostID(), missedID) == null) {
						sendMiss(m.getSource(), missedID);
					}
				}
			}
			break;

		case Message.NACK:
			if (m.getSource() != id)
				return;
			logger.debug(m.toString());
			for (Message stored : deliveryQueue) {
				if (stored.getMessageID() == m.getMessageID()) {
					sendMessage(stored);
					return;
				}
			}
		}
	}

	private Message findMessageInHoldbackQueue(int host, int messageID) {
		for (Message m : holdbackQueue) {
			if (m.getSource() == host && m.getMessageID() == messageID) {
				return m;
			}
		}
		return null;
	}

	private void rdeliver(Message m) {
		logger.debug("Consumed: " + m.toString());
	}

	private void sendMiss(int peer, int message_id) {
		Message miss = Message.miss(peer, message_id);
		sendMessage(miss);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o == listener) {
			if (arg instanceof byte[]) {
				byte[] bytes = (byte[]) arg;
				receiveMessage(bytes);
			}
		}
	}

	private void setListener(MulticastListener listener) {
		if (this.listener != null) {
			this.listener.deleteObserver(this);
		}
		listener.addObserver(this);
		this.listener = listener;
	}

}

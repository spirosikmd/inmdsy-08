package nl.rug.peerbox.middleware;

import java.net.DatagramPacket;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

final class Receiver {

	private static final int RECEIVE_QUEUE_SIZE = 1024;

	private final static Logger logger = Logger.getLogger(Receiver.class);

	private final Queue<MulticastMessage> holdbackQueue = new ConcurrentLinkedQueue<MulticastMessage>();
	private final BlockingQueue<DatagramPacket> receivedDataQueue = new ArrayBlockingQueue<DatagramPacket>(
			RECEIVE_QUEUE_SIZE);

	private RMulticastGroup group;

	public Receiver(RMulticastGroup group) {
		this.group = group;
	}

	private volatile boolean alive = true;
	private Thread thread;

	void start() {
		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (alive) {
					try {
						DatagramPacket data = receivedDataQueue.take();
						processMessage(data.getData());
					} catch (InterruptedException e) {
						logger.debug("Receiver interrupted");
					}
				}
				logger.debug("Receiver stopped");
			}

		});
		thread.setDaemon(true);
		thread.setName("Receiver");
		thread.start();
	}

	void shutdown() {
		logger.debug("Stop Receiver Thread");
		alive = false;
		thread.interrupt();
	}

	void pushDataPacket(DatagramPacket dp) {
		if (!receivedDataQueue.offer(dp)) {
			logger.equals("Incoming Blockingqueue is full");
		}
	}

	private void processMessage(byte[] bytes) {
		MulticastMessage m = null;
		try {
			m = MulticastMessage.fromByte(bytes);
			receiveMessage(m);
		} catch (ChecksumFailedException e) {
			logger.warn("Checksum failed");
		}
	}

	private void receiveMessage(MulticastMessage m) {

		switch (m.getCommand()) {
		case MulticastMessage.MESSAGE:

			if (m.getPeerID() == group.getPeerId())
				return;

			Peer p = null;
			if (!group.getPeers().containsKey(m.getPeerID())) {
				logger.debug("Detected group " + m.getPeerID()
						+ " with piggyback " + (m.getMessageID() - 1));
				p = new Peer();
				p.setHostID(m.getPeerID());
				p.setSeenMessageID(m.getMessageID() - 1);
				p.setReceivedMessageID(m.getMessageID() - 1);
				group.getPeers().put(m.getPeerID(), p);
			} else {
				p = group.getPeers().get(m.getPeerID());
			}

			int r = p.getReceivedMessageID();
			int s = m.getMessageID();
			if (s > p.getSeenMessageID()) {
				p.setSeenMessageID(s);
			}

			if (s == r + 1) {
				logger.debug("Received: " + m.toString());

				p.setReceivedMessageID(++r);
				//sendAck(m);
				group.rdeliver(m);

				MulticastMessage stored = findMessageInHoldbackQueue(p.getHostID(),
						s + 1);
				if (stored != null) {
					holdbackQueue.remove(stored);
					receiveMessage(stored);
				}

			} else if (s > r + 1) {
				logger.debug("Received: " + m.toString());
				logger.debug("Missed message " + (r + 1)
						+ " detected from group " + m.getPeerID());
				holdbackQueue.add(m);
				for (int missedID = r + 1; missedID < p.getSeenMessageID(); missedID++) {
					if (findMessageInHoldbackQueue(p.getHostID(), missedID) == null) {
						sendMiss(m.getPeerID(), missedID);
					}
				}
			} else if (s <= r) {
				logger.debug("Discarded duplicate: " + m.toString());
			}
			break;

		case MulticastMessage.NACK:
			if (m.getPeerID() != group.getPeerId())
				return;
			logger.debug(m.toString());
			group.getSender().resendMessage(m.getMessageID());

		case MulticastMessage.ACK:
			if (m.getPeerID() != group.getPeerId()) {
				logger.debug("Acked: " + m.toString());
			}
			
		}
	}

	void sendMiss(int peer, int message_id) {
		MulticastMessage miss = MulticastMessage.nack(peer, message_id);
		group.sendMessage(miss);
	}

	private MulticastMessage findMessageInHoldbackQueue(int host, int messageID) {
		for (MulticastMessage m : holdbackQueue) {
			if (m.getPeerID() == host && m.getMessageID() == messageID) {
				return m;
			}
		}
		return null;
	}
}
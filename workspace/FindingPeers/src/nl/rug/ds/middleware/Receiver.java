package nl.rug.ds.middleware;

import java.net.DatagramPacket;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

final class Receiver implements Runnable {
	
	private final BlockingQueue<DatagramPacket> receivedDataQueue = new ArrayBlockingQueue<DatagramPacket>(
			1024);
	private RMulticastGroup group;
	private List<Message> holdbackQueue = new CopyOnWriteArrayList<Message>();

	public Receiver(RMulticastGroup group) {
		this.group = group;
	}

	@Override
	public void run() {
		while (true) {
			try {
				DatagramPacket data = receivedDataQueue.take();
				receiveMessage(data.getData());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	void pushDataPacket(DatagramPacket dp) {
		if (!receivedDataQueue.offer(dp)) {
			RMulticastGroup.logger.equals("Incoming Blockingqueue is full");
		}
	}
	
	private void receiveMessage(byte[] bytes) {
		Message m = null;
		try {
			m = Message.fromByte(bytes);
			receiveMessage(m);
		} catch (ChecksumFailedException e) {
			RMulticastGroup.logger.warn("Checksum failed");
		}
	}
	
	private void receiveMessage(Message m) {

		switch (m.getCommand()) {
		case Message.MESSAGE:

			if (m.getSource() == group.getId())
				return;

			Peer p = null;
			if (!group.getPeers().containsKey(m.getSource())) {
				RMulticastGroup.logger.debug("Detected group " + m.getSource()
						+ " with piggyback " + (m.getMessageID() - 1));
				p = new Peer();
				p.setHostID(m.getSource());
				p.setSeenMessageID(m.getMessageID() - 1);
				p.setReceivedMessageID(m.getMessageID() - 1);
				group.getPeers().put(m.getSource(), p);
			} else {
				p = group.getPeers().get(m.getSource());
			}

			int r = p.getReceivedMessageID();
			int s = m.getMessageID();
			if (s > p.getSeenMessageID()) {
				p.setSeenMessageID(s);
			}

			if (s == r + 1) {
				RMulticastGroup.logger.debug("Received: " + m.toString());
				p.setReceivedMessageID(++r);
				group.sendMessage(m);
				group.rdeliver(m);

				Message stored = findMessageInHoldbackQueue(p.getHostID(),
						s + 1);
				if (stored != null) {
					holdbackQueue.remove(stored);
					receiveMessage(stored);
				}

			} else if (s > r + 1) {
				RMulticastGroup.logger.debug("Received: " + m.toString());
				RMulticastGroup.logger.debug("Missed message " + (r + 1)
						+ " detected from group " + m.getSource());
				holdbackQueue.add(m);
				for (int missedID = r + 1; missedID < p.getSeenMessageID(); missedID++) {
					if (findMessageInHoldbackQueue(p.getHostID(), missedID) == null) {
						group.sendMiss(m.getSource(), missedID);
					}
				}
			} else {
				RMulticastGroup.logger.debug("Discarded duplicate: " + m.toString());
			}
			break;

		case Message.NACK:
			if (m.getSource() != group.getId())
				return;
			RMulticastGroup.logger.debug(m.toString());
			group.getSender().resendMessage(m.getMessageID());
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
}
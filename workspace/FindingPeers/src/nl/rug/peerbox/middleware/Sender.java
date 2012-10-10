package nl.rug.peerbox.middleware;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.TimedSemaphore;

final class Sender implements Runnable {
	private final List<Message> sentMessagesList = new CopyOnWriteArrayList<Message>();
	private final BlockingQueue<Message> waitingForSendQueue = new ArrayBlockingQueue<Message>(
			1024);
	
	private final RMulticastGroup group;
	
	TimedSemaphore semaphore = new TimedSemaphore(100,
			TimeUnit.MILLISECONDS, 1);

	Sender(RMulticastGroup group) {
		this.group = group;
	}

	@Override
	public void run() {
		while (true) {
			try {
				//limit bandwidth
				semaphore.acquire();
				Message message = waitingForSendQueue.take();
				byte[] data = message.toByte();
				DatagramPacket outgoingPacket = new DatagramPacket(
						data, data.length, group.getAddress(), group.getPort());
				RMulticastGroup.logger.debug("Send: " + message);
				group.getSocket().send(outgoingPacket);
				if (message.getCommand() == Message.MESSAGE) {
					if (!sentMessagesList.contains(message)) {
						sentMessagesList.add(message);
					}
				}
			} catch (IOException | InterruptedException e) {
				RMulticastGroup.logger.error(e);
			}
		}
	}
	
	void resendMessage(int messageID) {
		for (Message stored : sentMessagesList) {
			if (stored.getMessageID() == messageID) {
				pushMessage(stored);
				return;
			}
		}
	}
	
	void pushMessage(Message toBeDeliverd) {
		try {
			if (!waitingForSendQueue.contains(toBeDeliverd)) {
				waitingForSendQueue.put(toBeDeliverd);
			} else {
				RMulticastGroup.logger.debug("Discarded duplicate message in send queue");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
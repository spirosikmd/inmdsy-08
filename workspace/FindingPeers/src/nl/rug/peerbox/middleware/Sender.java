package nl.rug.peerbox.middleware;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.TimedSemaphore;
import org.apache.log4j.Logger;

final class Sender {

	private final List<Message> sentMessagesList = new CopyOnWriteArrayList<Message>();
	private final BlockingQueue<Message> waitingForSendQueue = new ArrayBlockingQueue<Message>(
			1024);
	private final TimedSemaphore semaphore = new TimedSemaphore(100,
			TimeUnit.MILLISECONDS, 1);

	private static final Logger logger = Logger.getLogger(Sender.class);

	private final RMulticastGroup group;
	private Thread thread;
	private volatile boolean alive = true;

	Sender(RMulticastGroup group) {
		this.group = group;
	}

	void start() {
		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (alive) {
					try {
						// limit bandwidth
						semaphore.acquire();
						// get message from queue, if no message in queue wait
						Message message = waitingForSendQueue.take();

						RMulticastGroup.logger.debug("Send: " + message);

						byte[] data = message.toByte();
						DatagramPacket outgoingPacket = new DatagramPacket(
								data, data.length, group.getAddress(),
								group.getPort());

						group.getSocket().send(outgoingPacket);
						if (message.getCommand() == Message.MESSAGE) {
							if (!sentMessagesList.contains(message)) {
								sentMessagesList.add(message);
							}
						}
					} catch (IOException e) {
						alive = false;
						logger.error(e);
					} catch (InterruptedException e) {
						logger.debug("Sender interrupted");
					}
				}
				logger.debug("Sender stopped");
			}
		});
		thread.setName("Sender");
		thread.start();
	}

	void shutdown() {
		logger.debug("Stop Sender Thread");
		alive = false;
		thread.interrupt();
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
				RMulticastGroup.logger
						.debug("Discarded duplicate message in send queue");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
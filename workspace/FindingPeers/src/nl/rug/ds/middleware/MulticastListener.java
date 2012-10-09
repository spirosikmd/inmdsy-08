package nl.rug.ds.middleware;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Observable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

public class MulticastListener extends Observable implements Runnable {

	static Logger logger = Logger.getLogger(MulticastListener.class);
	
	private MulticastSocket socket;
	private final BlockingQueue<DatagramPacket> queue = new ArrayBlockingQueue<DatagramPacket>(
			1024);

	private MulticastListener() {
	}

	public MulticastSocket getSocket() {
		return socket;
	}

	public void setSocket(MulticastSocket socket) {
		this.socket = socket;
	}

	public static MulticastListener createListener(MulticastSocket socket) {
		MulticastListener instance = new MulticastListener();

		if (socket == null) {
			logger.error("Socket is null");
			throw new RuntimeException("socket is null");
		}

		instance.setSocket(socket);
		instance.startListener();
		return instance;
	}

	private void startListener() {
		Thread listener = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						DatagramPacket data = queue.take();
						setChanged();
						notifyObservers(data.getData());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		listener.setName("Receiver");
		listener.setDaemon(true);
		listener.start();
		
		Thread t = new Thread(this);
		t.setDaemon(true); // remove later to proper shutdown
		t.start();
	}

	public void run() {
		try {
			logger.info("Start listening");
			while (!Thread.interrupted()) {
				DatagramPacket incoming = new DatagramPacket(
						new byte[RMulticast.MAX_MESSAGE_SIZE], RMulticast.MAX_MESSAGE_SIZE);
				socket.receive(incoming);
				if (!queue.offer(incoming)) {
					logger.equals("Incoming Blockingqueue is full");
				}
			}
		} catch (IOException ex) {
			logger.error(ex);
		}
	}
}
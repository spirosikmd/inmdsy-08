package nl.rug.ds;

import static java.lang.System.out;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Observable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MulticastListener extends Observable implements Runnable {

	private MulticastSocket socket;
	private final BlockingQueue<DatagramPacket> queue = new ArrayBlockingQueue<DatagramPacket>(
			512);

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
		listener.setDaemon(true);
		listener.start();
		
		Thread t = new Thread(this);
		t.setDaemon(true); // remove later to proper shutdown
		t.start();
	}

	public void run() {
		try {
			out.println("Listening");
			while (!Thread.interrupted()) {
				DatagramPacket incoming = new DatagramPacket(
						new byte[Peer.MAX_MESSAGE_SIZE], Peer.MAX_MESSAGE_SIZE);
				socket.receive(incoming);
				queue.offer(incoming);
			}
		} catch (IOException ex) {
			out.println(ex);
		}
	}
}
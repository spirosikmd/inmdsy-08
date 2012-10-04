package nl.rug.ds;

import static java.lang.System.out;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Observable;

public class MulticastListener extends Observable implements Runnable {

	private MulticastSocket socket;

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
		Thread t = new Thread(this);
		t.setDaemon(true); // remove later to proper shutdown
		t.start();
	}

	public void run() {
		try {
			out.println("Listening");
			while (!Thread.interrupted()) {
				DatagramPacket incoming = new DatagramPacket(new byte[Peer.MAX_MESSAGE_SIZE],
						Peer.MAX_MESSAGE_SIZE);
				incoming.setLength(incoming.getData().length);
				socket.receive(incoming);
				setChanged();
				notifyObservers(incoming.getData());

			}
		} catch (IOException ex) {
			out.println(ex);
		}
	}
}
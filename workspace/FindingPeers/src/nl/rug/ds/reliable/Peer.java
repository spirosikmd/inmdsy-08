package nl.rug.ds.reliable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Peer implements Observer {

	 static final int MAX_MESSAGE_SIZE = 256;
	 static final int MAX_PAYLOAD_SIZE = MAX_MESSAGE_SIZE - Message.HEADER_SIZE;

	private static final int id = new Random().nextInt(Integer.MAX_VALUE);
	private MulticastSocket socket;
	private MulticastListener listener;
	private final AtomicInteger messageCounter = new AtomicInteger();
	private final int port;
	private final InetAddress group;

	private Peer(InetAddress group, int port) {
		this.port = port;
		this.group = group;
	}

	public static Peer createPeer(InetAddress group, int port) {
		Peer peer = null;
		try {
			MulticastSocket socket = new MulticastSocket(port);
			socket.setTimeToLive(5);
			socket.joinGroup(group);

			peer = new Peer(group, port);
			peer.setSocket(socket);

			MulticastListener listener = MulticastListener
					.createListener(socket);
			peer.setListener(listener);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return peer;
	}

	 MulticastSocket getSocket() {
		return socket;
	}

	 void setSocket(MulticastSocket socket) {
		this.socket = socket;
	}

	public void sendMessage(byte[] payload) {

		if (payload.length > MAX_PAYLOAD_SIZE) {
			throw new RuntimeException("Payload too large");
		}

		Message outgoing = Message.send(id, messageCounter.incrementAndGet(),
				payload);
		byte[] data = outgoing.toByte();
		DatagramPacket outgoingPacket = new DatagramPacket(data,
				data.length, group, port);

		try {
			socket.send(outgoingPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void receiveMessage(byte[] bytes) {
		Message m = null;
		try {
			m = Message.fromByte(bytes);
		} catch (ChecksumFailedException e) {
			e.printStackTrace();
			//miss
		}
		System.out.println(m);
		//inform listeners
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

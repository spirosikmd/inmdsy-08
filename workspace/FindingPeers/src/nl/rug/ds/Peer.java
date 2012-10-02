package nl.rug.ds;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

public class Peer implements Observer {

	private static final int id = new Random().nextInt();
	private MulticastSocket socket;
	private MulticastListener listener;
	private int messageCounter;

	public static Peer createPeer(InetAddress group, int port) {
		Random random = new Random();
		Peer peer = null;
		try {
			MulticastSocket socket = new MulticastSocket(port);
			socket.setTimeToLive(5);
			socket.joinGroup(group);

			peer = new Peer();
			peer.setSocket(socket);

			MulticastListener listener = MulticastListener
					.createListener(socket);
			peer.setListener(listener);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return peer;
	}

	public MulticastSocket getSocket() {
		return socket;
	}

	public void setSocket(MulticastSocket socket) {
		this.socket = socket;
	}

	public void sendMessage(String message) {
		try {
			Message m = new Message();
			m.setNumber(++messageCounter);
			m.setMessage(message);
			m.setSource(id);
			
			DatagramPacket outgoing = new DatagramPacket(m.toByte(), m.getLength(),
					socket.getInterface(), socket.getLocalPort());
			socket.send(outgoing);
		} catch (IOException ex) {
			System.out.println(ex);
		}
	}
	
	public void sendObject() {
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o == listener) {
			if (arg instanceof Message) {
				Message m = (Message)arg;
				System.out.println(m.getNumber() + "@" + m.getSource() + ":" + m.getMessage() );
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

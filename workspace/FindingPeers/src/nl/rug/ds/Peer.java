package nl.rug.ds;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.zip.CRC32;
import static java.lang.System.out;

public class Peer implements Observer {

	private static final int CHECKSUM_SIZE = Long.SIZE / Byte.SIZE;
	public static final int HEADER_SIZE = CHECKSUM_SIZE + (Short.SIZE / 8);
	public static final int MAX_MESSAGE_SIZE =  256;//(int) (Math.pow(2, Short.SIZE));
	public static final int MAX_PAYLOAD_SIZE = MAX_MESSAGE_SIZE - HEADER_SIZE;

	private static final int id = new Random().nextInt(Integer.MAX_VALUE);
	private MulticastSocket socket;
	private MulticastListener listener;
	private volatile int messageCounter = 0;
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

	public MulticastSocket getSocket() {
		return socket;
	}

	public void setSocket(MulticastSocket socket) {
		this.socket = socket;
	}

	public void sendObject(Serializable object) {
		Message outgoing = new Message();
		synchronized (this) {
			outgoing.setNumber(++messageCounter);
		}
		outgoing.setSource(id);
		outgoing.setPayload(object);

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(baos);
			os.writeObject(outgoing);
			byte[] data = baos.toByteArray();
			long checksum = calculateChecksum(data);

			if (data.length > MAX_PAYLOAD_SIZE) {
				throw new RuntimeException("Payload too large");
			}

			ByteBuffer tmpBuffer = ByteBuffer.allocate(MAX_MESSAGE_SIZE);
			tmpBuffer.putLong(checksum);
			tmpBuffer.putShort((short) data.length);
			tmpBuffer.put(data);

			byte[] completeMessage = tmpBuffer.array();
			DatagramPacket outgoingPacket = new DatagramPacket(completeMessage,
					MAX_MESSAGE_SIZE, group, port);

			socket.send(outgoingPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// send

	}

	private long calculateChecksum(byte[] data, int offset, int length) {
		CRC32 crc32 = new CRC32();
		crc32.update(data, offset, length);
		return crc32.getValue();
	}

	private long calculateChecksum(byte[] data) {
		return calculateChecksum(data, 0, data.length);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o == listener) {
			if (arg instanceof byte[]) {
				byte[] bytes = (byte[]) arg;

				for (int i = 0; i != bytes.length; ++i) {
					out.printf("%02x ", bytes[i]);
				}
				out.println();

				ByteBuffer tmpBuffer = ByteBuffer.wrap(bytes);
				long originalChecksum = tmpBuffer.getLong();
				short length = tmpBuffer.getShort();
				long checksum = calculateChecksum(bytes, HEADER_SIZE, length);

				if (originalChecksum != checksum) {
					System.out.println("Checksum failed" + checksum + "  "
							+ originalChecksum);
					// throw new ChecksumFailedException();
				}

				try {
					ObjectInputStream ois = new ObjectInputStream(
							new ByteArrayInputStream(bytes, HEADER_SIZE, length));
					Object object = ois.readObject();
					if (object instanceof Message) {
						Message incoming = (Message) object;
						System.out.println(incoming.getNumber() + "@"
								+ incoming.getSource() + " : "
								+ (String) incoming.getPayload());
						synchronized (this) {
							messageCounter = incoming.getNumber();
						}
					}
				} catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();
				}

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

package nl.rug.peerbox;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import nl.rug.peerbox.middleware.MulticastGroup;
import nl.rug.peerbox.middleware.RMulticastGroup;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class FindingPeersApp {

	static Logger logger = Logger.getLogger(FindingPeersApp.class);

	/**
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException,
			InterruptedException {
		Thread.currentThread().setName("Main");

		//BasicConfigurator.configure();
		PropertyConfigurator.configure("log4j.properties");

		try {
			InetAddress address = InetAddress.getByName("239.1.2.4");
			int port = 1567;

			logger.info(" (m)Starting app to find peers in group "
					+ address.getHostAddress() + ":" + port);

			MulticastGroup group = RMulticastGroup.createPeer(address, port);

			String message;
			Scanner scanner = new Scanner(System.in);
			boolean alive = true;
			do {
				message = scanner.nextLine();
				if ("shutdown".equals(message)) {
					group.shutdown();
				} else if ("close".equals(message)) {
					alive=false;
					scanner.close();
					System.exit(0);
				} else if ("threads".equals(message)) {
					Thread.currentThread().getThreadGroup().list();
				} else if ("list".equals(message)) {
					System.out.println("List all the files");
				} else if ("test".equals(message)) {
					for (int i = 0; i < 100; i++) {
						group.sendMessage(String.valueOf(i).getBytes());
					}
				} else {
					group.sendMessage(message.getBytes());
				}
			} while (alive);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	/*
	 * public void sendObject(Serializable object) { Message outgoing = new
	 * Message(); synchronized (this) { outgoing.setNumber(++messageCounter); }
	 * outgoing.setSource(id); outgoing.setPayload(object);
	 * outgoing.setCommand(Command.SEND);
	 * 
	 * try { ByteArrayOutputStream baos = new ByteArrayOutputStream();
	 * ObjectOutputStream os = new ObjectOutputStream(baos);
	 * os.writeObject(outgoing); byte[] data = baos.toByteArray(); long checksum
	 * = calculateChecksum(data);
	 * 
	 * if (data.length > MAX_PAYLOAD_SIZE) { throw new
	 * RuntimeException("Payload too large"); }
	 * 
	 * ByteBuffer tmpBuffer = ByteBuffer.allocate(MAX_MESSAGE_SIZE);
	 * tmpBuffer.putLong(checksum); tmpBuffer.putShort((short) data.length);
	 * tmpBuffer.put(data);
	 * 
	 * byte[] completeMessage = tmpBuffer.array(); DatagramPacket outgoingPacket
	 * = new DatagramPacket(completeMessage, MAX_MESSAGE_SIZE, group, port);
	 * 
	 * socket.send(outgoingPacket); } catch (IOException e) {
	 * e.printStackTrace(); }
	 * 
	 * // send
	 * 
	 * }
	 */
}

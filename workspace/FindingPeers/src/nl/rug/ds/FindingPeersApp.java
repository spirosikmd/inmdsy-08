package nl.rug.ds;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import nl.rug.ds.reliable.RMulticast;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class FindingPeersApp {

	static Logger logger = Logger.getLogger(FindingPeersApp.class);

	/**
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		Thread.currentThread().setName("Main");
		PropertyConfigurator.configure("log4j.properties");

		try {
			InetAddress group = InetAddress.getByName("239.1.2.4");
			int port = 1567;

			logger.info(" (m)Starting app to find peers in group "
					+ group.getHostAddress() + ":" + port);

			RMulticast peer = RMulticast.createPeer(group, port);

			String message;
			Scanner scanner = new Scanner(System.in);

			do {
				message = scanner.nextLine();
				if ("close".equals(message)) {
					break;
				} else if ("list".equals(message)) {
					System.out.println("List all the files");
				} else if ("test".equals(message)) {
					for (int i = 0 ; i < 100 ; i++) {
						peer.sendMessage(String.valueOf(i).getBytes());
						Thread.sleep(10);
					}
				} else {
					peer.sendMessage(message.getBytes());
				}
			} while (true);
			scanner.close();

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

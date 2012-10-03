package nl.rug.ds;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class FindingPeersApp {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		try {
			InetAddress group = InetAddress.getByName("239.1.2.4");
			int port = 1567;

			Peer peer = Peer.createPeer(group, port);

			String message;
			Scanner scanner = new Scanner(System.in);

			do {
				message = scanner.nextLine();
				if ("close".equals(message)) {
					break;
				} else if ("list".equals(message)) {
					System.out.println("List all the files");
				}
				peer.sendMessage(message);
			} while (true);
			scanner.close();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

}

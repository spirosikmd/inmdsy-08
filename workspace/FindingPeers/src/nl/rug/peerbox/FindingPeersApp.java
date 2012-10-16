package nl.rug.peerbox;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import nl.rug.peerbox.logic.Peerbox;

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

		// BasicConfigurator.configure();
		PropertyConfigurator.configure("log4j.properties");

		try {
			InetAddress address = InetAddress.getByName("239.1.2.4");
			int port = 1567;

			logger.info("Starting app to find peers in group "
					+ address.getHostAddress() + ":" + port);

			String message;
			Scanner scanner = new Scanner(System.in);
			System.out.print("Set Peerbox Path: ");
			String path = scanner.nextLine();

			Peerbox peerbox = new Peerbox(address, port, path);
			peerbox.join();

			boolean alive = true;
			do {
				message = scanner.nextLine();
				String[] parts = message.split(" ");
				String command = parts[0];
				String arg = "";
				if (parts.length == 2) {
					arg = parts[1];
				}
				if ("leave".equals(message)) {
					peerbox.leave();
					alive = false;
					scanner.close();
				} else if ("threads".equals(message)) {
					Thread.currentThread().getThreadGroup().list();
				} else if ("list".equals(message)) {
					peerbox.listFiles();
				} else if ("request".equals(message)) {
					peerbox.requestFiles();
				} else if ("get".equals(command)) {
					peerbox.getFile(arg);
				} else if ("test".equals(message)) {
					peerbox.testBulkData();
				}
			} while (alive);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

}

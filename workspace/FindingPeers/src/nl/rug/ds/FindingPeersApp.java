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
			
			
			Message m = new Message();
			m.setMessage("Hello, this is dog!");
			m.setNumber(1);
			m.setSource(3);
			
			for (byte b : m.toByte()) {
				System.out.print(b + ",");
			}
			//System.out.println(m.toByte().toString());
			
			
			do {
				 message = scanner.nextLine();
				peer.sendMessage(message);
			} while (! "close".equals(message));
			scanner.close();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}


}

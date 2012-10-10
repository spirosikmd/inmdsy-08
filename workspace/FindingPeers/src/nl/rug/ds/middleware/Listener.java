package nl.rug.ds.middleware;

import java.io.IOException;
import java.net.DatagramPacket;

final class Listener implements Runnable {
	private final RMulticastGroup group;

	Listener(RMulticastGroup group) {
		this.group = group;
	}

	public void run() {
		try {
			RMulticastGroup.logger.info("Start listening");
			while (!Thread.interrupted()) {
				DatagramPacket incoming = new DatagramPacket(
						new byte[RMulticastGroup.MAX_MESSAGE_SIZE], RMulticastGroup.MAX_MESSAGE_SIZE);
				group.getSocket().receive(incoming);
				group.getReceiver().pushDataPacket(incoming);
			}
		} catch (IOException ex) {
			RMulticastGroup.logger.error(ex);
		}
	}
}
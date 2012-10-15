package nl.rug.peerbox.logic;

import java.net.InetAddress;

import nl.rug.peerbox.middleware.Message;
import nl.rug.peerbox.middleware.MessageListener;
import nl.rug.peerbox.middleware.MulticastGroup;
import nl.rug.peerbox.middleware.RMulticastGroup;

import org.apache.log4j.Logger;

public class Peerbox implements MessageListener {

	private final MulticastGroup group;
	private final static Logger logger = Logger.getLogger(Peerbox.class);

	public Peerbox(InetAddress address, int port) {
		group = RMulticastGroup.createPeer(address, port);
		group.addMessageListener(this);
	}

	public void join() {
		PeerboxMessage message = new PeerboxMessage();
		message.put("COMMAND", "JOIN");
		message.put("IP", "myip");
		message.put("PORT", 50982);
		
		group.announce(message.serialize());
	}

	public void listFiles() {
		group.announce("sendmeyourfilelist".getBytes());
	}

	public void getFile(int fileid) {
		group.announce("sendmethefile".getBytes());
	}

	public void leave() {
		group.announce("I left".getBytes());
		group.shutdown();
	}

	public void testBulkData() {
		for (int i = 0; i < 100; i++) {
			group.announce(String.valueOf(i).getBytes());
		}
	}

	@Override
	public void receivedMessage(Message m) {
		
		PeerboxMessage message = PeerboxMessage.deserialize(m.getPayload());

		if (message != null) {
			
			
			// react on someone joining the group
			logger.info("Received a message in logic " + message.get("COMMAND"));
			// react on someone requesting filelist
			// react on someone updating his filelist
			// react on someone requesting file
		}
	}

}

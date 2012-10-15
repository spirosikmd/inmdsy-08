package nl.rug.peerbox.logic;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import nl.rug.peerbox.middleware.Message;
import nl.rug.peerbox.middleware.MessageListener;
import nl.rug.peerbox.middleware.MulticastGroup;
import nl.rug.peerbox.middleware.RMulticastGroup;

import org.apache.log4j.Logger;

public class Peerbox implements MessageListener {

	private final MulticastGroup group;
	private final String path;
	private final static Logger logger = Logger.getLogger(Peerbox.class);
	private Map<Host, String[]> filelist = new HashMap<Host, String[]>();

	public Peerbox(InetAddress address, int port, String path) {
		group = RMulticastGroup.createPeer(address, port);
		group.addMessageListener(this);
		this.path = path;
	}

	public void join() {
		PeerboxMessage message = new PeerboxMessage();
		message.put("COMMAND", "JOIN");
		message.put("IP", "myip");
		message.put("PORT", 50982);
		
		group.announce(message.serialize());
	}

	public void listFiles() {
		System.out.println("===FILE LIST BEGIN===");
		for (Entry<Host, String[]> entry : filelist.entrySet()) {
			Host h = entry.getKey();
			for (String f : entry.getValue()) {
				System.out.println(f + "  @" + h );
			}
		}
		System.out.println("===FILE LIST END===");
	}
	

	public void requestFiles() {
		PeerboxMessage message = new PeerboxMessage();
		message.put("COMMAND", "LIST");
		group.announce(message.serialize());		
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
			logger.info("Received a message in logic " + message.get("COMMAND"));
			if (message.get("COMMAND").equals("JOIN")) {
				PeerboxMessage reply = new PeerboxMessage();
				reply.put("COMMAND", "WELCOME");
			} else if(message.get("COMMAND").equals("LIST")) {
				
				try {
					String[] files = new String[0];
					File directory = new File(path);
					if (directory.isDirectory()) {
						files = directory.list();
					}
				    InetAddress addr = InetAddress.getLocalHost();
				    byte[] ipAddr = addr.getAddress();
				    
				    PeerboxMessage reply = new PeerboxMessage();
					reply.put("COMMAND", "LISTREPLY");
					reply.put("FILES", files);
					reply.put("IP", ipAddr);
					reply.put("PORT", 12345);
					group.announce(reply.serialize());
				    
				} catch (UnknownHostException e) {
				}
				
				
			} else if(message.get("COMMAND").equals("LISTREPLY")) {
				logger.info("List files from remote host");
				String[] files = (String[])message.get("FILES");
								
				byte[] ip = (byte[])message.get("IP");
				int port = (int)message.get("PORT");
				
				filelist.put(Host.byIpAndPort(ip, port), files);
				
			}
			
			// react on someone joining the group
			
			// react on someone requesting filelist
			// react on someone updating his filelist
			// react on someone requesting file
		}
	}

}

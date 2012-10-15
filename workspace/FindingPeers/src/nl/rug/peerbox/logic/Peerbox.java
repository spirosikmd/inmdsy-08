package nl.rug.peerbox.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

	private final int serverPort = 6666;

	public Peerbox(InetAddress address, int port, final String path) {
		group = RMulticastGroup.createPeer(address, port);
		group.addMessageListener(this);
		this.path = path;

		ExecutorService es = Executors.newSingleThreadExecutor();
		es.execute(new Runnable() {

			@Override
			public void run() {
				try (ServerSocket server = new ServerSocket(serverPort)) {
					try (Socket s = server.accept()) {

						PrintWriter put = new PrintWriter(s.getOutputStream(),
								true);
						BufferedReader st = new BufferedReader(
								new InputStreamReader(s.getInputStream()));
						String fileid = st.readLine();
						System.out.println("The requested file is : " + fileid);
						try (FileReader fstream = new FileReader(path + "/" + fileid)) {
							BufferedReader d = new BufferedReader(new FileReader(fileid));
							String line;
							while ((line = d.readLine()) != null) {
								put.write(line);
								put.flush();
							}
							d.close();
							System.out.println("File transfered");
						} finally {
						}
					} catch (IOException e) {
					}

				} catch (IOException e) {
					logger.error(e);
				}

			}
		});

	}

	public void join() {
		PeerboxMessage message = new PeerboxMessage();
		message.put("COMMAND", "JOIN");
		group.announce(message.serialize());
	}

	public void listFiles() {
		System.out.println("===FILE LIST BEGIN===");
		for (Entry<Host, String[]> entry : filelist.entrySet()) {
			Host h = entry.getKey();
			for (String f : entry.getValue()) {
				System.out.println(f + "  @" + h);
			}
		}
		System.out.println("===FILE LIST END===");
	}

	public void requestFiles() {
		PeerboxMessage message = new PeerboxMessage();
		message.put("COMMAND", "LIST");
		group.announce(message.serialize());
	}

	public void getFile(String filename) {
		
		Host h = findHostThatServesTheFileHelper(filename);
		
		try (Socket s = new Socket(h.address, h.port)) {
			BufferedReader get = new BufferedReader(new InputStreamReader(
					s.getInputStream()));
			PrintWriter put = new PrintWriter(s.getOutputStream(), true);
			put.println(filename);
			String u;
			File f1 = new File(filename);
			try (FileOutputStream fs = new FileOutputStream(f1)) {
				while ((u = get.readLine()) != null) {
					byte jj[] = u.getBytes();
					fs.write(jj);
				}
			}

		} catch (Exception e) {
			System.exit(0);
		}
	}

	private Host findHostThatServesTheFileHelper(String filename) {
		for (Entry<Host, String[]> entry : filelist.entrySet()) {
			Host h = entry.getKey();
			for (String f : entry.getValue()) {
				if (filename.equals(f)) {
					return h;
				}
			}
		}
		return null;
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
			} else if (message.get("COMMAND").equals("LIST")) {

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
					reply.put("PORT", serverPort);
					group.announce(reply.serialize());

				} catch (UnknownHostException e) {
				}

			} else if (message.get("COMMAND").equals("LISTREPLY")) {
				logger.info("List files from remote host");
				String[] files = (String[]) message.get("FILES");

				byte[] ip = (byte[]) message.get("IP");
				int port = (int) message.get("PORT");

				filelist.put(Host.byIpAndPort(ip, port), files);

			}

			// react on someone joining the group

			// react on someone requesting filelist
			// react on someone updating his filelist
			// react on someone requesting file
		}
	}

}

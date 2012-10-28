package nl.rug.peerbox.logic;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

public final class FileRequestTask implements Runnable {

	private static final int SOCKET_TIMEOUT = 5000;

	private static final Logger logger = Logger
			.getLogger(FileRequestTask.class);

	private final Peer h;
	private final String filename;
	private final Context ctx;
	private final PeerboxFile file;

	public FileRequestTask(PeerboxFile file, Context ctx) {
		this.file = file;
		this.h = file.getOwner();
		this.filename = file.getFilename();
		this.ctx = ctx;
	}

	@Override
	public void run() {
		File sharedFile = new File(ctx.getPathToPeerbox()
				+ System.getProperty("file.separator") + filename);
		
		try (Socket s = new Socket()) {
			s.connect(new InetSocketAddress(h.getAddress(), h.getPort()), SOCKET_TIMEOUT);
			logger.debug("Created direct connection to " + h.getAddress().toString() +":"+ h.getPort());
			PrintWriter put = new PrintWriter(s.getOutputStream(), true);
			put.println(filename);
			byte[] mybytearray = new byte[1024];
			InputStream is = s.getInputStream();

			FileOutputStream fos = new FileOutputStream(sharedFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos);

			int bytesRead;
			while ((bytesRead = is.read(mybytearray, 0, mybytearray.length)) != -1) {
				bos.write(mybytearray, 0, bytesRead);
			}
			bos.close();
			put.close();
			logger.info("File " + filename + " has been received");
			file.setFile(sharedFile);
		} catch (SocketTimeoutException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
//			sharedFile.delete();
//			sharedFile = null;
//			file.setFile(null);
		}
		
		
		//verify file with peerboxfile properties
		//filesize and content checksum		
	}
}
package nl.rug.peerbox.logic;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

final class FileRequestTask implements Callable<File> {
	
	private static final Logger logger = Logger.getLogger(FileRequestTask.class);
	
	private final Peer h;
	private final String filename;
	private final Context ctx;
	
	FileRequestTask(Peer h, String filename, Context ctx) {
		this.h = h;
		this.filename = filename;
		this.ctx = ctx;
	}

	@Override
	public File call() throws Exception {
		File sharedFile = new File(ctx.getPathToPeerbox() + "/" + filename);
		
		try (Socket s = new Socket(h.address, h.port)) {
			PrintWriter put = new PrintWriter(s.getOutputStream(), true);
			put.println(filename);
			byte[] mybytearray = new byte[1024];
			InputStream is = s.getInputStream();
			
			FileOutputStream fos = new FileOutputStream(sharedFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			
			int bytesRead;
			while ((bytesRead = is.read(mybytearray, 0,
					mybytearray.length)) != -1) {
				bos.write(mybytearray, 0, bytesRead);
			}
			bos.close();
			put.close();
			logger.info("File " + filename + " has been received");
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
			sharedFile.delete();
			sharedFile = null;
		}
		return sharedFile;
	}
}
package nl.rug.peerbox.logic;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

final class FileServer implements Runnable {
	
	private final String path;
	private final int port;

	private static final Logger logger = Logger.getLogger(FileServer.class);
	
	FileServer(Context ctx) {
		this.path = ctx.getPathToPeerbox();
		this.port = ctx.getPort();
	}

	@Override
	public void run() {
		try (ServerSocket server = new ServerSocket(port)) {

			while (true) {
				try (Socket s = server.accept()) {

					BufferedReader st = new BufferedReader(
							new InputStreamReader(s.getInputStream()));
					String fileid = st.readLine();
					System.out.println("The requested file is : "
							+ path + "/" + fileid);
					File myFile = new File(path + "/" + fileid);
					byte[] mybytearray = new byte[(int) myFile.length()];

					BufferedInputStream bis = new BufferedInputStream(
							new FileInputStream(myFile));

					bis.read(mybytearray, 0, mybytearray.length);

					OutputStream os = s.getOutputStream();
					os.write(mybytearray, 0, mybytearray.length);
					os.flush();
					bis.close();

				} catch (IOException e) {
					logger.error(e);
				}
			}

		} catch (IOException e) {
			logger.error(e);
		}

	}
}
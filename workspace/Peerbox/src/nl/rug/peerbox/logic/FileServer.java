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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

final class FileServer implements Runnable {


	private final Context ctx;

	private static final Logger logger = Logger.getLogger(FileServer.class);
	private final ExecutorService pool = Executors.newCachedThreadPool();

	FileServer(Context ctx) {
		this.ctx = ctx;
	}

	@Override
	public void run() {
		try (ServerSocket server = new ServerSocket(ctx.getPort())) {
			while (true) {
				logger.info("Waiting for incoming connection");
				try {
					final Socket s = server.accept();
					logger.debug("Accepted incoming connection from " + s.getRemoteSocketAddress());
					pool.execute(new SendFileTask(s, ctx));
				} catch (IOException e) {
					logger.error(e);
				}
			}

		} catch (IOException e) {
			logger.error(e);
		}

	}
	
	
	
	private final class SendFileTask implements Runnable {
		private final Socket s;
		private final Context ctx;

		private SendFileTask(Socket s, Context ctx) {
			this.s = s;
			this.ctx = ctx;
		}

		@Override
		public void run() {
			try {
				BufferedReader st = new BufferedReader(
						new InputStreamReader(s
								.getInputStream()));
				String fileid = st.readLine();
				logger.info("File " + fileid
						+ " has been requested.");
				File myFile = new File(ctx.getPathToPeerbox() + "/" + fileid);
				byte[] mybytearray = new byte[(int) myFile
						.length()];

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
	}
	
}
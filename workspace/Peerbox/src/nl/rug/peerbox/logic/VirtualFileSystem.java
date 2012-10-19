package nl.rug.peerbox.logic;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

public class VirtualFileSystem {

	private static ConcurrentHashMap<String, PeerboxFile> filelist;
	private final static Logger logger = Logger.getLogger(Peerbox.class);
	private final Context ctx;

	private VirtualFileSystem(Context ctx) {

		FileSystem fs = FileSystems.getDefault();
		this.ctx = ctx;

		try {
			Path path = fs.getPath(ctx.getPathToPeerbox());
			final WatchService watcher = fs.newWatchService();
			path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_MODIFY);

			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						while (true) {
							System.out.println("Wait for watchkey");
							WatchKey watckKey = watcher.take();
							System.out.println("got one");
							List<WatchEvent<?>> events = watckKey.pollEvents();

							for (WatchEvent<?> event : events) {
								if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
									System.out.println("Created: "
											+ event.context().toString());
								}
								if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
									System.out.println("Delete: "
											+ event.context().toString());
								}
								if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
									System.out.println("Modify: "
											+ event.context().toString());
								}
							}
							watckKey.reset();
						}
					} catch (InterruptedException e) {
					} finally {
						System.out.println("finished");
					}

				}
			}).start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	};

	public static VirtualFileSystem initVirtualFileSystem(Context ctx) {
		VirtualFileSystem vfs = new VirtualFileSystem(ctx);

		filelist = new ConcurrentHashMap<String, PeerboxFile>(8, 0.9f, 1);

		File directory = new File(ctx.getPathToPeerbox());

		File f = new File(directory.getAbsolutePath() + "/data.pbx");
		if (f.exists()) {
			vfs.deserializeFilelist();
		}

		if (directory.isDirectory()) {
			for (String filename : directory.list()) {
				if (!filelist.containsValue(filename)
						&& !filename.equals("data.pbx")) {
					filelist.put(filename,
							new PeerboxFile(filename, ctx.getLocalPeer()));
				}
			}
		}
		vfs.serializeFilelist();

		return vfs;
	}

	public ConcurrentHashMap<String, PeerboxFile> getFileList() {
		return filelist;
	}

	public void serializeFilelist() {
		try {
			String path = ctx.getPathToPeerbox();
			OutputStream file = new FileOutputStream(path
					+ this.ctx.getProperties().getProperty(
							Property.DATAFILE_NAME));
			OutputStream buffer = new BufferedOutputStream(file);
			try (ObjectOutput output = new ObjectOutputStream(buffer)) {
				output.writeObject(filelist);
			}
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public void deserializeFilelist() {
		try {
			String path = ctx.getPathToPeerbox();
			InputStream file = new FileInputStream(path
					+ ctx.getProperties().getProperty(Property.DATAFILE_NAME));
			InputStream buffer = new BufferedInputStream(file);
			try (ObjectInput input = new ObjectInputStream(buffer)) {
				filelist = (ConcurrentHashMap<String, PeerboxFile>) input
						.readObject();
			}
		} catch (ClassNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}
}

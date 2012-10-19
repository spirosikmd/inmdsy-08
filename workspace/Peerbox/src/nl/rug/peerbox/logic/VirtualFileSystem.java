package nl.rug.peerbox.logic;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

public class VirtualFileSystem {

	private Filelist filelist;

	private VirtualFileSystem(Context ctx) {

		FileSystem fs = FileSystems.getDefault();

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
							// send list of events in one message
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

		vfs.filelist = new Filelist();

		File directory = new File(ctx.getPathToPeerbox());
		String datafile = ctx.getProperties().getProperty(
				Property.DATAFILE_NAME);
		String path = ctx.getProperties().getProperty(Property.PATH);

		File f = new File(directory.getAbsolutePath() + "/"
				+ ctx.getProperties().getProperty(Property.DATAFILE_NAME));
		if (f.exists()) {
			vfs.filelist.deserialize(datafile, path);
		}

		if (directory.isDirectory()) {
			for (String filename : directory.list()) {
				if (!vfs.filelist.containsValue(filename)
						&& !filename.equals(ctx.getProperties().getProperty(
								Property.DATAFILE_NAME))
						&& !filename.startsWith(".")) {
					PeerboxFile pbxf = new PeerboxFile(filename,
							ctx.getLocalPeer());
					vfs.filelist.put(pbxf.getUFID(), pbxf);
				}
			}
		}
		vfs.filelist.serialize(datafile, path);

		return vfs;
	}

	public Filelist getFileList() {
		return filelist;
	}
}

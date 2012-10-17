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
import java.util.ArrayList;
import java.util.List;

public class VirtualFileSystem {

	private static ArrayList<FileDescriptor> filelist = new ArrayList<FileDescriptor>();
	private final Context ctx;

	private VirtualFileSystem(Context ctx) {
		this.ctx = ctx;
		FileSystem fs = FileSystems.getDefault();

		try {
			Path path = fs.getPath(ctx.getPathToPeerbox());
			final WatchService watcher = fs.newWatchService();
			path.register(watcher,
					StandardWatchEventKinds.ENTRY_CREATE,
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

							for (WatchEvent event : events) {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};

	public static VirtualFileSystem initVirtualFileSystem(Context ctx) {
		VirtualFileSystem vfs = new VirtualFileSystem(ctx);

		File directory = new File(ctx.getPathToPeerbox());
		if (directory.isDirectory()) {
			for (String file : directory.list()) {
				filelist.add(new FileDescriptor(file, ctx.getLocalPeer()));
			}
		}

		return vfs;
	}

	public ArrayList<FileDescriptor> getFileList() {
		return filelist;
	}

}

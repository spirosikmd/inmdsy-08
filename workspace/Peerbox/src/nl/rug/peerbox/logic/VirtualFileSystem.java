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
import java.util.Collection;
import java.util.List;

public class VirtualFileSystem {

	private Filelist filelist;
	private final Context ctx;
	private final List<VFSListener> listeners = new ArrayList<VFSListener>();

	private VirtualFileSystem(final Context ctx) {

		this.ctx = ctx;
		FileSystem fs = FileSystems.getDefault();

		try {
			Path path = fs.getPath(ctx.getPathToPeerbox());
			final WatchService watcher = fs.newWatchService();
			path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_MODIFY);

			Thread peerboxObserver = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						while (true) {
							WatchKey watckKey = watcher.take();
							List<WatchEvent<?>> events = watckKey.pollEvents();
							// send list of events in one message
							// ctx.sendChanges(..., ...);
							// update own vfs with the events
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
						System.out.println("watcher finished");
					}

				}
			});
			peerboxObserver.setDaemon(true);
			// peerboxObserver.start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	};

	public static VirtualFileSystem initVirtualFileSystem() {
		Context ctx = Peerbox.getInstance();
		VirtualFileSystem vfs = new VirtualFileSystem();

		vfs.filelist = new Filelist();

		String path = ctx.getPathToPeerbox();
		File directory = new File(path);
		String datafile = ctx.getDatafileName();

		File f = new File(directory.getAbsolutePath()
				+ System.getProperty("file.separator") + datafile);
		if (f.exists()) {
			vfs.filelist = Filelist.deserialize(datafile, path);
		}

		if (directory.isDirectory()) {
			for (String filename : directory.list()) {
				PeerboxFile file = new PeerboxFile(filename, ctx.getLocalPeer());
				if (!filename.equals(datafile) && !filename.startsWith(".")) {
					if (!vfs.filelist.containsKey(file.getUFID())) {
						vfs.filelist.put(file.getUFID(), file);
					}
				}
			}
		}
		vfs.filelist.serialize(datafile, path);
		vfs.notifyListeners();
		return vfs;
	}

	public void addFile(PeerboxFile file) {
		if (!filelist.containsKey(file.getUFID())) {
			filelist.put(file.getUFID(), file);
			filelist.serialize(ctx.getDatafileName(), ctx.getPathToPeerbox());
		}
		notifyListeners();
	}

	public PeerboxFile removeFile(UFID ufid) {
		notifyListeners();
		return null;
	}

	public void addVFSListener(VFSListener l) {
		listeners.add(l);
	}

	public void removeVFSListener(VFSListener l) {
		listeners.remove(l);
	}

	public Collection<PeerboxFile> getFileList() {
		return new ArrayList<PeerboxFile>(filelist.values());
	}

	private void notifyListeners() {
		for (VFSListener l : listeners) {
			l.updated();
		}
	}
}

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

import nl.rug.peerbox.logic.messaging.Message;
import nl.rug.peerbox.logic.messaging.Message.Command;
import nl.rug.peerbox.logic.messaging.Message.Key;

import org.apache.log4j.Logger;
import org.apache.log4j.pattern.FullLocationPatternConverter;

public class VirtualFileSystem implements PeerListener {

	private Filelist filelist;
	private final List<VFSListener> listeners = new ArrayList<VFSListener>();
	private final Context ctx;
	private static final Logger logger = Logger
			.getLogger(VirtualFileSystem.class);

	private VirtualFileSystem(final Context ctx) {

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
							for (WatchEvent<?> event : events) {
								if (!(event.context() instanceof Path)) {
									continue;
								}

								Path path = (Path) event.context();

								String filename = path.toString();
								File file = new File(ctx.getPathToPeerbox(),
										filename);
								if (file.isDirectory() || file.isHidden()) {
									continue;
								}

								if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
									if (file.isFile()) {
										logger.info("Detected file created event "
												+ event.context().toString());
										addFile(file);
									}

								}
								if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
									logger.info("Detect file deleted event " 
											+ event.context().toString());
									if (event.context() instanceof Path) {
										PeerboxFile pbf = new PeerboxFile(
												filename, ctx.getLocalPeer());
										if (removeFile(pbf.getUFID()) != null) {

										}
									}
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
			peerboxObserver.start();

		} catch (IOException e) {
			e.printStackTrace();
		}
		this.ctx = ctx;
		ctx.addPeerListener(this);

	}

	public static VirtualFileSystem initVirtualFileSystem(Context ctx) {
		File folder = new File(ctx.getPathToPeerbox());
		if (!folder.exists()) {
			folder.mkdirs();
		}

		VirtualFileSystem vfs = new VirtualFileSystem(ctx);

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
			for (File file : directory.listFiles()) {
				if (file.isFile() && !file.isHidden()) {
					String filename = file.getName();
					PeerboxFile pbf = new PeerboxFile(filename,
							ctx.getLocalPeer(), file);
					if (!filename.equals(datafile)) {
						vfs.addFile(pbf);
					}
				}
			}
		}
		vfs.filelist.serialize(datafile, path);
		return vfs;
	}

	private void addFile(File file) {
		PeerboxFile pbf = new PeerboxFile(file.getName(), ctx.getLocalPeer(),
				file);
		for (PeerboxFile f : filelist.values()) {
			if (file.equals(f.getFile())) {
				return;
			}
		}
		addFile(pbf);
	}

	public void addFile(PeerboxFile file) {
		if (!filelist.containsKey(file.getUFID())) {
			filelist.put(file.getUFID(), file);
			notifyAboutAddedFile(file);
			Message update = new Message();
			update.put(Key.Command, Command.Created);
			update.put(Key.Peer, ctx.getLocalPeer());
			update.put(Key.File, file);
			ctx.getMulticastGroup().announce(update.serialize());
		}
	}

	public PeerboxFile removeFile(UFID ufid) {
		PeerboxFile f = filelist.remove(ufid);
		logger.info("Remove " + ufid + " from VFS");
		if (f != null) {
			logger.info("Actually removed");
			notifyAboutDeletedFile(f);
			Message update = new Message();
			update.put(Key.Command, Command.Deleted);
			update.put(Key.Peer, ctx.getLocalPeer());
			update.put(Key.FileId, f.getUFID());
			ctx.getMulticastGroup().announce(update.serialize());
		}
		return f;
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

	private void notifyAboutAddedFile(PeerboxFile f) {
		for (VFSListener l : listeners) {
			l.added(f);
		}
	}

	private void notifyAboutDeletedFile(PeerboxFile f) {
		for (VFSListener l : listeners) {
			l.deleted(f);
		}
	}

	private void notifyAboutUpdatedFile(final PeerboxFile f) {
		for (VFSListener l : listeners) {
			l.updated(f);
		}
	}

	@Override
	public void deleted(PeerHost ph) {
		for (PeerboxFile f : filelist.values()) {
			if (f.getOwner().equals(ph.getPeer())) {
				removeFile(f.getUFID());
			}
		}
	}

	@Override
	public void joined(PeerHost peerHost) {
		refresh();
	}

	@Override
	public void updated(PeerHost ph) {
	}

	public void refresh() {
		Message askForFiles = new Message();
		askForFiles.put(Key.Command, Command.Refresh);
		askForFiles.put(Key.Peer, ctx.getLocalPeer());
		ctx.getMulticastGroup().announce(askForFiles.serialize());

	}
}

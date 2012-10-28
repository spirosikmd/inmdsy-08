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

public class VirtualFileSystem implements PeerListener {

	private Filelist filelist;
	private final List<VFSListener> listeners = new ArrayList<VFSListener>();
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
								if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
									logger.info("Detect file created event "
											+ event.context().toString());
									if (event.context() instanceof Path) {
										String path = ctx.getPathToPeerbox();
										File directory = new File(path);
										String filename = event.context().toString();
										File file = new File(
												directory.getAbsolutePath()
														+ System.getProperty("file.separator")
														+ filename);
										if (file.isFile() && !file.isHidden()) {
											PeerboxFile pbf = new PeerboxFile(
													file.getName(), ctx.getLocalPeer(), file);
											addFile(pbf);
											Message update = new Message();
											update.put(Key.Command,
													Command.Info.Created);
											update.put(Key.Peer,
													ctx.getLocalPeer());
											update.put(Key.File, pbf);
											ctx.getMulticastGroup().announce(
													update.serialize());
										}
									}
								}
								if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
									logger.info("Detect file deleted event "
											+ event.context().toString());
									if (event.context() instanceof Path) {
										String filename = event.context().toString();
										PeerboxFile pbf = new PeerboxFile(filename, ctx.getLocalPeer());
										if (removeFile(pbf.getUFID()) != null) {
											Message update = new Message();
											update.put(Key.Command,
													Command.Info.Deleted);
											update.put(Key.Peer,
													ctx.getLocalPeer());
											update.put(Key.FileId,
													pbf.getUFID());
											ctx.getMulticastGroup().announce(
													update.serialize());
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
					} 
				finally {
						System.out.println("watcher finished");
					}

				}
			});
			peerboxObserver.setDaemon(true);
			peerboxObserver.start();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
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

	public void addFile(PeerboxFile file) {
		if (!filelist.containsKey(file.getUFID())) {
			filelist.put(file.getUFID(), file);
			notifyAboutAddedFile(file);
		}
	}

	public PeerboxFile removeFile(UFID ufid) {
		PeerboxFile f = filelist.remove(ufid);
		logger.info("Remove " + ufid + " from VFS");
		if (f != null) {
			logger.info("Actually removed");
			notifyAboutDeletedFile(f);
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

	private void notifyAboutUpdatedFile(PeerboxFile f) {
		for (VFSListener l : listeners) {
			l.updated(f);
		}
	}

	@Override
	public void updated(int hostID, Peer peer) {
		
	}

	@Override
	public void deleted(int hostID, Peer peer) {
		for (PeerboxFile f : filelist.values()) {
			if (f.getOwner().equals(peer)) {
				removeFile(f.getUFID());
			}
		}
	}
}

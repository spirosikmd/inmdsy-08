package nl.rug.peerbox.logic;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PeerboxFile implements Serializable {

	private static final long serialVersionUID = 1L;
	private UFID ufid;
	private int version;
	private Peer owner;
	private String filename;
	private transient File file = null;
	private transient List<PeerboxFileListener> listeners;

	public PeerboxFile(String filename, Peer owner) {
		this(filename, owner, null);
	}

	public PeerboxFile(String filename, Peer owner, File file) {
		this.filename = filename;
		this.file = file;
		this.ufid = new UFID(filename, owner);
		this.owner = owner;
		listeners = new ArrayList<PeerboxFileListener>();
	}

	public String getFilename() {
		return filename;
	}

	public UFID getUFID() {
		return ufid;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Peer getOwner() {
		return owner;
	}

	public boolean isOwn() {
		return getOwner().equals(Peerbox.getInstance().getLocalPeer());
	}

	public boolean exists() {
		return (file != null && file.exists());
	}

	public File getFile() {
		return file;
	}

	public void setFile(File sharedFile) {
		this.file = sharedFile;
		notifyListeners();
	}

	public void addListener(PeerboxFileListener l) {
		listeners.add(l);
	}

	public void removeListener(PeerboxFileListener l) {
		listeners.remove(l);
	}

	private void notifyListeners() {
		for (PeerboxFileListener l : listeners) {
			l.modelUpdated();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof PeerboxFile)) {
			return false;
		}

		PeerboxFile otherfile = (PeerboxFile) obj;

		return ufid.equals(otherfile.getUFID());
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		listeners = new ArrayList<PeerboxFileListener>();
	}

}
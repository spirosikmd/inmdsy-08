package nl.rug.peerbox.logic;

import java.io.File;
import java.io.Serializable;

public class PeerboxFile implements Serializable {

	private static final long serialVersionUID = 1L;
	private UFID ufid;
	private int version;
	private Peer owner;
	private String filename;

	public PeerboxFile(String filename, Peer owner) {
		this.filename = filename;
		this.ufid = new UFID(filename, owner);
		this.owner = owner;
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

	public File getFile() {
		return new File(filename);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof PeerboxFile)) {
			return false;
		}

		PeerboxFile otherfile = (PeerboxFile) obj;

		return ufid.equals(otherfile.getUFID());
	}
}

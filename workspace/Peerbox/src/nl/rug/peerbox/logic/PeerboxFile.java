package nl.rug.peerbox.logic;

import java.io.File;

public class PeerboxFile extends File {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int ufid;
	private int version;
	private Peer owner;

	public PeerboxFile(String filename, Peer owner) {
		super(filename);
		this.ufid = filename.hashCode();
		this.owner = owner;
	}

	public int getUFID() {
		return ufid;
	}

	public int getVersion() {
		return version;
	}

	public Peer getOwner() {
		return owner;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof PeerboxFile)) {
			return false;
		}

		PeerboxFile otherfile = (PeerboxFile) obj;

		return (ufid == otherfile.getUFID())
				&& this.getName().equals(otherfile.getName());
	}
}

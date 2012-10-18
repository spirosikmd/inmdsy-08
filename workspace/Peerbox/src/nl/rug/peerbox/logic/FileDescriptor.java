package nl.rug.peerbox.logic;

import java.io.Serializable;

public class FileDescriptor implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int ufid;
	private int version;
	private String filename;
	private Peer owner;

	public FileDescriptor(String filename, Peer owner) {
		this.filename = filename;
		this.ufid = filename.hashCode();
		this.owner = owner;
	}

	public int getUFID() {
		return ufid;
	}

	public int getVersion() {
		return version;
	}

	public String getFilename() {
		return filename;
	}

	public Peer getOwner() {
		return owner;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof FileDescriptor)) {
			return false;
		}

		FileDescriptor otherfile = (FileDescriptor) obj;

		return (ufid == otherfile.getUFID())
				&& filename.equals(otherfile.getFilename());
	}
}

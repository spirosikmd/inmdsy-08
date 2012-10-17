package nl.rug.peerbox.logic;

import java.io.Serializable;
import java.util.UUID;

public class FileDescriptor implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UUID ufid;
	private int version;
	private String filename;
	private Peer owner;

	public FileDescriptor(String filename, Peer owner) {
		this.ufid = UUID.randomUUID();
		this.filename = filename;
		this.owner = owner;
	}

	public UUID getUFID() {
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

		return (ufid.equals(otherfile.getUFID()) && filename.equals(otherfile
				.getFilename()));
	}
}

package nl.rug.peerbox.logic;

public class FileDescriptor {

	private String filename;
	private Peer owner;

	public FileDescriptor(String filename, Peer owner) {
		this.filename = filename;
		this.owner = owner;
	}

	public String getFilename() {
		return filename;
	}

	public Peer getOwner() {
		return owner;
	}

}

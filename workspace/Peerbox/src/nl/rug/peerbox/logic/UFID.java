package nl.rug.peerbox.logic;

import java.io.Serializable;

public class UFID implements Serializable {

	private static final long serialVersionUID = 1L;
	private String fileid;
	private String owner;

	public UFID(String filename, Peer owner) {
		this.fileid = MD5Util.md5(filename);
		this.owner = MD5Util.md5(owner.getName());
		// plus md5 of data
	}

	public String getFileid() {
		return fileid;
	}

	public String getOwner() {
		return owner;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof UFID)) {
			return false;
		}

		UFID otherUFID = (UFID) obj;

		return fileid.equals(otherUFID.getFileid())
				&& owner.equals(otherUFID.getOwner());
	}
	
	@Override
	public int hashCode() {
		return new String(fileid + "" + owner).hashCode();
	}

	@Override
	public String toString() {
		return fileid.concat(owner);
	}
}
package nl.rug.peerbox.logic;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UFID implements Serializable {

	private static final long serialVersionUID = 1L;
	private String fileid;
	private String owner;

	public UFID(String filename, Peer owner) {
		this.fileid = md5(filename);
		this.owner = md5(owner.getName());
		// plus md5 of data
	}

	public String getFileid() {
		return fileid;
	}

	public String getOwner() {
		return owner;
	}

	private String md5(String input) {
		String md5 = null;

		if (null == input) {
			return null;
		}

		try {
			// Create MessageDigest object for MD5
			MessageDigest digest = MessageDigest.getInstance("MD5");
			// Update input string in message digest
			digest.update(input.getBytes(), 0, input.length());
			// Converts message digest value in base 16 (hex)
			md5 = new BigInteger(1, digest.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return md5;
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
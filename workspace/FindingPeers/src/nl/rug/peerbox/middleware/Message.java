package nl.rug.peerbox.middleware;

public interface Message {

	
	public int getPeerID();

	public int getMessageID();

	public byte[] getPayload();
	
}

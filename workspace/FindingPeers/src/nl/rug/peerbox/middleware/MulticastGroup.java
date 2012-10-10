package nl.rug.peerbox.middleware;


public interface MulticastGroup {

	
	public void announce(byte[] message);
	
	public void addMessageListener(MessageListener ml);
	
	public void shutdown();	
}

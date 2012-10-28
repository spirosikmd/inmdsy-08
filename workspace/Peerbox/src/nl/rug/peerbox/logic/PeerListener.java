package nl.rug.peerbox.logic;

public interface PeerListener {
	
	void updated(int hostID, Peer peer);
	void deleted(int hostID, Peer peer);

}

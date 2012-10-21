package nl.rug.peerbox.logic;

public interface VFSListener {
	
	void added(PeerboxFile f);
	
	void deleted(PeerboxFile f);
	
	void updated(PeerboxFile f);

}

package nl.rug.ds.middleware;

class Peer {

	private int hostID;
	private int processedMessageID;
	private int actualMessageID;

	static Peer find(int hostID) {
		Peer p  = new Peer();
		p.setHostID(hostID);
		return p;
	}
	
	int getHostID() {
		return hostID;
	}

	void setHostID(int hostID) {
		this.hostID = hostID;
	}

	int getReceivedMessageID() {
		return processedMessageID;
	}

	void setReceivedMessageID(int receivedMessageID) {
		this.processedMessageID = receivedMessageID;
	}

	int getSeenMessageID() {
		return actualMessageID;
	}

	void setSeenMessageID(int seenMessageID) {
		this.actualMessageID = seenMessageID;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Peer) {
			Peer other = (Peer)obj;
			return this.hostID == other.hostID;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return hostID;
	}

}

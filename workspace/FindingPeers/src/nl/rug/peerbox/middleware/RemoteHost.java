package nl.rug.peerbox.middleware;

class RemoteHost {

	private int hostID;
	private int processedMessageID;
	private int actualMessageID;

	static RemoteHost find(int hostID) {
		RemoteHost p  = new RemoteHost();
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
		if (obj != null && obj instanceof RemoteHost) {
			RemoteHost other = (RemoteHost)obj;
			return this.hostID == other.hostID;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return hostID;
	}

}

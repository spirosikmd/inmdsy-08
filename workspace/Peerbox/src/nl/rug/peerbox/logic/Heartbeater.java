package nl.rug.peerbox.logic;

public class Heartbeater implements Runnable {

	@Override
	public void run() {
		
		// every 30 seconds do the following
			//send own heartbeat
			//iterate over peers
				//if peers.lastHeartbeat > 120seconds 
					//this peer is dead 
					//do stuff	
	}

}

package nl.rug.peerbox.logic.handler;

import nl.rug.peerbox.logic.PeerboxMessage;

final class ListMessageHandler extends MessageHandler {
	
	static {
		registerHandler(new ListMessageHandler(), "LIST");
	}
	
	@Override
	void handle(PeerboxMessage message) {
		//handle list
	}
	
}
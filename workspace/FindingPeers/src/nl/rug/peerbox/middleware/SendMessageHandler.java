package nl.rug.peerbox.middleware;

class SendMessageHandler extends MessageHandler {
	
	static {
		MessageHandler.register(MulticastMessage.MESSAGE, new SendMessageHandler());
	}
	
	@Override
	void processMessage(MulticastMessage m) {
		
	}
}
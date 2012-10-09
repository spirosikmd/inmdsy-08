package nl.rug.ds.middleware;

class SendMessageHandler extends MessageHandler {
	
	static {
		MessageHandler.register(Message.MESSAGE, new SendMessageHandler());
	}
	
	@Override
	void processMessage(Message m) {
		
	}
}
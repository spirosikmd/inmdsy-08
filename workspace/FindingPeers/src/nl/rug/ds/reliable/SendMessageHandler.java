package nl.rug.ds.reliable;

class SendMessageHandler extends MessageHandler {
	
	static {
		MessageHandler.register(Message.SEND, new SendMessageHandler());
	}
	
	@Override
	void processMessage(Message m) {
		
	}
}
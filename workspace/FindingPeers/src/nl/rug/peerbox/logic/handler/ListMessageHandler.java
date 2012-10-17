package nl.rug.peerbox.logic.handler;

import java.io.File;

import nl.rug.peerbox.logic.Context;
import nl.rug.peerbox.logic.Peerbox;
import nl.rug.peerbox.logic.PeerboxMessage;

final class ListMessageHandler extends MessageHandler {

	static {
		registerHandler(new ListMessageHandler(), "LIST");
	}

	@Override
	void handle(PeerboxMessage message, Context ctx) {

		String[] files = new String[0];
		File directory = new File(ctx.getPathToPeerbox());
		if (directory.isDirectory()) {
			files = directory.list();
		}

		PeerboxMessage reply = new PeerboxMessage();
		reply.put(Peerbox.KEY_COMMAND, "LISTREPLY");
		reply.put("FILES", files);
		reply.put("IP", ctx.getIP());
		reply.put("PORT", ctx.getPort());
		ctx.getMulticastGroup().announce(reply.serialize());
		
		
		
	}

}
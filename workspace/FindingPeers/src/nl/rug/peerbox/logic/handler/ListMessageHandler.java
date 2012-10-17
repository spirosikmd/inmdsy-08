package nl.rug.peerbox.logic.handler;

import java.io.File;

import nl.rug.peerbox.logic.Context;
import nl.rug.peerbox.logic.PeerboxMessage;
import nl.rug.peerbox.logic.PeerboxMessage.Key;

final class ListMessageHandler extends MessageHandler {


	@Override
	void handle(PeerboxMessage message, Context ctx) {

		String[] files = new String[0];
		File directory = new File(ctx.getPathToPeerbox());
		if (directory.isDirectory()) {
			files = directory.list();
		}

		PeerboxMessage reply = new PeerboxMessage();
		reply.put(Key.Command, "LISTREPLY");
		reply.put(Key.Files, files);
		reply.put(Key.IP, ctx.getIP());
		reply.put(Key.Port, ctx.getPort());
		ctx.getMulticastGroup().announce(reply.serialize());
		
		
		
	}

}
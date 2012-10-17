package nl.rug.peerbox.logic.handler;

import nl.rug.peerbox.logic.Context;
import nl.rug.peerbox.logic.Peer;
import nl.rug.peerbox.logic.Message;
import nl.rug.peerbox.logic.Message.Key;



final class ReplyToListMessageHandler extends MessageHandler {
	
	
	@Override
	final void handle(Message message, Context ctx) {
		
		String[] files = (String[]) message.get(Key.Files);
		Object obj = message.get(Key.Peer);
		if (obj instanceof Peer) {
			Peer peer = (Peer)obj;
			ctx.getVirtualFilesystem().put(peer, files);
		}

		
	}

}

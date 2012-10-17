package nl.rug.peerbox.logic.handler;

import nl.rug.peerbox.logic.Context;
import nl.rug.peerbox.logic.Peer;
import nl.rug.peerbox.logic.Message;
import nl.rug.peerbox.logic.Message.Key;



final class ReplyToListMessageHandler extends MessageHandler {
	
	
	@Override
	final void handle(Message message, Context ctx) {
		
		String[] files = (String[]) message.get(Key.Files);
		byte[] ip = (byte[]) message.get(Key.IP);
		int port = (int) message.get(Key.Port);
		
		ctx.getVirtualFilesystem().put(Peer.byIpAndPort(ip, port), files);
		
	}

}

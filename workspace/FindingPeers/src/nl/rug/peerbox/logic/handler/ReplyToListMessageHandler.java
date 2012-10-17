package nl.rug.peerbox.logic.handler;

import nl.rug.peerbox.logic.Context;
import nl.rug.peerbox.logic.Host;
import nl.rug.peerbox.logic.PeerboxMessage;
import nl.rug.peerbox.logic.PeerboxMessage.Key;



final class ReplyToListMessageHandler extends MessageHandler {
	
	
	@Override
	final void handle(PeerboxMessage message, Context ctx) {
		
		String[] files = (String[]) message.get(Key.Files);
		byte[] ip = (byte[]) message.get(Key.IP);
		int port = (int) message.get(Key.Port);
		
		ctx.getVirtualFilesystem().put(Host.byIpAndPort(ip, port), files);
		
	}

}

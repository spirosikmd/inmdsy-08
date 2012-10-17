package nl.rug.peerbox.logic.handler;

import nl.rug.peerbox.logic.Context;
import nl.rug.peerbox.logic.Host;
import nl.rug.peerbox.logic.PeerboxMessage;



final class ReplyToListMessageHandler extends MessageHandler {
	
	
	@Override
	final void handle(PeerboxMessage message, Context ctx) {
		
		String[] files = (String[]) message.get("FILES");
		byte[] ip = (byte[]) message.get("IP");
		int port = (int) message.get("PORT");
		
		ctx.getVirtualFilesystem().put(Host.byIpAndPort(ip, port), files);
		
	}

}

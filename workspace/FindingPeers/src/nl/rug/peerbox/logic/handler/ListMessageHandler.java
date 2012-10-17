package nl.rug.peerbox.logic.handler;

import java.io.File;
import java.util.ArrayList;

import nl.rug.peerbox.logic.Context;
import nl.rug.peerbox.logic.FileDescriptor;
import nl.rug.peerbox.logic.Peerbox;
import nl.rug.peerbox.logic.PeerboxMessage;

final class ListMessageHandler extends MessageHandler {

	static {
		registerHandler(new ListMessageHandler(), "LIST");
	}

	@Override
	void handle(PeerboxMessage message, Context ctx) {

		ArrayList<FileDescriptor> filelist = ctx.getVirtualFilesystem();
		File directory = new File(ctx.getPathToPeerbox());
		if (directory.isDirectory()) {
			for (String file : directory.list()) {
				filelist.add(new FileDescriptor(file, ctx.getIP(), ctx
						.getPort()));
			}
		}

		PeerboxMessage reply = new PeerboxMessage();
		reply.put(Peerbox.KEY_COMMAND, "LISTREPLY");
		reply.put("FILES", filelist);
		// reply.put("IP", ctx.getIP());
		// reply.put("PORT", ctx.getPort());
		ctx.getMulticastGroup().announce(reply.serialize());

	}

}
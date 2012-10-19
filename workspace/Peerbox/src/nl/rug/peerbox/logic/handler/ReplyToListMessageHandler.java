package nl.rug.peerbox.logic.handler;

import java.util.Map.Entry;

import nl.rug.peerbox.logic.Context;
import nl.rug.peerbox.logic.Filelist;
import nl.rug.peerbox.logic.Message;
import nl.rug.peerbox.logic.Message.Key;
import nl.rug.peerbox.logic.Peer;
import nl.rug.peerbox.logic.PeerboxFile;

final class ReplyToListMessageHandler extends MessageHandler {

	@Override
	final void handle(Message message, Context ctx) {

		Filelist messageFilelist = (Filelist) message.get(Key.Files);
		Filelist localfilelist = ctx.getVirtualFilesystem().getFileList();

		Object obj = message.get(Key.Peer);
		if (obj instanceof Peer) {
			for (Entry<String, PeerboxFile> entry : messageFilelist.entrySet()) {
				if (!localfilelist.containsValue(entry.getValue())) {
					localfilelist.put(entry.getKey(), entry.getValue());
				}
			}
			localfilelist.serialize(ctx);
			System.out.println("serialized after update from request");
		}

	}

}

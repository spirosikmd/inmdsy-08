package nl.rug.peerbox.logic.messaging;

import nl.rug.peerbox.logic.Context;
import nl.rug.peerbox.logic.UFID;
import nl.rug.peerbox.logic.messaging.Message.Key;

public class InfoDeletedMessageHandler extends MessageHandler {

	@Override
	void handle(Message message, Context ctx) {
		
		Object o = message.get(Key.FileId);
		if (o instanceof UFID) {
			UFID ufid = (UFID)o;
			ctx.getVirtualFilesystem().removeFile(ufid);
		}

	}

}

package nl.rug.peerbox.logic.handler;

import java.util.ArrayList;

import nl.rug.peerbox.logic.Context;
import nl.rug.peerbox.logic.FileDescriptor;
import nl.rug.peerbox.logic.Message;
import nl.rug.peerbox.logic.Message.Command;
import nl.rug.peerbox.logic.Message.Key;

final class ListMessageHandler extends MessageHandler {

	@Override
	void handle(Message message, Context ctx) {

		ArrayList<FileDescriptor> files = ctx.getVirtualFilesystem().getFileList();

		Message reply = new Message();
		reply.put(Key.Command, Command.Reply.List);
		reply.put(Key.Files, files);
		reply.put(Key.Peer, ctx.getLocalPeer());
		ctx.getMulticastGroup().announce(reply.serialize());

	}

}
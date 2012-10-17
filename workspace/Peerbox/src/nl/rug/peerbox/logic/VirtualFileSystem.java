package nl.rug.peerbox.logic;

import java.io.File;
import java.util.ArrayList;

public class VirtualFileSystem {

	private static ArrayList<FileDescriptor> filelist = new ArrayList<FileDescriptor>();
	private final Context ctx;

	private VirtualFileSystem(Context ctx) {
		this.ctx = ctx;
	};

	public static VirtualFileSystem initVirtualFileSystem(Context ctx) {
		VirtualFileSystem vfs = new VirtualFileSystem(ctx);

		File directory = new File(ctx.getPathToPeerbox());
		if (directory.isDirectory()) {
			for (String file : directory.list()) {
				filelist.add(new FileDescriptor(file, ctx.getLocalPeer()));
			}
		}

		return vfs;
	}

	public ArrayList<FileDescriptor> getFileList() {
		return filelist;
	}

}

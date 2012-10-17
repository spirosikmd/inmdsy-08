package nl.rug.peerbox.logic;

import java.util.ArrayList;

public class VirtualFileSystem {

	private ArrayList<FileDescriptor> filelist = new ArrayList<FileDescriptor>();
	
	public ArrayList<FileDescriptor> getFileList() {
		return filelist;
	}

}

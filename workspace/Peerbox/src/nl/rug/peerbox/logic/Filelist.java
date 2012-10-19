package nl.rug.peerbox.logic;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

public class Filelist extends ConcurrentHashMap<String, PeerboxFile> {

	/**
	 * 
	 */
	private final static int INITIAL_CAPACITY = 1;
	private final static float LOAD_FACTOR = 0.9f;
	private final static int CONCURRENCY_LEVEL = 1;
	private final static long serialVersionUID = 1L;
	private final static Logger logger = Logger.getLogger(Filelist.class);

	private Filelist(Context ctx) {
		super(INITIAL_CAPACITY, LOAD_FACTOR, CONCURRENCY_LEVEL);
	}

	public static Filelist initFilelist(Context ctx) {
		Filelist filelist = new Filelist(ctx);

		return filelist;
	}

	public void serialize(Context ctx) {
		try {
			String path = ctx.getPathToPeerbox();
			OutputStream file = new FileOutputStream(path
					+ ctx.getProperties().getProperty(Property.DATAFILE_NAME));
			OutputStream buffer = new BufferedOutputStream(file);
			try (ObjectOutput output = new ObjectOutputStream(buffer)) {
				output.writeObject(this);
			}
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public Filelist deserialize(Context ctx) {
		Filelist filelist = null;
		try {
			String path = ctx.getPathToPeerbox();
			InputStream file = new FileInputStream(path
					+ ctx.getProperties().getProperty(Property.DATAFILE_NAME));
			InputStream buffer = new BufferedInputStream(file);
			try (ObjectInput input = new ObjectInputStream(buffer)) {
				filelist = (Filelist) input.readObject();
			}
		} catch (ClassNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		return filelist;
	}
}

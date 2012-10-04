package nl.rug.ds;

import java.io.Serializable;

/**
 * Uses a CRC32 checksum check for integrity 
 * 
 * @author cm
 *
 */
public class Message implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int number; //check for integrity (reject duplicates), piggyback
	private int source;
	private Serializable payload;

	public void setPayload(Serializable payload) {
		this.payload = (Serializable)payload;
	}

	public Serializable getPayload() {
		return payload;
	}
	
	public int getNumber() {
		return number;
	}

	public int getSource() {
		return source;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public void setSource(int source) {
		this.source = source;
	}	
}
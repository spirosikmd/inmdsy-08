package nl.rug.peerbox.logic.handler;

public class UnsupportedCommandException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	private Object unsupportedCommand;
	
	public UnsupportedCommandException(Object command) {
		unsupportedCommand = command;
	}
	
	public Object getUnsupportedCommand() {
		return unsupportedCommand;
	}

}

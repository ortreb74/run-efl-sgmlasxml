package sasgml.com.exception;

import sasgml.com.log.LogManager;

public class SASgmlException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SASgmlException(String message) {
		super(message);
	}
	
	public void printMessage() {
		LogManager.writeError(getMessage());
		printStackTrace();
	}
}

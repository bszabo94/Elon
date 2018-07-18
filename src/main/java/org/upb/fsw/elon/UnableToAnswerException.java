package org.upb.fsw.elon;

/**
 * 
 * An exception to hold unique exception for our software
 *
 */
public class UnableToAnswerException extends Exception {
		
	public UnableToAnswerException() {
		super();
	};
	
	public UnableToAnswerException(String message) {
		super(message);
	}

}

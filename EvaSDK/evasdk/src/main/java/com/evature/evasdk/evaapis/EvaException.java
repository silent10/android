package com.evature.evasdk.evaapis;

public class EvaException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public EvaException(String message) {
        super(message);
    }

    public EvaException(String message,  Throwable cause) {
        super(message, cause);
    }
}

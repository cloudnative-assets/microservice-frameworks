package com.ibm.epricer.svclib.email;

@SuppressWarnings("serial")
public class InvalidEmailException extends RuntimeException {
	InvalidEmailException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
	InvalidEmailException(final String message) {
		super(message);
	}
}

package com.ibm.epricer.svclib.email;

@SuppressWarnings("serial")
public class SendMailException extends RuntimeException {
	SendMailException(final String message, final Throwable cause) {
		super(message, cause);
	}
}

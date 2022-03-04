package com.ibm.epricer.svclib.email;

@SuppressWarnings("serial")
public class AttachmentResourceException extends RuntimeException {
	
	AttachmentResourceException(final String message, final Throwable cause) {
		super(message, cause);
	}
}

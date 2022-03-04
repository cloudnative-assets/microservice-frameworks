package com.ibm.epricer.svclib;

/**
 * Wrapper for non-business rule exceptions caused by technical or programmatic errors in remote
 * service business code, for example NPE. Usually developer should not catch the exception and let
 * the system propagate it to the caller. Is there a sensible way to recover from NPE?
 * 
 * @author Kiran Chowdhury
 */

public class UnhandledTechnicalException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UnhandledTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }

}

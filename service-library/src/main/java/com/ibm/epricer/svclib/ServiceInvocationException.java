package com.ibm.epricer.svclib;

/**
 * Unable to connect, corrupted message or incorrect message-to-protocol binding
 */
public class ServiceInvocationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public enum Reason {
        OTHER
    }

    private Reason code;

    public ServiceInvocationException(String message) {
        super(message);
        this.code = Reason.OTHER;
    }

    public ServiceInvocationException(Reason code, String message, Throwable t) {
        super(message, t);
        this.code = code;
    }

    public ServiceInvocationException(String message, Throwable t) {
        super(message, t);
        this.code = Reason.OTHER;
    }

    public Reason getCode() {
        return this.code;
    }
}

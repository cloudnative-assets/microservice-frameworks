package com.ibm.epricer.svclib;

import java.time.Instant;
import java.util.Map;

/**
 * Business rule exception indicates a business rule violation, for example input parameter is not
 * within acceptable range. The client is expected to be able to either recover from such an
 * exception programmatically or at least display a meaningful message to the end user so the
 * request can be re-submitted with a valid input. This is the only exception that can be defined on
 * a service interface end-point.
 * 
 * @author Kiran Chowdhury
 *
 */
public class BusinessRuleException extends Exception {
    private static final long serialVersionUID = 1L;
    private static final String CODE_PATTERN = "^[a-z][a-z0-9-]+[a-z0-9]:[1-9][0-9]{0,3}$";

    private int code;
    private Map<String, String> details;
    private final String timeStamp;
    private boolean upstream = false; // the exception has crossed service boundary

    public BusinessRuleException(int code, String message) {
        super(message);
        this.timeStamp = Instant.now().toString();
        if (code < 1) {
            throw new IllegalArgumentException("Illegal business rule violation code");
        }
        this.code = code;
    }

    public BusinessRuleException setDetail(String key, String data) {
        this.details.put(key, data);
        return this;
    }

    public String getDetail(String key) {
        return this.details.get(key);
    }

    public int getCode() {
        return this.code;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public boolean isUpstream() {
        return upstream;
    }

    public BusinessRuleException(ExceptionState state) {
        super(state.message);
        if (!state.code.matches(CODE_PATTERN)) {
            throw new IllegalArgumentException("Business rule exception code does not match pattern <service-id>:<number>");
        }
        this.code = Integer.valueOf(state.code.split(":")[1]);
        this.details = state.details;
        this.timeStamp = state.timeStamp;
        this.upstream = true;
    }

    public ExceptionState export(String serviceId) {
        ExceptionState exs = new ExceptionState();
        exs.message = this.getMessage();
        exs.code = serviceId + ":" + this.code;
        exs.details = this.details;
        exs.timeStamp = this.timeStamp;
        return exs;
    }

    @Override
    public String toString() {
        return this.code + ", " + super.toString();
    }
}

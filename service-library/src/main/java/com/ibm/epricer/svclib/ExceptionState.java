package com.ibm.epricer.svclib;

import java.time.Instant;
import java.util.Map;

/**
 * Structure representing internal state of a limited number of exception types for the purpose of
 * serializing those exceptions to cross service process boundaries.
 * 
 * @author Kiran Chowdhury
 */

public class ExceptionState {
    public String code; // ex. "epricer-hello:12"
    public Map<String, String> details;
    public String message;
    public String timeStamp = Instant.now().toString(); // default value
}

package com.ibm.epricer.svclib.objectstore;

import java.util.List;

/**
 * Executes object request and returns result
 * 
 * @author Kiran Chowdhury
 */
public interface ObjectRequestExecutor {

    <T extends ObjectEntity> List<T> execute(ObjectRequest<T> request);
    
    Type type ();
    
    enum Type { FILTER, TERMINAL }

}

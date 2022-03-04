package com.ibm.epricer.svclib.objectstore;

import java.util.List;

/**
 * Filters requests and responses of ObjectRequestExecutor. Instances of this type can be put into
 * ObjectRequest execution chain.
 * 
 * @author Kiran Chowdhury
 */

public interface ObjectRequestExecutorFilter {

    <T extends ObjectEntity> List<T> filter(ObjectRequest<T> request, ObjectRequestExecutor executor);

}

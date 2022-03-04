package com.ibm.epricer.svclib.objectstore;

import java.util.List;

/**
 * Adapts ObjectRequestExecutor to ObjectRequestExecutorFilter interface
 *  
 * @author Kiran Chowdhury
 */
class ObjectRequestExecutorAdapter implements ObjectRequestExecutor {

    private ObjectRequestExecutorFilter filter;
    private ObjectRequestExecutor executor; // either next executor adapter or a terminal executor

    ObjectRequestExecutorAdapter(ObjectRequestExecutorFilter filter, ObjectRequestExecutor executor) {
        this.filter = filter;
        this.executor = executor;
    }
    
    @Override
    public <T extends ObjectEntity> List<T> execute(ObjectRequest<T> request) {
        return filter.filter(request, executor);
    }

    @Override
    public Type type() {
        return Type.FILTER;
    }
}

package com.ibm.epricer.svclib.objectstore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Facade to create ObjectQuery instances
 * 
 * @author Kiran Chowdhury
 */
public interface ObjectStore {

    /**
     * Creates ObjectQuery instance
     * 
     * @param type - object entity type to retrieve
     * @return - ObjectQuery instance
     */
    <T extends ObjectEntity> ObjectQuery<T> retrieve(Class<T> type);
}


@Component
class ObjectStoreImpl implements ObjectStore {

    @Autowired
    private ObjectRequestExecutorFactory factory;

    @Override
    public <T extends ObjectEntity> ObjectQuery<T> retrieve(Class<T> type) {
        return new ObjectQuery<T>(type, factory);
    }

}

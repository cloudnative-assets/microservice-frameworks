package com.ibm.epricer.svclib.objectstore;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import com.ibm.epricer.svclib.objectstore.Criteria.Criterion;

/**
 * Persistence-technology-neutral query to retrieve ObjectStoreEntity objects.
 * 
 * Usually internal class of ObjectStore
 * 
 * @author Kiran Chowdhury
 *
 * @param <T> - entity type
 */

public class ObjectQuery<T extends ObjectEntity> implements ObjectRequest<T> {
    private ObjectRequestExecutorFactory factory;

    private final Class<T> type;
    private Criteria criteria;

    ObjectQuery(Class<T> type, ObjectRequestExecutorFactory factory) {
        this.type = type;
        this.factory = factory;
    }

    public ObjectQuery<T> where(Criteria criteria) {
        this.criteria = criteria;
        return this;
    }

    public Optional<T> single() {
        List<T> result = factory.createExecutor(type).execute(this);
        if (result.size() > 1) {
            throw new IllegalStateException("Result set has more than one entity");
        }
        return result.stream().findAny();
    }

    public List<T> list() {
        return factory.createExecutor(type).execute(this);
    }

    @Override
    public Class<T> getEntityType() {
        return type;
    }

    @Override
    public Collection<Criterion> getCriteria() {
        return criteria.criteria();
    }

    @Override
    public String toString() {
        return String.format("Query [%s, %s]", type.getName(), criteria);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(criteria, type.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ObjectQuery<T> other = (ObjectQuery<T>) obj;
        return Objects.equals(criteria, other.criteria) && Objects.equals(type.getName(), other.type.getName());
    }
}

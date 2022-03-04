package com.ibm.epricer.svclib.objectstore;

import java.util.Collection;
import com.ibm.epricer.svclib.objectstore.Criteria.Criterion;

/**
 * Instance of this type represent entity object request in persistence-technology-neutral way that
 * can be passed along to persistence-technology-specific implementations for interpretation and
 * execution.
 * 
 * @author Kiran Chowdhury
 *
 * @param <T> - result entity type
 */
public interface ObjectRequest<T extends ObjectEntity> {

    Class<T> getEntityType();

    Collection<Criterion> getCriteria();

}

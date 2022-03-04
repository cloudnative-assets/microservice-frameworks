package com.ibm.epricer.svclib.objectstore;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Add this filter into an object request processing chain to enable results caching
 * 
 * @author Kiran Chowdhury
 */

@Component("CachingFilter")
class CachingFilter implements ObjectRequestExecutorFilter {
    private static final Logger LOG = LoggerFactory.getLogger(CachingFilter.class);

    @Override
    @Cacheable(cacheNames = "object-store", key = "#request")
    public <T extends ObjectEntity> List<T> filter(ObjectRequest<T> request, ObjectRequestExecutor executor) {

        LOG.debug("Cache missed, calling executor with request {}", request);

        List<T> result = executor.execute(request);

        LOG.debug("Cached response with {} record(s)", result.size());

        return result;
    }

}

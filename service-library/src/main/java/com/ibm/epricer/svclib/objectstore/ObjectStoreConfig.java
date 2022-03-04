package com.ibm.epricer.svclib.objectstore;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;

@Configuration
@EnableCaching
@PropertySource("classpath:object-store.properties") // default object store properties
public class ObjectStoreConfig {
    private static final Logger LOG = LoggerFactory.getLogger(ObjectStoreConfig.class);

    @Bean
    public Caffeine<Object, Object> caffeineConfig(@Value("${epricer.object-store.cache-ttl}") int ttl) {
        return Caffeine.newBuilder()
                .recordStats()
                .expireAfterWrite(ttl, TimeUnit.SECONDS)
                .removalListener((Object key, Object value, RemovalCause cause) 
                        -> LOG.trace("Cache key {} was removed ({})", key, cause));
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(caffeine);
        return caffeineCacheManager;
    }
}

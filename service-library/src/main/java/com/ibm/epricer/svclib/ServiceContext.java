package com.ibm.epricer.svclib;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Transport-independent service context. 
 * 
 * @author Kiran Chowdhury
 */

public interface ServiceContext {
    /**
     * RPC implementations must call this method on receiving requests
     * 
     * @param inputHeaders - all headers from the incoming HTTP request
     */
    void processRequestHeaders(Map<String, String> inputHeaders);
    
    /**
     * RPC implementations must call this method on sending responses
     * 
     * @param responseHeaders - all headers from the outgoing HTTP response
     */
    void processResponseHeaders(Map<String, String> responseHeaders);
    
    /**
     * Returns headers saved for propagation
     */
    Map<String, String> getPropagatingHeaders();
}

@Component
class ServiceContextImpl implements ServiceContext {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceContext.class);
    private static final String CONFIG_PROP = "${epricer.propagating-header-prefixes}";
    private static final String DELIMITER = ",";
    private static final String TRACE_ID_HEADER = "epricer-trace-id";
    private static final String TRACE_ID = "TRACEID";
    private static final String SPAN_ID = "SPANID";
    private static final int MIN_PREFIX_LEN = 2;

    private final List<String> prefixList = new LinkedList<>();

    private static final ThreadLocal<Map<String, String>> HEADERS = new ThreadLocal<>() {
        @Override
        protected Map<String, String> initialValue() {
            return new HashMap<>();
        }
    };

    public ServiceContextImpl(@Value(CONFIG_PROP) String headerPrefixes) {
        String[] prefixes = headerPrefixes.split(DELIMITER);
        for (String prefix : prefixes) {
            if (!prefix.strip().isEmpty()) {
                if (prefix.strip().length() < MIN_PREFIX_LEN) {
                    throw new IllegalStateException("Propagaing header prefix is too short: '" + prefix + "'");
                }
                prefixList.add(prefix.strip());
            }
        }
        if (prefixList.size() > 0) {
            LOG.info("HTTP headers with names starting from {} will be propagated", prefixList);
        }
    }

    /**
     * Look through the input map of headers and save those that need to be propagated. Any previously
     * saved headers will be deleted.
     * 
     * @param inputHeaders - all headers from the incoming HTTP request
     */
    @Override
    public void processRequestHeaders(Map<String, String> inputHeaders) {
        HEADERS.remove();
        for (Entry<String, String> header : inputHeaders.entrySet()) {
            for (String prefix : this.prefixList) {
                // Save only those that start with one of the prefixes
                if (header.getKey().toLowerCase().startsWith(prefix.toLowerCase())) {
                    HEADERS.get().put(header.getKey(), header.getValue());
                    LOG.trace("Saved propagating HTTP header: {}", header);
                    break;
                }
            }
        }
        /*
         * Setting logging MDC
         */
        MDC.put(TRACE_ID, inputHeaders.get(TRACE_ID_HEADER));
        MDC.put(SPAN_ID, UUID.randomUUID().toString().substring(26));
    }

    /**
     * Clear MDC on returning response
     * 
     * @param responseHeaders - all headers from the outgoing HTTP response
     */
    @Override
    public void processResponseHeaders(Map<String, String> responseHeaders) {
        MDC.clear();
    }

    @Override
    public Map<String, String> getPropagatingHeaders() {
        return HEADERS.get();
    }
}

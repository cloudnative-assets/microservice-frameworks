package com.ibm.epricer.svclib.objectstore;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Creates chains of executors of ObjectRequest instances. The last executor in the chain must be of
 * type ObjectRequestExecutor, all others must be ObjectRequestExecutorFilter.
 * 
 * @author Kiran Chowdhury
 */
public interface ObjectRequestExecutorFactory {

    <T extends ObjectEntity> ObjectRequestExecutor createExecutor(Class<T> type);

}


@Component
class EpricerObjectRequestExecutorFactory implements ObjectRequestExecutorFactory {
    private static final Logger LOG = LoggerFactory.getLogger(EpricerObjectRequestExecutorFactory.class);
    private static final String PROP_PREFIX = "epricer.object-store.";
    
    @Autowired
    private ApplicationContext context;
    
    @Autowired
    Environment env;
    
    @Override
    public <T extends ObjectEntity> ObjectRequestExecutor createExecutor(Class<T> type) {
        String chain = env.getProperty(PROP_PREFIX + type.getName());
        if (StringUtils.isNotBlank(chain)) {
            return buildExecutionChain(chain);
        } else {
            return buildExecutionChain(env.getProperty(PROP_PREFIX + "default-execution-chain"));
        }
    }

    private ObjectRequestExecutor buildExecutionChain(String beanNames) {
        Deque<String> deque = new LinkedList<>(Arrays.asList(beanNames.split(",")));
        String executorName = deque.removeLast().strip(); // the last one must be an executor
        ObjectRequestExecutor executor = context.getBean(executorName, ObjectRequestExecutor.class);
        String chain = executorName;
        while(deque.peekLast() != null) {
            String filterName = deque.removeLast().strip(); // all others are filters
            chain = filterName + "->" + chain;
            ObjectRequestExecutorFilter filter = context.getBean(filterName, ObjectRequestExecutorFilter.class);
            executor = new ObjectRequestExecutorAdapter(filter, executor);
        }
        LOG.debug("Object request execution chain created: {}", chain);
        return executor;
    }
    
}

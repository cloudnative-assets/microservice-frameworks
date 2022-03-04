package com.ibm.epricer.svclib.rpc;

import static com.ibm.epricer.svclib.ServiceMessage.Status.SUCCESS;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.epricer.svclib.BusinessRuleException;
import com.ibm.epricer.svclib.ServiceMessage;
import com.ibm.epricer.svclib.data.DatabaseSupportChecker;
import com.ibm.epricer.svclib.data.EpricerTransaction;

/**
 * Invokes service end-point in a transaction.
 * 
 * @author Kiran Chowdhury
 */

@Component
class RpcServiceEndpointInvoker {
    private static final Logger LOG = LoggerFactory.getLogger(RpcServiceEndpointInvoker.class);

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ObjectMapper mapper;

    @EpricerTransaction
    ServiceMessage callEndpoint(RpcServiceEndpointMetadata endPoint, ServiceMessage reqMsg)
            throws BusinessRuleException {

        checkTransactionIsActive();

        LOG.trace("Incoming RPC message: {}", reqMsg);
        Object[] input = unmarshallRequest(endPoint, reqMsg);

        LOG.info("Invoking service endpoint {}", endPoint);
        Object bean = context.getBean(endPoint.type);
        Instant start = Instant.now();
        Object result = call(bean, endPoint.method, input);
        long timeElapsed = Duration.between(start, Instant.now()).toMillis();
        LOG.info("Service endpoint {} finished in {} ms", endPoint, timeElapsed);
        
        ServiceMessage respMsg = new ServiceMessage();
        respMsg.setThrows(endPoint.canThrow);
        respMsg.setStatus(SUCCESS);
        String response = marshallResponse(result);
        respMsg.setPayload(response);
        LOG.trace("Outgoing RPC message: {}", respMsg);

        return respMsg;
    }

    private Object call(Object bean, Method method, Object[] input) throws BusinessRuleException {
        try {

            return method.invoke(bean, input);

        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new IllegalStateException("Failed to invoke service endpoint", e);
        } catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            if (target instanceof BusinessRuleException) {
                throw (BusinessRuleException) target;
            } else if (target instanceof RuntimeException) {
                throw (RuntimeException) target;
            } else {
                throw new IllegalStateException("Failed to invoke service endpoint due to an error", target);
            }
        }
    }

    private Object[] unmarshallRequest(RpcServiceEndpointMetadata metaData, ServiceMessage reqMsg) {
        Object[] input;
        if (metaData.method.getParameterCount() > 0) { // no more than one parameter is expected
            Class<?> paramType = metaData.method.getParameterTypes()[0];
            input = new Object[1];
            if (reqMsg.getPayload() == null) {
                throw new IllegalStateException(
                        "Service endpoint '" + metaData.endpointId + "' input parameter value must not be null");
            } else {
                try {
                    input[0] = mapper.readValue(reqMsg.getPayload(), paramType);
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException("Failed to un-marshall service parameter from JSON", e);
                }
            }
        } else {
            input = new Object[0];
        }
        return input;
    }

    private String marshallResponse(Object result) {
        String response = null;
        try {
            response = mapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to marshall service response into JSON", e);
        }
        return response;
    }

    private static void checkTransactionIsActive() {
        if (DatabaseSupportChecker.isDatabaseEnabled() && (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionAspectSupport.currentTransactionStatus().isNewTransaction())) {
            throw new IllegalStateException("Service invocation is not top-level transactional.");
        }
    }

}

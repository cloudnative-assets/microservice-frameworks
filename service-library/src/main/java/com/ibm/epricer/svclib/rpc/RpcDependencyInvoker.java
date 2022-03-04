package com.ibm.epricer.svclib.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.epricer.svclib.BusinessRuleException;
import com.ibm.epricer.svclib.ExceptionState;
import com.ibm.epricer.svclib.ServiceMessage;
import com.ibm.epricer.svclib.ServiceMessage.Status;

/**
 * Processes and forwards calls to dependency end-points to communication protocol implementations.
 * This bean is stateful and must be prototype-scoped.
 * 
 * @author Kiran Chowdhury
 *
 */
class RpcDependencyInvoker implements InvocationHandler {
    @Configuration
    static class RpcDependencyInvokerFactory {
        @Bean
        @Scope("prototype")
        RpcDependencyInvoker rpcDependencyInvoker(String serviceId) {
            return new RpcDependencyInvoker(serviceId);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(RpcDependencyInvoker.class);
    private static final Class<RpcServiceEndpoint> EPCLASS = RpcServiceEndpoint.class;

    private final String serviceId;

    @Autowired
    private RpcDependencyController controller;

    @Autowired
    private ObjectMapper mapper;

    private RpcDependencyInvoker(String serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcServiceEndpoint ann = method.getAnnotation(EPCLASS);
        if (ann == null) {
            return method.invoke(this, args); // either hashCode or equals
        }
        
        String endpointId = ann.endpointId();
        int endpointVer = ann.endpointVer();
        String endPoint = serviceId + "/" + endpointId + ":" + endpointVer;

        ServiceMessage reqMsg = new ServiceMessage();
        reqMsg.setServiceInfo(serviceId, endpointId, endpointVer);
        if (args != null) {
            Object param = Objects.requireNonNull(args[0],
                    "Service endpoint '" + endPoint + "' parameter value must not be null");
            reqMsg.setPayload(marshallRequest(param));
        }

        LOG.info("Calling remote service endpoint '{}'", endPoint);
        LOG.trace("Request RPC message: {}", reqMsg);
        
        Instant start = Instant.now();
        ServiceMessage respMsg = controller.call(reqMsg);
        long timeElapsed = Duration.between(start, Instant.now()).toMillis();
        
        LOG.trace("Response RPC message: {}", respMsg);
        LOG.info("Call to remote service endpoint '{}' finished in {} ms", endPoint, timeElapsed);
        
        respMsg.getThrows().ifPresent(remoteThrows -> validateThrows(remoteThrows, method));

        Status status = respMsg.getStatus()
                .orElseThrow(() -> new IllegalStateException("Invalid service message, status header is missing"));

        /*
         * Only messages with valid status header will reach this point
         */
        switch (status) {
            case SUCCESS: {
                LOG.info("Remote service endpoint '{}' returned successful result", endPoint);
                return unmarshallResponse(respMsg, method.getReturnType());
            }
            case BUSINESS_EXCEPTION: {
                LOG.error("Remote service endpoint '{}' returned business rule violation", endPoint);
                throw new BusinessRuleException(unmarshallResponse(respMsg, ExceptionState.class));
            }
            default:
                throw new IllegalArgumentException("Unexpected value: " + status);
        }
    }

    private String marshallRequest(Object input) {
        String payload = "";
        try {
            payload = mapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to marshall service parameter into JSON", e);
        }
        return payload;
    }

    private <T> T unmarshallResponse(ServiceMessage svcMsg, Class<T> clazz) {
        T response;
        try {
            response = mapper.readValue(svcMsg.getPayload(), clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to un-marshall service response from JSON", e);
        }
        return response;
    }

    private void validateThrows(boolean remoteThrows, Method method) {
        boolean localThrows = method.getExceptionTypes().length > 0;
        /*
         * If the remote end-point is capable of throwing business exceptions then the local proxy interface
         * must have throws clause.
         */
        if (remoteThrows && !localThrows) {
            throw new IllegalStateException("Contract mismatch: interface method " + method.getName() + " must throw "
                    + BusinessRuleException.class.getSimpleName());
        }
        /*
         * If the remote end-point never throws business exceptions then the local proxy interface should
         * not do it either.
         */
        if (!remoteThrows && localThrows) {
            throw new IllegalStateException("Contract mismatch: interface method " + method.getName()
                    + " should not throw " + BusinessRuleException.class.getSimpleName());
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RpcDependencyInvoker other = (RpcDependencyInvoker) obj;
        return Objects.equals(serviceId, other.serviceId);
    }
    
}

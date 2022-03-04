package com.ibm.epricer.svclib.rpc.http;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import com.ibm.epricer.svclib.ServiceContext;

/**
 * Intercepts every outgoing HTTP request to insert propagating headers.
 * 
 * @author Kiran Chowdhury
 */

@Component
class HttpOutgoingRequestInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(HttpOutgoingRequestInterceptor.class);

    @Autowired
    private ServiceContext context;
    
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        HttpHeaders headers = request.getHeaders();
        LOG.trace("Intercepted outgoing RPC request with headers: {}", headers);

        headers.setAll(context.getPropagatingHeaders());
        
        LOG.trace("Request headers with propagation: {}", headers);

        return execution.execute(request, body);
    }
}

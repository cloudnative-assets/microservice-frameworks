package com.ibm.epricer.svclib.rpc.http;

import static com.ibm.epricer.svclib.rpc.http.HttpConstants.UTE_MEDIA_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import com.ibm.epricer.svclib.RemoteUnhandledTechnicalException;
import com.ibm.epricer.svclib.ServiceInvocationException;
import com.ibm.epricer.svclib.ServiceMessage;
import com.ibm.epricer.svclib.UnhandledTechnicalException;
import com.ibm.epricer.svclib.rpc.RpcDependencyController;
import com.ibm.epricer.svclib.rpc.ServiceUriResolver;

/**
 * HTTP communication protocol implementation using Spring REST facility. Receives messages from the
 * Spring servlet and maps HTTP request/response message structures to ServiceMessage structure.
 * 
 * Null payloads are accepted by POST so void parameters and return types will map correctly.
 * 
 * Business rule exceptions must be returned with HTTP 200 and an error payload.
 * 
 * ePricer Java Unhandled Technical Exception should be returned back serialized in the HTTP message
 * body using content-type=application/epricer-exception with HTTP 500 status.
 * 
 * @author Kiran Chowdhury
 */

@Service
class RpcDependencyOverHttpController implements RpcDependencyController {
    private static final Logger LOG = LoggerFactory.getLogger(RpcDependencyOverHttpController.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ServiceUriResolver resolver;

    @Override
    public ServiceMessage call(ServiceMessage reqMsg) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAll(reqMsg.getHeaders());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> reqEntity = new HttpEntity<>(reqMsg.getPayload(), headers);

        String uri = resolver.discoverUri(reqMsg.getServiceId());

        LOG.info("Sending HTTP request to {}", uri);
        ResponseEntity<String> respEntity = callRemoteService(uri, reqEntity);

        validateStatus(respEntity);
        
        ServiceMessage respMsg = new ServiceMessage();
        respMsg.setPayload(respEntity.getBody());
        respMsg.importResponseHeaders(respEntity.getHeaders().toSingleValueMap());

        return respMsg;
    }

    private ResponseEntity<String> callRemoteService(String uri, HttpEntity<String> reqEntity) {
        try {

            return restTemplate.postForEntity(uri, reqEntity, String.class);

        } catch (HttpServerErrorException e) {
            // Un-handled technical exception in the remote service
            throw convertHttp5xxException(e);
        } catch (Throwable e) {
            // All other problems (ResourceAccessException, RestClientException, etc)
            throw new ServiceInvocationException("Failed to invoke remote service", e);
        }
    }

    private void validateStatus(ResponseEntity<String> respEntity) {
        int httpStatusCode = respEntity.getStatusCodeValue();
        if (httpStatusCode != 200) { // 201 is not allowed
            throw new ServiceInvocationException("Invalid remote service message, unexpected HTTP status code " + httpStatusCode);
        }
        if (respEntity.getBody() == null) { // 201 is not allowed
            throw new ServiceInvocationException("Invalid remote service message, blank response is not expected");
        }
    }

    private UnhandledTechnicalException convertHttp5xxException(HttpServerErrorException remoteException) {
        if (INTERNAL_SERVER_ERROR.equals(remoteException.getStatusCode())
                && UTE_MEDIA_TYPE.equals(remoteException.getResponseHeaders().getContentType())) {
            String errorMessageBody = remoteException.getResponseBodyAsString();
            try {
                Throwable cause = RemoteUnhandledTechnicalException.deserialize(errorMessageBody);
                return new UnhandledTechnicalException(cause.getMessage(), cause);
            } catch (Exception e) {
                LOG.error("Failed to de-serialize remote exception stack trace", e);
            }
        }

        // Non-Java services may return generic 500 status code responses
        return new UnhandledTechnicalException("Remote service invocation failed", remoteException);
    }
}

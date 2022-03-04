package com.ibm.epricer.svclib.rpc.http;

import static com.ibm.epricer.svclib.rpc.http.HttpConstants.UTE_MEDIA_TYPE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ibm.epricer.svclib.RemoteUnhandledTechnicalException;
import com.ibm.epricer.svclib.ServiceMessage;
import com.ibm.epricer.svclib.rpc.RpcServiceInvoker;

/**
 * HTTP communication protocol implementation using Spring REST facility. Maps request/response
 * message structures to HTTP artifacts and passes control to the service invoker.
 * 
 * Request Content-Type header is ignored. Success response content is JSON with HTTP status 200.
 * 
 * The controller may return other HTTP codes, like 5xx and 4xx and they ALL must be processed as
 * errors on the client side.
 * 
 * Java un-handled exception traces are returned back as media type "application/epricer-exception"
 * with HTTP 500 status code.
 * 
 * @author Kiran Chowdhury
 */

@RestController
@RequestMapping("/rpc")
class RpcServiceOverHttpController {
    private static final Logger LOG = LoggerFactory.getLogger(RpcServiceOverHttpController.class);

    @Autowired
    private RpcServiceInvoker invoker;

    @Value("${epricer.service-id}")
    private String serviceId;

    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<String> post(@RequestBody(required = false) String payload,
            @RequestHeader Map<String, String> allHeaders) { // or HttpHeaders 

        ServiceMessage svcMsg = new ServiceMessage();
        svcMsg.setPayload(payload);
        svcMsg.getHeaders().putAll(allHeaders); // shall we pass only the well-known headers?

        ServiceMessage respMsg = invoker.invoke(svcMsg); // response is optional

        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setAll(respMsg.getHeaders());

        if (isBlank(respMsg.getPayload())) {
            /*
             * Serialization of output objects never results in empty or null strings. Null is serialized as
             * "null", empty string becomes """". This means HttpStatus.NO_CONTENT (201) will never be returned.
             */
            throw new IllegalStateException("Blank responses are not allowed");
        }

        return ResponseEntity.status(HttpStatus.OK).headers(respHeaders).body(respMsg.getPayload());
    }

    /*
     * Replacing default BasicErrorController with this custom handler. Serialized ePricer UTEs are
     * passed back in the HTTP message body using application/epricer-exception content-type
     */
    @ExceptionHandler
    ResponseEntity<String> handleException(Throwable exception) {
        LOG.error("Unhandled exception", exception);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).contentType(UTE_MEDIA_TYPE) // UTE Java Stack Trace
                .body(RemoteUnhandledTechnicalException.serialize(exception, serviceId));
    }
}

package com.ibm.epricer.svclib;

import static com.ibm.epricer.svclib.rpc.http.HttpConstants.UTE_MEDIA_TYPE;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility methods to make tests less cluttered and more maintainable
 *  
 * @author Michael
 */

@Component
public class TestUtil {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;
    
    @Value("${epricer.gateway}")
    private String gatewayUrl;

    @Value("${epricer.service-id}")
    private String serviceId;
    
    /**
     * Call SUT service end-point
     * 
     * @param endpointId - end-point id to call
     * @param endpointVer - version
     * @param input - service call input object
     * @return - mock request builder to pass to mock mvc
     */
    public MockHttpServletRequestBuilder callSutEndpoint(String endpointId, Integer endpointVer, Object input) {
        return MockMvcRequestBuilders.post("/rpc")
                        .header(ServiceMessage.SVCID_HEADER, serviceId)
                        .header(ServiceMessage.EPID_HEADER, endpointId)
                        .header(ServiceMessage.EPVER_HEADER, endpointVer)
                        .content(marshallResultToJson(input));
    }

    /*
     * Same as above but with default version 1
     */
    public MockHttpServletRequestBuilder callSutEndpoint(String endpointId, Object input) {
        return callSutEndpoint(endpointId, 1, input);
    }
    
    /**
     * Build mock remote server returning single successful response
     * 
     * @param remoteServiceId - service id
     * @param remoteEndpointId - end-point id
     * @param canThrow - end-point declares BusinessRuleException
     * @param response - mock server object response
     * @return - mock server
     */
    public MockRestServiceServer oneSuccessResponse(String remoteServiceId, String remoteEndpointId, Boolean canThrow, Object response) {
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.add(ServiceMessage.STATUS_HEADER, "0");
        if (canThrow != null) {
            respHeaders.add(ServiceMessage.THROWS_HEADER, Boolean.toString(canThrow));    
        }

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect(ExpectedCount.once(), requestTo(gatewayUrl))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(ServiceMessage.SVCID_HEADER, is(remoteServiceId)))
                .andExpect(header(ServiceMessage.EPID_HEADER, is(remoteEndpointId)))
                .andRespond(withSuccess()
                        .headers(respHeaders)
                        .body(marshallResultToJson(response)));
        
        return mockServer;
    }

    /*
     * Same as above but no canThrow header
     */
    public MockRestServiceServer oneSuccessResponse(String remoteServiceId, String remoteEndpointId, Object response) {
        return oneSuccessResponse(remoteServiceId, remoteEndpointId, null, response);
    }
    
    /**
     * Build mock remote server that throws BusinessRuleException
     * 
     * @param remoteServiceId - service id
     * @param remoteEndpointId - end-point id
     * @param errorCode - business rule exception error code 
     * @return - mock server
     */
    public MockRestServiceServer oneBusinessRuleExceptionResponse(String remoteServiceId, String remoteEndpointId, int errorCode) {
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.add(ServiceMessage.STATUS_HEADER, "1");
        respHeaders.add(ServiceMessage.THROWS_HEADER, Boolean.toString(true));

        String response = marshallResultToJson(new BusinessRuleException(errorCode, "Business rule exception").export(remoteServiceId));
        
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect(ExpectedCount.once(), requestTo(gatewayUrl))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(ServiceMessage.SVCID_HEADER, is(remoteServiceId)))
                .andExpect(header(ServiceMessage.EPID_HEADER, is(remoteEndpointId)))
                .andRespond(withSuccess().headers(respHeaders).body(response));
        
        return mockServer;
    }
    
    /**
     * Build mock remote server that throws unhandled technical exception
     * 
     * @param remoteServiceId - service id
     * @param remoteEndpointId - endpoint id
     * @return - mock server
     */
    public MockRestServiceServer oneUnhandledExceptionResponse(String remoteServiceId, String remoteEndpointId) {
        HttpHeaders respHeaders = new HttpHeaders();
        
        String response = "@" + remoteServiceId + " Oops\nMyType|myMethod|myfile.java|12";
        
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect(ExpectedCount.once(), requestTo(gatewayUrl))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(ServiceMessage.SVCID_HEADER, is(remoteServiceId)))
                .andExpect(header(ServiceMessage.EPID_HEADER, is(remoteEndpointId)))
                .andRespond(withServerError()
                        .headers(respHeaders)
                        .contentType(UTE_MEDIA_TYPE)
                        .body(response));
        
        return mockServer;
    }
    
    /**
     * Marshall object to JSON
     * 
     * @param result - input
     * @return - JSON representation of the input
     */
    public String marshallResultToJson(Object result) {
        String response = null;
        try {
            response = mapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to marshall service response into JSON", e);
        }
        return response;
    }
    
    /**
     * Read resource file
     * 
     * @param path - relative or absolute path of the resource file
     * @return - file content as a String
     */
    public String readStringResource(String path) {
        InputStream stream = this.getClass().getResourceAsStream(path);
        return new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n"));
    }
}

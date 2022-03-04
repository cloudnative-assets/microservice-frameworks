package com.ibm.epricer.svclib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.client.MockRestServiceServer;
import com.ibm.epricer.svclib.test.HelloInput;
import com.ibm.epricer.svclib.test.HelloResult;
import com.ibm.epricer.svclib.test.HelloService;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
public class RpcDependencyTests {

    @Autowired
    private TestUtil util;
    
    @Autowired
    HelloService hello;

    @Test
    @DisplayName("Absolutely sunny day scenario")
    void greetSingleOk() throws BusinessRuleException {
        String name = "Boss";
        String mockGreeting = "Hello, Boss";
        
        HelloResult mockResponse = new HelloResult();
        mockResponse.setGreeting(mockGreeting);

        MockRestServiceServer mockServer = util.oneSuccessResponse("hello", "greet-one", true, mockResponse);

        HelloInput request = new HelloInput();
        request.setName(name);
        
        String actualResponse = hello.greetSingleOne(request).getGreeting();

        mockServer.verify();        
        assertEquals(mockGreeting, actualResponse);
    }

    @Test
    @DisplayName("Sunny day scenario with empty String result")
    void greetWorldOk() {
        String mockResponse = "";
        
        MockRestServiceServer mockServer = util.oneSuccessResponse("hello", "greet-all", false, mockResponse);

        String actualResponse = hello.greetWholeWorld(); 
        
        mockServer.verify();
        assertEquals(mockResponse, actualResponse);
    }

    @Test
    @DisplayName("No input parameters and void response")
    void simplestTest() {
        MockRestServiceServer mockServer = util.oneSuccessResponse("hello", "test", false, null);

        hello.test(); 
        
        mockServer.verify();
    }

    @Test
    @DisplayName("Business rule exception must be propagated back to the client")
    void businessRuleBroken() {
        HelloInput input = new HelloInput();
        input.setName("Boss"); 

        MockRestServiceServer mockServer = util.oneBusinessRuleExceptionResponse("hello", "greet-one", 1);

        assertThrows(BusinessRuleException.class, () -> hello.greetSingleOne(input).getGreeting());
        mockServer.verify();
    }

    @Test
    @DisplayName("HTTP 500 results in an nnhandled technical exception with remote stack trace")
    void unhandledTechnicalException() {
        MockRestServiceServer mockServer = util.oneUnhandledExceptionResponse("hello", "greet-one");

        HelloInput input = new HelloInput();

        Exception exception = assertThrows(UnhandledTechnicalException.class, () -> hello.greetSingleOne(input).getGreeting());
        assertEquals("@hello Oops", exception.getMessage());
        assertEquals("MyType", exception.getCause().getStackTrace()[0].getClassName());

        mockServer.verify();
    }
}

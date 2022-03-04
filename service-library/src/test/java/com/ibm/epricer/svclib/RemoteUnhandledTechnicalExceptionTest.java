package com.ibm.epricer.svclib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class RemoteUnhandledTechnicalExceptionTest {
    String twoLineErrorMsg = "First\nSecond";
    String serviceId = "epricer-hello";
    String serializedTwoLineErrorMsg = "@epricer-hello First\nSecond";
    String firstResponseBodyLine = "%40epricer-hello+First%0ASecond";

    @Test
    void testSerialization() {
        Throwable exception = new IllegalStateException(twoLineErrorMsg);
        String errorBody = RemoteUnhandledTechnicalException.serialize(exception, serviceId);

        assertTrue(errorBody.startsWith(firstResponseBodyLine));
        String secondBodyLine = errorBody.split("\n")[1];
        assertTrue(secondBodyLine.startsWith(this.getClass().getName()));
    }

    @Test
    void testCompliantErrorResponse() {
        String errorResponseBody = firstResponseBodyLine + "\n" + "MyType|myMethod|myfile.java|12";

        RemoteUnhandledTechnicalException exception = RemoteUnhandledTechnicalException.deserialize(errorResponseBody);

        assertEquals(serializedTwoLineErrorMsg, exception.getMessage());
        assertEquals("MyType", exception.getStackTrace()[0].getClassName());
    }

    @Test
    void testCorruptedErrorResponse() {
        String errorResponseBody = "explosion\n" + "MyType|..."; // corrupt stack trace

        assertThrows(Exception.class, () -> {
            RemoteUnhandledTechnicalException.deserialize(errorResponseBody);
        });
    }
}

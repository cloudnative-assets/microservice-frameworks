package com.ibm.epricer.svclib.rpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class RpcServiceEndpointMapTest {

    @Test
    void testDiscover() {
        RpcServiceEndpointMap map = new RpcServiceEndpointMap();
        map.discover();

        RpcServiceEndpointMetadata greeting = map.getEndpointInfo("greeting", 1);
        assertTrue(greeting.canThrow, "Wrong throws exception flag");
        assertEquals("greeting", greeting.endpointId, "Wrong endpoint id");
        
        RpcServiceEndpointMetadata test = map.getEndpointInfo("test", 2);
        assertFalse(test.canThrow, "Wrong throws exception flag");
        assertEquals("test", test.endpointId, "Wrong endpoint id");

    }
}

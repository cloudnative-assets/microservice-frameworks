package com.ibm.epricer.svclib.rpc;

/**
 * Transforms service ID to the real service URL using apropriate service
 * discovery and load balancing mechanism.
 * 
 */
public interface ServiceUriResolver {
	String discoverUri(String serviceId);
}

package com.ibm.epricer.svclib.rpc.http;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.ibm.epricer.svclib.rpc.ServiceUriResolver;

/**
 * Resolves service id to real service URI
 * 
 * @author Michael
 */

@Component
@ConfigurationProperties(prefix = "epricer")
class StandardServiceUriResolver implements ServiceUriResolver {
    private Map<String, String> rpcDependencyLocation;
    private String gateway;
    
    /*
     * There are three ways to discover the service uri.
     * 1. If the property epricer.rpcDependencyLocation.<service-id> is defined then the destination service will be called directly.
     * 2. If the property rpcDependencyLocation is not defined and epricer.gatway is defined, then the destination service will called via gateway (service1 -> gateway -> service2)
     * 3. If neither rpcDependencyLocation nor gateway are defined, then it will assume that service mesh is available and the destination service will be called via sidecar envoy proxy container.
     */
    @Override
    public String discoverUri(String serviceId) {
        /*
         * First check if epricer.rpc-dependency-location.<service-id> exists. When configuration is passed
         * via OS environment variables dashes "-" are removed.
         */
        if (rpcDependencyLocation != null) {
            String location = defaultIfBlank(rpcDependencyLocation.get(serviceId),
                    rpcDependencyLocation.get(serviceId.replace("-", "")));
            if (isNotBlank(location)) {
                return location; // service URI is explicitly specified in the configuration
            }
        }

        /*
         * Return service gateway URI
         */
        if (!isBlank(gateway)) {
        	return gateway;
        }
        // Istio Enabled - Internal service to service invocation via envoy proxy.
        return "http://" + serviceId + ":80/rpc";
        
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public Map<String, String> getRpcDependencyLocation() {
        return rpcDependencyLocation;
    }

    public void setRpcDependencyLocation(Map<String, String> rpcDependencyLocation) {
        this.rpcDependencyLocation = rpcDependencyLocation;
    }
}

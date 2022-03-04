package com.ibm.epricer.svclib.rpc;

import com.ibm.epricer.svclib.ServiceMessage;

/**
 * Communication mechanism to call remote services
 */

public interface RpcDependencyController {

    ServiceMessage call(ServiceMessage msg);

}

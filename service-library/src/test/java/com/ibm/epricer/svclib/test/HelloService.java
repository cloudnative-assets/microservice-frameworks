package com.ibm.epricer.svclib.test;

import com.ibm.epricer.svclib.BusinessRuleException;
import com.ibm.epricer.svclib.rpc.RpcDependency;
import com.ibm.epricer.svclib.rpc.RpcServiceEndpoint;

@RpcDependency("hello")
public interface HelloService {

    @RpcServiceEndpoint(endpointId = "greet-one")
    HelloResult greetSingleOne(HelloInput input) throws BusinessRuleException;

    @RpcServiceEndpoint(endpointId = "greet-all")
    String greetWholeWorld();

    @RpcServiceEndpoint(endpointId = "test")
    void test();
}

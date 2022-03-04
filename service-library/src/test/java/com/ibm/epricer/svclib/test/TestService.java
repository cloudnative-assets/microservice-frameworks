package com.ibm.epricer.svclib.test;

import com.ibm.epricer.svclib.BusinessRuleException;
import com.ibm.epricer.svclib.rpc.RpcService;
import com.ibm.epricer.svclib.rpc.RpcServiceEndpoint;

@RpcService
public interface TestService {

    @RpcServiceEndpoint(endpointId = "greeting")
    String greet(String name) throws BusinessRuleException;

    @RpcServiceEndpoint(endpointId = "test", endpointVer = 2)
    void test();

    @RpcServiceEndpoint(endpointId = "user-change-lastname")
    void userChangeLastname(UserDataInput input) throws BusinessRuleException;
    
}

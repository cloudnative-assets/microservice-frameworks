package com.ibm.epricer.svclib.rpc;

import java.lang.reflect.Method;

class RpcServiceEndpointMetadata {

    RpcServiceEndpointMetadata(Class<?> clazz, Method method) {
        this.type = clazz;
        this.method = method;
        this.canThrow = false;
    }

    public String endpointId;
    public int endpointVer;
    public Class<?> type;
    public Method method;
    public boolean canThrow;

    @Override
    public String toString() {
        return endpointId + ":" + endpointVer + " -> " + type.getSimpleName() + "." + method.getName();
    }
}

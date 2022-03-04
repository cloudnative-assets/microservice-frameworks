package com.ibm.epricer.svclib.rpc;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(METHOD)
public @interface RpcServiceEndpoint {

    /**
     * Service end-point ID
     */
    String endpointId();

    /**
     * Service end-point version
     */
    int endpointVer() default 1;

}

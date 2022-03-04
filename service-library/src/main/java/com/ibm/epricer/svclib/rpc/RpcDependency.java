package com.ibm.epricer.svclib.rpc;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotated interface represents remote service contract.
 */

@Retention(RUNTIME)
@Target(TYPE)
public @interface RpcDependency {

    /**
     * Service ID
     */
    String value();

}

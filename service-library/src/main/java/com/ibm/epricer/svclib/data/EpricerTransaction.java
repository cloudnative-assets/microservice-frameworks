package com.ibm.epricer.svclib.data;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotated bean methods will be wrapped into the epricer composite transaction.
 * 
 * @author Kiran Chowdhury
 */

@Retention(RUNTIME)
@Target(METHOD)
public @interface EpricerTransaction {

}

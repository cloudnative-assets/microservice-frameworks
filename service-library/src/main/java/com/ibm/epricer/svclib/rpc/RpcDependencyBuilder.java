package com.ibm.epricer.svclib.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.ClassUtils;
import com.ibm.epricer.EpricerServiceApplication;
import com.ibm.epricer.svclib.BusinessRuleException;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

/**
 * Discovers dependency interfaces, creates and registers dynamic proxies for them as Spring beans.
 * This class must be configured in META-INF/spring.factories.
 * 
 * @author Kiran Chowdhury
 */

public class RpcDependencyBuilder implements ApplicationContextInitializer<GenericApplicationContext> {
    private static final Logger LOG = LoggerFactory.getLogger(RpcDependencyBuilder.class);

    private static final Class<RpcDependency> RPCDEPCLASS = RpcDependency.class;
    private static final Class<RpcServiceEndpoint> EPCLASS = RpcServiceEndpoint.class;

    @Override
    public void initialize(GenericApplicationContext applicationContext) {
        String packageName = EpricerServiceApplication.class.getPackageName();
        LOG.info("Service dependency builder scans classes from: {}", packageName);
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(packageName).scan()) {
            ClassInfoList depList = scanResult.getClassesWithAnnotation(RPCDEPCLASS.getName());
            LOG.info("Discovered {} types annotated with {}", depList.size(), RPCDEPCLASS.getName());
            depList.stream().map(ci -> ci.loadClass()).forEach(type -> {
                LOG.info("Registering implementation of interface {} as a Spring bean", type.getName());
                validateEndpoints(type);
                String serviceId = type.getAnnotation(RPCDEPCLASS).value();
                applicationContext.registerBean(type, () -> getBean(applicationContext, type, serviceId), bd -> {
                    // bd.setAutowireCandidate(false)
                });
            });
        }
    }

    private <T> T getBean(ApplicationContext context, Class<?> mainInterface, String serviceId) {
        LOG.info("Implementing remote interface '{}' with service-id '{}'", mainInterface.getName(), serviceId);
        InvocationHandler handler = context.getBean(RpcDependencyInvoker.class, serviceId);
        Class<?>[] interfaces = new Class[] {mainInterface};
        @SuppressWarnings("unchecked")
        T result = (T) Proxy.newProxyInstance(ClassUtils.getDefaultClassLoader(), interfaces, handler);
        return result;
    }

    /*
     * The type must be an interface and all its methods must have an end-point annotation and throw
     * valid exceptions.
     */
    private void validateEndpoints(Class<?> type) {
        if (!type.isInterface()) {
            throw new IllegalStateException("Type " + type.getName() + " is annotated with " + RPCDEPCLASS.getName()
                    + ", but is not an interface");
        }

        for (Method method : type.getMethods()) {
            if (!method.isAnnotationPresent(EPCLASS)) {
                throw new IllegalStateException("Interface " + type.getName() + ", method " + method.getName()
                        + " does not have an endpoint annotation");
            }

            Class<?>[] exceptions = method.getExceptionTypes();
            if (exceptions.length > 1
                    || (exceptions.length == 1 && !exceptions[0].equals(BusinessRuleException.class))) {
                throw new IllegalStateException("Interface " + type.getName() + ", method " + method.getName()
                        + " can define only single checked exception of type " + BusinessRuleException.class.getName()
                        + " or no exceptions");
            }
        }
    }
}

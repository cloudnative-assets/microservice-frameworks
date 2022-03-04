package com.ibm.epricer.svclib.rpc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.ibm.epricer.EpricerServiceApplication;
import com.ibm.epricer.svclib.BusinessRuleException;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

/**
 * Scan all service annotations (only directly annotated types), validates them, and build data
 * structures to be used by the invoker.
 *
 * An annotated interface can have non-endpoint methods without annotations. An annotated method
 * must have no more than one parameter and throw no more than one exception.
 * 
 * @author Kiran Chowdhury
 */

@Component
class RpcServiceEndpointMap {
    private static final Logger LOG = LoggerFactory.getLogger(RpcServiceEndpointMap.class);

    private static final Class<RpcService> SERCLASS = RpcService.class;
    private static final Class<RpcServiceEndpoint> EPCLASS = RpcServiceEndpoint.class;
    private static final String ROOT_PACKAGE = EpricerServiceApplication.class.getPackageName();

    private final Map<Pair<String, Integer>, RpcServiceEndpointMetadata> endpoints = new HashMap<>();

    @EventListener
    private void contextRefreshed(ContextRefreshedEvent event) {
        LOG.info("Registering and validating service endpoints");
        discover();
        validate(event.getApplicationContext());
    }

    void discover() {
        endpoints.clear();
        Set<Class<?>> esCandidates;
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(ROOT_PACKAGE).scan()) {
            ClassInfoList esList = scanResult.getClassesWithAnnotation(SERCLASS.getName());
            esCandidates = esList.stream().map(ci -> ci.loadClass()).collect(Collectors.toSet());
            LOG.info("Discovered {} types annotated with {}", esCandidates.size(), SERCLASS.getName());
        }
        for (Class<?> type : esCandidates) {
            Arrays.stream(type.getMethods()).filter(m -> m.isAnnotationPresent(EPCLASS)).forEach(method -> {

                if (!type.isInterface()) {
                    throw new IllegalStateException("Type " + type.getName() + " is annotated with "
                            + SERCLASS.getName() + ", but is not an interface");
                }

                RpcServiceEndpointMetadata info = new RpcServiceEndpointMetadata(type, method);
                Class<?>[] exceptions = method.getExceptionTypes();
                if (exceptions.length == 0) {
                    info.canThrow = false;
                } else if (exceptions.length == 1 && exceptions[0].equals(BusinessRuleException.class)) {
                    info.canThrow = true;
                } else {
                    throw new IllegalStateException("Interface " + type.getName() + ", method " + method.getName()
                            + " can define only single checked exception of type "
                            + BusinessRuleException.class.getName() + " or no exceptions");
                }

                RpcServiceEndpoint apAnn = method.getAnnotation(EPCLASS);

                String endpointId = apAnn.endpointId();
                if (!isEndpointIdValid(endpointId)) {
                    throw new IllegalStateException("Interface " + type.getName() + ", method " + method.getName()
                            + " has illegal endpoint id: " + endpointId);
                }

                int endpointVer = apAnn.endpointVer();
                if (!isEndpointVerValid(endpointVer)) {
                    throw new IllegalStateException("Interface " + type.getName() + ", method " + method.getName()
                            + " has illegal endpoint version: " + endpointVer);
                }

                if (method.getParameterCount() > 1) {
                    throw new IllegalStateException(
                            "Service endpoint " + endpointId + ":" + endpointVer + " has too many parameters");
                }
                if (endpoints.putIfAbsent(Pair.of(endpointId, endpointVer), info) != null) {
                    throw new IllegalStateException(
                            "Duplicate epricer service endpoint:version " + endpointId + ":" + endpointVer);
                }

                info.endpointId = endpointId;
                info.endpointVer = endpointVer;

                LOG.info("Initialized service interface '{}/{}:{}'", type.getName(), endpointId, endpointVer);
            });
        } ;
    }

    void validate(ApplicationContext context) {
        for (RpcServiceEndpointMetadata endpoint : endpoints.values()) {
            try {
                context.getBean(endpoint.type);
            } catch (Throwable e) {
                LOG.error("Implementation is not found for service interface {}", endpoint.type.getName());
                throw e;
            }
        }
    }

    RpcServiceEndpointMetadata getEndpointInfo(String endpointId, int endpointVer) {
        return endpoints.get(Pair.of(endpointId, endpointVer));
    }

    /*
     * Valid end-point names are non-blank alpha-numeric. Is this a good place for this rule?
     */
    private static boolean isEndpointIdValid(String s) {
        return s != null && s.length() > 0 && s.matches("^[a-z][a-z0-9-]+[a-z0-9]$");
    }

    /*
     * End-point version validation
     */
    private static boolean isEndpointVerValid(int endpointVer) {
        return endpointVer > 0;
    }
}

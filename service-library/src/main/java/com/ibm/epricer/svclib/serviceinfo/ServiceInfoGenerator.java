package com.ibm.epricer.svclib.serviceinfo;

import java.util.ArrayList;
import java.util.List;

import com.ibm.epricer.svclib.rpc.RpcDependency;
import com.ibm.epricer.svclib.rpc.RpcService;
import com.ibm.epricer.svclib.rpc.RpcServiceEndpoint;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;


public class ServiceInfoGenerator {	

    
	public static ServiceInfo getServiceInfo(String serviceId, String serviceVer) {
		return ServiceInfoHolder.getServiceInfo(serviceId, serviceVer);
	}
    
    private static class ServiceInfoHolder {
    	
    	static final String ROOT_PACKAGE = "com.ibm.epricer";
    	
    	private static ServiceInfo getServiceInfo(String serviceId, String serviceVer) {
        	ServiceInfo serviceInfo = new ServiceInfo();
        	serviceInfo.serviceid = serviceId;
        	serviceInfo.image = serviceVer;
        	List<EndpointInfo> endpoints = new ArrayList<>();
        	List<InternalDependency> internalDependencies = new ArrayList<>();
        	List<ExternalDependency> externalDependencies = new ArrayList<>();
        	// List<>
        	try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(ROOT_PACKAGE).scan()) {
        		scanResult.getClassesWithAnnotation(RpcService.class.getName())
        			.stream()
        			.forEach(classInfo -> {
        				classInfo.getMethodInfo().stream()
        					.forEach(methodInfo -> {
        						//methodInfo.get
        						AnnotationInfo annotation = methodInfo.getAnnotationInfo(RpcServiceEndpoint.class.getName());
        						String endpointId = annotation.getParameterValues().getValue("endpointId").toString();
        						int endpointVer = annotation.getParameterValues().getValue("endpointVer") !=null ? 
        												Integer.valueOf(annotation.getParameterValues().getValue("endpointVer").toString()) :
        												1;
        						EndpointInfo endpoint = new EndpointInfo();
        						endpoint.endpointid = endpointId;
        						endpoint.endpointversion = endpointVer;
        						endpoints.add(endpoint);
        						
        					});
        			});
        		scanResult.getClassesWithAnnotation(RpcDependency.class.getName())
        			.stream()
        			.forEach(classInfo -> {
        				String dependentServiceId = classInfo.getAnnotationInfo().get(RpcDependency.class.getName())
        					.getParameterValues()
        					.getValue("value")
        					.toString();
        				InternalDependency intDependency = new InternalDependency();
        				intDependency.dependentserviceid = dependentServiceId;
        				List<EndpointInfo> internalDependentEndpoints = new ArrayList<>();
        				classInfo.getMethodInfo().stream()
    					.forEach(methodInfo -> {
    						//methodInfo.get
    						AnnotationInfo annotation = methodInfo.getAnnotationInfo(RpcServiceEndpoint.class.getName());
    						String endpointId = annotation.getParameterValues().getValue("endpointId").toString();
    						int endpointVer = annotation.getParameterValues().getValue("endpointVer") !=null ? 
    												Integer.valueOf(annotation.getParameterValues().getValue("endpointVer").toString()) :
    												1;
    						EndpointInfo endpoint = new EndpointInfo();
    						endpoint.endpointid = endpointId;
    						endpoint.endpointversion = endpointVer;
    						internalDependentEndpoints.add(endpoint);
    						intDependency.dependentendpoints = internalDependentEndpoints;
    					});
        				internalDependencies.add(intDependency);
        			});
        		serviceInfo.endpoints = endpoints;
        		serviceInfo.internaldependencies = internalDependencies;
        		serviceInfo.externaldependencies = externalDependencies;
        	}
        	return serviceInfo;   		
    	}
    }
}

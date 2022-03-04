package com.ibm.epricer.svclib.serviceinfo;

import java.util.ArrayList;
import java.util.List;

public class ServiceInfo {
	public String serviceid;
	
	public String image;
	
	public List<EndpointInfo> endpoints = new ArrayList<EndpointInfo>();
	
	public List<InternalDependency> internaldependencies = new ArrayList<InternalDependency>();
	
	public List<ExternalDependency> externaldependencies = new ArrayList<ExternalDependency>();
	
}

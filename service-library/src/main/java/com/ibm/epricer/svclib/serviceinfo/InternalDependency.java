package com.ibm.epricer.svclib.serviceinfo;

import java.util.ArrayList;
import java.util.List;

public class InternalDependency {
	public String dependentserviceid;
	
	public List<EndpointInfo> dependentendpoints = new ArrayList<EndpointInfo>();
}

package com.ibm.epricer.svclib.serviceinfo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/info")
public class ServiceInfoController {

    @Value("${epricer.service-id}")
    private String serviceId;
    
    @Value("${epricer.service-ver:0.0.0}")
    private String serviceVer;
    


    
    
    @GetMapping(produces = "application/json")
	public ResponseEntity<ServiceInfo> getServiceInfo() {
    	ServiceInfo serviceInfo = ServiceInfoGenerator.getServiceInfo(serviceId, serviceVer);
    	return ResponseEntity.status(HttpStatus.OK).body(serviceInfo); 
	}

}

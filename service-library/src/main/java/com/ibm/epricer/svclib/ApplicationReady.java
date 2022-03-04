package com.ibm.epricer.svclib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * This handler runs when Spring context is refreshed and the service is ready to accept requests.
 * 
 * @author Kiran Chowdhury
 */

@Component
class ApplicationReady {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationReady.class);

    @Autowired
    private ServerProperties serverProperties;

    @Value("${epricer.service-id}")
    private String serviceId;
    @Value("${epricer.service-ver:0.0.0}")
    private String serviceVer;

    @EventListener
    private void contextRefreshed(ApplicationReadyEvent event) {
        LOG.debug("Max number of concurrent requests configured: {}",
                serverProperties.getTomcat().getThreads().getMax());
        LOG.info("Service {} version {} is ready to serve the world", serviceId, serviceVer);
    }
}

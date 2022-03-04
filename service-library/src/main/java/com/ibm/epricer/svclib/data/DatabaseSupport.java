package com.ibm.epricer.svclib.data;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

/**
 * Configures database transaction point-cut and helps check if database support should be enabled.
 * This class must be configured in META-INF/spring.factories.
 * 
 * The point-cut is always created even if no database support is required because it may be used by
 * higher-level libraries for their own transaction needs.
 * 
 * @author Kiran Chowdhury
 */

@Configuration
@ImportResource("classpath:database-pointcut.xml")
public class DatabaseSupport implements ApplicationListener<ApplicationContextInitializedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseSupport.class);
    private static final String KEY = "spring.autoconfigure.exclude";
    private static final String VALUE = "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration";

    @Override
    public void onApplicationEvent(ApplicationContextInitializedEvent event) {
        LOG.debug("Checking if database support needs to be enabled...");
        if (!DatabaseSupportChecker.isDatabaseEnabled()) {
            ConfigurableEnvironment env = event.getApplicationContext().getEnvironment();
            /*
             * If KEY property already exists, override it with the combined value 
             */
            String existingValue = env.getProperty(KEY);
            String value = VALUE + (isNotBlank(existingValue) ? "," + existingValue : "");
            MutablePropertySources sources = env.getPropertySources();
            sources.addFirst(new MapPropertySource("disable-database", Collections.singletonMap(KEY, value)));
        }
    }
}

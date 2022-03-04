package com.ibm.epricer.svclib.data;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;

abstract class CommonDataConfig {

    @Value("${epricer.eclipselink.logging.level}")
    private String loggingLevel;

    @Value("${epricer.eclipselink.logging.level.metadata}")
    private String loggingMetadata;

    @Value("${epricer.eclipselink.logging.level.connection}")
    private String loggingConnection;

    @Value("${epricer.eclipselink.logging.level.sql}")
    private String loggingSql;

    @Value("${epricer.database-platform}")
    private String databasePlatform;

    @Value("${epricer.eclipselink.logging.level.parameters}")
    private String loggingParamters;
    
    LocalContainerEntityManagerFactoryBean entityManagerFactoryTemplate() {

        EclipseLinkJpaVendorAdapter vendorAdapter = new EclipseLinkJpaVendorAdapter();
        vendorAdapter.setDatabasePlatform(databasePlatform);

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);

        Properties props = new Properties();

        props.setProperty("eclipselink.cache.shared.default", "false");
        props.setProperty("eclipselink.jdbc.batch-writing", "JDBC");
        props.setProperty("eclipselink.weaving", "false");

        /*
         * EclipseLink logging levels are configurable in the clients with defaults defined here. The full
         * list of available logging levels see LogCategory.java and PersistenceUnitProperties.java
         */
        props.setProperty("eclipselink.logging.logger", "JavaLogger");
        props.setProperty("eclipselink.logging.level", loggingLevel);
        props.setProperty("eclipselink.logging.level.metadata", loggingMetadata);
        props.setProperty("eclipselink.logging.level.connection", loggingConnection);
        props.setProperty("eclipselink.logging.level.sql", loggingSql);
        props.setProperty("eclipselink.logging.parameters", loggingParamters);

        factory.setJpaProperties(props);

        return factory;
    }
}

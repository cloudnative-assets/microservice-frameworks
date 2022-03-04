package com.ibm.epricer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import com.ibm.epricer.svclib.TrustedCertificates;

/**
 * This class must be located in the top-level epricer package so that all annotations from all
 * lower-level packages can be picked up by the auto-configuration.
 * 
 * Services should use this bootstrapper without @SpringBootApplication annotation:
 * 
 * <pre>
 * public class HelloApplication {
 *     public static void main(String[] args) {
 *         EpricerServiceApplication.run(args);
 *     }
 * }
 * </pre>
 */

@SpringBootApplication
public class EpricerServiceApplication {
    public static ConfigurableApplicationContext run(String... args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(EpricerServiceApplication.class);
        SpringApplication app = builder.build();
        app.addListeners(new TrustedCertificates());
        return app.run(args);
    }
}

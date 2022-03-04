package com.ibm.epricer.svclib;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.ibm.epricer.svclib.rpc.http.NoProxyRestTemplateCustomizer;

/**
 *  REST template and Jackson JSON mapper configurations.
 */

@Configuration
@PropertySource("classpath:svclib.properties") // default service properties
public class SvcLibConfig {
    private static final String DATE_FORMAT = "dd MMM yyyy";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5L); // 5 seconds max

    @Autowired
    private ClientHttpRequestInterceptor rpcInterceptor;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .additionalInterceptors(rpcInterceptor)
                .messageConverters(new StringHttpMessageConverter())
                .additionalCustomizers(new NoProxyRestTemplateCustomizer())
                .setConnectTimeout(CONNECT_TIMEOUT)
                .build();
    }

    @Bean
    public Jackson2ObjectMapperBuilder objectMapperBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

        builder.indentOutput(true);

        builder.simpleDateFormat(DATE_FORMAT);
        /*
         * Do not serialize properties with null values
         */

        builder.serializationInclusion(JsonInclude.Include.NON_ABSENT);
        /*
         * Prevent NPE when property case is misspelled: "userid" vs "userId"
         */
        builder.featuresToEnable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return builder;
    }
}

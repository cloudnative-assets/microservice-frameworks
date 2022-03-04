package com.ibm.epricer.svclib.rpc.http;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Customizes REST template to, instead of using DefaultProxyRoutePlanner, always connect via direct
 * routes and NOT use any HTTP proxies.
 * 
 * @author Kiran Chowdhury
 */
public class NoProxyRestTemplateCustomizer implements RestTemplateCustomizer {

    @Override
    public void customize(RestTemplate restTemplate) {

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        /*
         * The biggest connection consumer is the gateway route. Default max per route is only 2, which
         * creates a bottleneck, so setting it to 50 ensures best possible gateway communication throughput.
         * For all other routes, Solr and anything else, adding another 50 connections for a total of 100.
         */
        cm.setMaxTotal(100);
        cm.setDefaultMaxPerRoute(50);
        HttpClient httpClient = HttpClientBuilder.create().setRoutePlanner(new HttpRoutePlanner() {

            @Override
            public HttpRoute determineRoute(final HttpHost host, final HttpRequest request, final HttpContext context) {
                return new HttpRoute(host); // return DIRECT route
            }

        }).setConnectionManager(cm).build();

        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));

    }

}

package com.ibm.epricer.svclib.objectstore.solr;

import java.util.List;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.convert.MappingSolrConverter;
import org.springframework.data.solr.core.convert.SolrConverter;
import org.springframework.data.solr.core.convert.SolrCustomConversions;
import org.springframework.data.solr.core.mapping.SimpleSolrMappingContext;

@Configuration
class SolrConfig {
    private static final String DEFAULT_URL = "http://127.0.0.1:8983/solr";
    
    private static final String FIRST_SOLR_URL_KEY = "epricer.solr-store.first-url";
    
    /*
     * This key is used while integration test of an endpoint that interact with solr. 
     * Do not define this property in application.properties or application.yaml.
     */
    private static final String TEST_CONTAINERS_SOLR_URL_KEY = "epricer.solr-store.test-url";

    private static final int SOLR_CONNECTION_TIMEOUT = 5_000; // 5 seconds
    private static final int SOLR_SOCKET_TIMEOUT = 30_000; // 30 seconds
    
    private static Logger LOG = LoggerFactory.getLogger(SolrConfig.class);

    @Autowired
    Environment env;

    /**
     * If there are multiple Solr stores, define additional solr client and template beans like this,
     * but with different names, and add second-url to the configuration.
     */
    @Bean
    SolrClient firstSolrClient() {
        String host = solrUrlResolver();
        return new HttpSolrClient.Builder(host)
                .withConnectionTimeout(SOLR_CONNECTION_TIMEOUT)
                .withSocketTimeout(SOLR_SOCKET_TIMEOUT).build();
    }    
    
     
    private String solrUrlResolver() {
    	String host = env.getProperty(FIRST_SOLR_URL_KEY, System.getProperty(TEST_CONTAINERS_SOLR_URL_KEY));
    	if (host !=null && host.startsWith("http")) {
    	} else {
    		host = DEFAULT_URL;
    	}
    	LOG.info("Solr url resolved with {}", host);
    	return host;
    }
    
    @Bean
    SolrOperations firstSolrTemplate() {
        SolrTemplate template = new SolrTemplate(firstSolrClient());
        template.setSolrConverter(converter());
        return template;
    }

    @Bean
    SolrConverter converter() {
        MappingSolrConverter converter = new MappingSolrConverter(getContext());
        converter.setCustomConversions(new SolrCustomConversions(List.of(jscCollectionConverter())));
        return converter;
    }
    
    @Bean
    SimpleSolrMappingContext getContext() {
        return new SimpleSolrMappingContext();
    }

    @Bean
    JscCollectionConverter jscCollectionConverter() {
        return new JscCollectionConverter();
    }
}


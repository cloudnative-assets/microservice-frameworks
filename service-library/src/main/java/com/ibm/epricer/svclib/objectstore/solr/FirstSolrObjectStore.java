package com.ibm.epricer.svclib.objectstore.solr;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.stereotype.Component;
import com.ibm.epricer.svclib.objectstore.Criteria.Criterion;
import com.ibm.epricer.svclib.objectstore.ObjectEntity;
import com.ibm.epricer.svclib.objectstore.ObjectRequest;
import com.ibm.epricer.svclib.objectstore.ObjectRequestExecutor;

/**
 * Solr object store executor. Translates ObjectRequst into Solr requests and executes them. Can
 * work with multiple Solr instances if multiple Solr templates are configured.
 * 
 * Entity objects must extend ObjectEntity class and can have strongly typed sub-entities of Array
 * or Collection type.
 * 
 * @author Kiran Chowdhury
 */

@Component("FirstSolrObjectStore")
class FirstSolrObjectStore implements ObjectRequestExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(FirstSolrObjectStore.class);
    private static final String MAX_RESULT_SIZE_PROP = "epricer.solr-store.max-result-size";

    @Autowired
    Environment env;

    @Autowired
    @Qualifier("firstSolrTemplate")
    private SolrOperations solrOps;

    /*
     * Translating persistence-technology-neutral Criteria into Solr-native Criteria
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends ObjectEntity> List<T> execute(ObjectRequest<T> request) {
        Criteria nativeCriteria = null;
        boolean contentTypeSet = false;

        for (Criterion criterion : request.getCriteria()) {

            if (nativeCriteria == null) { // first criterion
                nativeCriteria = Criteria.where(criterion.getField());
            } else {
                nativeCriteria = nativeCriteria.and(criterion.getField());
            }

            if (criterion.getField().equalsIgnoreCase("contenttype")) {
                contentTypeSet = true;
            }

            switch (criterion.getKey()) {
                case IS:
                    nativeCriteria = nativeCriteria.is(criterion.getValue());
                    break;

                case IN:
                    nativeCriteria = nativeCriteria.in(criterion.getValue());
                    break;

                case CONTAINS:
                    nativeCriteria = nativeCriteria.contains((Iterable<String>) criterion.getValue());
                    break;

                case STARTS_WITH:
                    nativeCriteria = nativeCriteria.startsWith((String) criterion.getValue());
                    break;

                case ENDS_WITH:
                    nativeCriteria = nativeCriteria.endsWith((String) criterion.getValue());
                    break;

                default:
                    throw new IllegalStateException("Unknown predicate type");
            }
        }

        if (!contentTypeSet) {
            throw new IllegalStateException("Contenttype criterion must always be specified in the query");
        }

        int maxResultSize = env.getProperty(MAX_RESULT_SIZE_PROP, Integer.class);

        Query nativeQuery = Query.query(nativeCriteria).setRows(maxResultSize);

        Instant start = Instant.now();
        Page<T> page = solrOps.query("param", nativeQuery, request.getEntityType());
        long timeElapsed = Duration.between(start, Instant.now()).toMillis();

        if (page.getTotalPages() > 1) {
            LOG.warn("Result set size exceeds max allowed: {}", maxResultSize);
        }

        List<T> result = page.getContent();

        LOG.debug("Retrieved {} Solr entities in {} ms", result.size(), timeElapsed);
        return result;
    }

    @Override
    public Type type() {
        return Type.TERMINAL;
    }
}

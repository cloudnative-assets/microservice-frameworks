package com.ibm.epricer.svclib.objectstore;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

/**
 * Examples of working with ObjectStore. These test never fail 
 */

@SpringBootTest
@TestPropertySource(properties = "epricer.solr-store.first-url=http://tpydalepws401.sl.bluecloud.ibm.com:8983/solr")
@TestPropertySource(properties = "epricer.object-store.cache-ttl=2")
@DirtiesContext
@Tag("ManualTest")
public class ObjectStoreTest {
    @Autowired
    ObjectStore store;
    
    @Test
    @DisplayName("Object store test")
    void testSpcn() throws Exception {
        Criteria criteria = Criteria
                .where("contenttype").endsWith("ctmpspcn.json")
                .and("spcn_ctrycode").is("US")
                .and("spcn_codetype").is("C")
                .and("spcn_spcndiscount").in("0.41"); // testing IN predicate
        
        ObjectQuery<SpcnEntity> query = store.retrieve(SpcnEntity.class).where(criteria);

        try {
            SpcnEntity spcn = query.single().get();
            System.out.println("SPCN document: " + spcn);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Test failed");
        }
    }
    
    @Test
    @DisplayName("Conversion of JSCCOLLECTION strings")
    void testOrf() throws Exception {
        Criteria criteria = Criteria
                .where("contenttype").is("CYctmporf.json")
                .and("orf_fk_tablecode").is("C");
        
        ObjectQuery<OrfEntity> query = store.retrieve(OrfEntity.class).where(criteria);
        
        try {
            List<OrfEntity> orfList = query.list();
            System.out.println("Number of ORF entries: " + orfList.size());
            System.out.println("First ORF document: " + orfList.get(0));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Test failed");
        }
    }
}

class SpcnEntity extends ObjectEntity {
    public String contenttype;
    public String spcn_codetype;
    public String spcn_parentcode;
    public String spcn_alternatespcnnb;
    public String spcn_spcndiscount;
    public String spcn_ctrycode;
    public String spcn_ctmpspcnid;
    public String spcn_spcnnb;
}

class OrfEntity extends ObjectEntity {
    public String orf_activetransactionq;
    public String orf_ctmporfid;
    public String orf_description;
    public Nls[] nls_list; // can also be Collection<Map<String,String>>
}

class Nls extends ObjectEntity {
    public String nls_description;
    public String nls_languagecode;
}

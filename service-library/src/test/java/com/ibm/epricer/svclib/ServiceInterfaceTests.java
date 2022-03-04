package com.ibm.epricer.svclib;

import static com.ibm.epricer.svclib.rpc.http.HttpConstants.UTE_MEDIA_TYPE;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.ibm.epricer.data.pref.Ctmausr;
import com.ibm.epricer.svclib.test.UserDataInput;

@SpringBootTest(properties = {"pref-db-name=ServiceInterfaceTests"})
@AutoConfigureMockMvc
@DirtiesContext
public class ServiceInterfaceTests {
    @Autowired 
    private MockMvc mvc;
    @Autowired
    private TestUtil util;
    @PersistenceContext(unitName = "pu-pref")
    private EntityManager prefEm;
    
    private static final EmbeddedDatabase PREF_DB = new EmbeddedDatabaseBuilder()
            .setName("ServiceInterfaceTests")
            .addScript("test/pref-db.sql")
            .build();

    @AfterAll
    public static void cleanUp() {
        PREF_DB.shutdown();
    }
    
    @Test
    @DisplayName("GET call to /rpc should result in HTTP 400 error")
    void testGetMethod() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/rpc")).andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Successful service calls should commit database changes")
    void testUpdateCommit(@Autowired MockMvc mvc) throws Exception {
        final String newLastName = "Jordan";

        UserDataInput serviceInput = new UserDataInput();
        serviceInput.setUserId("MB");
        serviceInput.setLastName(newLastName);

        mvc.perform(util.callSutEndpoint("user-change-lastname", serviceInput)) // .andDo(print())
                .andExpect(status().isOk());

        assertEquals(newLastName, prefEm.find(Ctmausr.class, "2").lastname);
    }
    
    @Test
    @DisplayName("Business rule exceptions should roll-back database changes")
    void testUpdateRollback(@Autowired MockMvc mvc) throws Exception {
        final String existingLastName = "Topchiev";
        final String newLastName = "Cohen";

        UserDataInput serviceInput = new UserDataInput();
        serviceInput.setUserId("MT");
        serviceInput.setLastName(newLastName);

        mvc.perform(util.callSutEndpoint("user-change-lastname", serviceInput)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("epricer-status", "1"));

        assertEquals(existingLastName, prefEm.find(Ctmausr.class, "1").lastname);
    }

    @Test
    @DisplayName("UTEs should result in HTTP 500, application/epricer-exception and roll-back")
    void testDbConstraintViolation(@Autowired MockMvc mvc) throws Exception {
        final String existingLastName = "Jackson";
        final String newLastName = "King of Pop";

        UserDataInput serviceInput = new UserDataInput();
        serviceInput.setUserId("MJ");
        serviceInput.setLastName(newLastName);

        mvc.perform(util.callSutEndpoint("user-change-lastname", serviceInput)) // .andDo(print())
                .andExpect(status().is(500))
                .andExpect(content().contentType(UTE_MEDIA_TYPE))
                .andExpect(content().string(startsWith("%40")));

        assertEquals(existingLastName, prefEm.find(Ctmausr.class, "3").lastname);
    }
}

package com.ibm.epricer.svclib;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provides user id and potentially other details
 *   
 * @author Kiran Chowdhury
 */

@Component
public class UserDetails {
    private static final String EMAIL_HEADER = "epricer-user-id";
    private static final String GROUPID_HEADER = "epricer-group-id";
    
    @Autowired
    private ServiceContext context;

    public String getUserId() {
        Map<String, String> savedHeaders = context.getPropagatingHeaders();
        return savedHeaders.get(EMAIL_HEADER);
    }
    
    public String getGroupId() {
        Map<String, String> savedHeaders = context.getPropagatingHeaders();
        return savedHeaders.get(GROUPID_HEADER);
    }

}

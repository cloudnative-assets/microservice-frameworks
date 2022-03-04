package com.ibm.epricer.svclib.rpc.http;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ibm.epricer.svclib.ServiceContext;

/**
 * This filter looks through ALL incoming and outgoing HTTP requests, and passes their headers into
 * the service context bean for processing.
 * 
 * @author Kiran Chowdhury
 */

@Component
class HttpServiceRequestFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(HttpServiceRequestFilter.class);

    @Autowired
    private ServiceContext context;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

        Map<String, String> requestHeaders = new HashMap<>();
        List<String> headerNames = Collections.list(httpServletRequest.getHeaderNames());
        for (String headerName : headerNames) {
            requestHeaders.put(headerName, httpServletRequest.getHeader(headerName));
        }
        LOG.trace("Incoming request header map: {}", requestHeaders);

        context.processRequestHeaders(requestHeaders);

        filterChain.doFilter(httpServletRequest, servletResponse);

        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        Map<String, String> responseHeaders = new HashMap<>();
        for (String headerName : httpServletResponse.getHeaderNames()) {
            responseHeaders.put(headerName, httpServletResponse.getHeader(headerName));
        }
        LOG.trace("Outgoing response header map: {}", responseHeaders);

        context.processResponseHeaders(responseHeaders);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}

}

/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package org.sec2.frontend;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.MDC;

/**
 * A servlet filter that inserts various values retrieved from the incoming http
 * request into the MDC.
 * Class was copied into the sec2 namespace to prevent having a compile time
 * dependancy to ch.qos.logback.classic.
 * <p/>
 * <p/>
 * The values are removed after the request is processed.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public final class MDCInsertingServletFilter implements Filter {

    /** {@inheritDoc } */ @Override
    public void init(final FilterConfig arg0) throws ServletException { }

    /** {@inheritDoc } */ @Override
    public void destroy() { }

    /** {@inheritDoc } */ @Override
    public void doFilter(final ServletRequest request,
            final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        insertIntoMDC(request);
        try {
          chain.doFilter(request, response);
        } finally {
          clearMDC();
        }
    }

    /**
     * Creates MDC entries.
     * @param request The request object
     */
    private void insertIntoMDC(final ServletRequest request) {
        MDC.put("req.remoteHost", request.getRemoteHost());
        MDC.put("req.remoteAddr", request.getRemoteAddr());
        MDC.put("req.remotePort", String.valueOf(request.getRemotePort()));

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest =
                    (HttpServletRequest) request;
            MDC.put("req.requestURI", httpServletRequest.getRequestURI());
            StringBuffer requestURL = httpServletRequest.getRequestURL();
            if (requestURL != null) {
              MDC.put("req.requestURL", requestURL.toString());
            }
            MDC.put("req.queryString", httpServletRequest.getQueryString());
            MDC.put("req.userAgent",
                    httpServletRequest.getHeader("User-Agent"));
            MDC.put("req.xForwardedFor",
                    httpServletRequest.getHeader("X-Forwarded-For"));
        }
    }

    /**
     * Removes MDC entries.
     */
    private void clearMDC() {
        MDC.remove("req.remoteHost");
        MDC.remove("req.requestURI");
        MDC.remove("req.queryString");
        // removing possibly inexistent item is OK
        MDC.remove("req.requestURL");
        MDC.remove("req.userAgent");
        MDC.remove("req.xForwardedFor");
    }
}

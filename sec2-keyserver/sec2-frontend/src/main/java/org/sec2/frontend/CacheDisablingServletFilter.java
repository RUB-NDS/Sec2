/*
 * Copyright 2012 Ruhr-University Bochum, Chair for Network and Data Security
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://nds.rub.de/research/projects/sec2/
 */
package org.sec2.frontend;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet filter that disables all caching mechanisms in HTTP.
 * Adapted from
 * http://darksleep.com/notablog/articles/Java_Servlet_NoCache_Filtering
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * January 21, 2013
 */
public class CacheDisablingServletFilter implements Filter {

    /** {@inheritDoc } */ @Override
    public void init(final FilterConfig arg0) throws ServletException { }

    /** {@inheritDoc } */ @Override
    public void destroy() { }

    /** {@inheritDoc } */ @Override
    public void doFilter(final ServletRequest request,
            final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpresponse =
                    (HttpServletResponse) response;
            httpresponse.setHeader("Cache-Control",
                    "no-cache, no-store, must-revalidate, private");
            httpresponse.setHeader("Pragma", "no-cache");
            httpresponse.setHeader("Expires", "0");
            }
        chain.doFilter(request, response);
    }
}

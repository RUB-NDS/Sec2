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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import junit.framework.TestCase;
import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;

/**
 * Tests for the endpoint servlet of the keyserver.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * Oktober 17, 2012
 */
public class AbstractServletTests extends TestCase {

    /**
     * An embedded jetty server created by
     * {@link org.sec2.frontend.testsuites.ServletTestSetup}.
     */
    private ServletTester tester;

    /**
     * A helper to generate a HTTP request.
     */
    private HttpTester request;

    /**
     * A helper to parse a HTTP response.
     */
    private HttpTester response;

    /**
     * Setups the HTTP testers.
     */
    @Override
    public void setUp() {
        tester   = ServletTestSetup.getTester();
        if (tester == null) {
            fail("This test case needs to be run in context of "
                    + "org.sec2.frontend.ServletTestSetup!");
        }
        request  = new HttpTester();
        response = new HttpTester();
        request.setMethod("POST");
        request.setURI("/keyserver");
        request.setVersion("HTTP/1.1");
        request.setHeader("Host", "localhost"); //needed to be HTTP1.1 compliant
    }

    /**
     * Deletes the HTTP testers.
     */
    @Override
    public void tearDown() {
        tester   = null;
        request  = null;
        response = null;
    }

    /**
     * @return the tester
     */
    protected final ServletTester getTester() {
        return tester;
    }

    /**
     * @return the request
     */
    protected final HttpTester getRequest() {
        return request;
    }

    /**
     * @return the response
     */
    protected final HttpTester getResponse() {
        return response;
    }

    /**
     * @param filename a resource file's name
     * @return The content of the resource file
     */
    protected final String getTestXML(final String filename) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(
                this.getClass().getResourceAsStream("/testXML/" + filename)));
            try {
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                    sb.append('\n');
                }
            } finally {
                rd.close();
            }
        } catch (IOException e) {
            return null;
        }
        return sb.toString();
    }
}

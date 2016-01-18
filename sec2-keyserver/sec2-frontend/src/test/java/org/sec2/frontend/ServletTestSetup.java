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

import junit.extensions.TestSetup;
import junit.framework.Test;
import org.mortbay.jetty.testing.ServletTester;

/**
 * This testsetup is used to setup the jetty server before testing and tearing
 * it down afterwards.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * Oktober 15, 2012
 */
public class ServletTestSetup extends TestSetup {

    /**
     * An embedded jetty server that uses a special local connector
     * to test the servlet.
     */
    private static ServletTester tester;

    /**
     * @return the tester
     */
    public static ServletTester getTester() {
        return tester;
    }

    /**
     * Create the test setup.
     *
     * @param test the test case
     */
    public ServletTestSetup(final Test test) {
        super(test);
    }

    /**
     * Setups the jetty server for testing.
     */
    @Override
    public void setUp() {
        tester = new ServletTester();
        tester.setContextPath("/");
        tester.addServlet(Endpoint.class, "/keyserver");
        try {
            tester.start();
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            fail(e.getMessage());
        }
    }

    /**
     * Tears the jetty server down after testing.
     */
    @Override
    public void tearDown() {
        try {
            tester.stop();
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            fail(e.getMessage());
        }
    }
}

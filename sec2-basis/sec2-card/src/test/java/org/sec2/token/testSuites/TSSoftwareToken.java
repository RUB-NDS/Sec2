/*
 * Copyright 2011 Sec2 Consortium
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit written
 * permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://www.sec2.org
 */
package org.sec2.token.testSuites;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.sec2.token.softwaretoken.*;

/**
 * Testsuite for HardwareToken.
 *
 * @author benedikt
 */
public class TSSoftwareToken extends TestCase {

    public TSSoftwareToken(String testName) {
        super(testName);
    }

    public static Test suite() {
        System.out.println("\n==== Starting SoftwareToken Tests ====");

        TestSuite suite = new TestSuite();

        suite.addTestSuite(GroupKeyTest.class);
        suite.addTestSuite(DocumentKeyTest.class);
        suite.addTestSuite(PinPukTest.class);
        suite.addTestSuite(ServerKeyTest.class);
        suite.addTestSuite(UserKeyTest.class);
        suite.addTestSuite(ConcurrentAccessTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public static void main (String[] args) {
        int c = 0;
        while (true) {
            TestRunner.run(suite());
            System.out.println(c++);
        }
    }
}

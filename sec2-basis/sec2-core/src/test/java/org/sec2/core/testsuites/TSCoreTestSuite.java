/*
 * Copyright 2011 Ruhr-University Bochum, Chair for Network and Data Security
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://nds.rub.de/research/projects/sec2/
 */

package org.sec2.core.testsuites;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.sec2.configuration.ConfigurationManagerTests;
import org.sec2.managers.GroupTests;
import org.sec2.managers.UserTests;

/**
 * Test suite for module sec2-core.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * June 24, 2012
 */
public class TSCoreTestSuite extends TestCase {
    /**
     * Create the test case.
     *
     * @param testName name of the test case
     */
    public TSCoreTestSuite(final String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();

        suite.addTestSuite(UserTests.class);
        suite.addTestSuite(GroupTests.class);
        suite.addTestSuite(ConfigurationManagerTests.class);
        //Add more tests here

        return suite;
    }
}

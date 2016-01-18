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
package org.sec2.frontend.processors;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test suite for namespace org.sec2.frontend.processors.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 29, 2012
 */
public class FrontendProcessorsTestSuite extends TestCase {

    /**
     * Create the test suite.
     *
     * @param testName name of the test case
     */
    public FrontendProcessorsTestSuite(final String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();

        suite.addTestSuite(AddUsersToGroupProcessorTests.class);
        suite.addTestSuite(CreateGroupProcessorTests.class);
        suite.addTestSuite(DeleteGroupProcessorTests.class);
        suite.addTestSuite(GetGroupMembersProcessorTests.class);
        suite.addTestSuite(GetGroupProcessorTests.class);
        suite.addTestSuite(GetGroupsForUserProcessorTests.class);
        suite.addTestSuite(GetKnownUsersForUserProcessorTests.class);
        suite.addTestSuite(GetUserInfoByIDProcessorTests.class);
        suite.addTestSuite(GetUserInfoByMailProcessorTests.class);
        suite.addTestSuite(RegisterUserProcessorTests.class);
        suite.addTestSuite(RemoveUsersFromGroupProcessorTests.class);
        suite.addTestSuite(Sec2MessageProcessorTests.class);

        return suite;
    }
}

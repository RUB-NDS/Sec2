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
package org.sec2.saml.xml;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test suite for namespace org.sec2.saml.xml.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * December 03, 2012
 */
public class SAMLXMLTestSuite extends TestCase {

    /**
     * Create the test suite.
     *
     * @param testName name of the test case
     */
    public SAMLXMLTestSuite(final String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();

        suite.addTestSuite(AddUsersToGroupTests.class);
        suite.addTestSuite(CreateGroupTests.class);
        suite.addTestSuite(ConfirmUserTests.class);
        suite.addTestSuite(DeleteGroupTests.class);
        suite.addTestSuite(EmailAddressTests.class);
        suite.addTestSuite(GetGroupMembersTests.class);
        suite.addTestSuite(GetGroupTests.class);
        suite.addTestSuite(GetGroupsForUserTests.class);
        suite.addTestSuite(GetKnownUsersForUserTests.class);
        suite.addTestSuite(GetUserInfoByIDTests.class);
        suite.addTestSuite(GetUserInfoByMailTests.class);
        suite.addTestSuite(GroupInfoTests.class);
        suite.addTestSuite(GroupListTests.class);
        suite.addTestSuite(GroupMemberListTests.class);
        suite.addTestSuite(RegisterUserTests.class);
        suite.addTestSuite(RemoveUsersFromGroupTests.class);
        suite.addTestSuite(UpdateGroupTests.class);
        suite.addTestSuite(UpdateUserTests.class);
        suite.addTestSuite(UserInfoTests.class);
        suite.addTestSuite(UserListTests.class);

        return suite;
    }
}

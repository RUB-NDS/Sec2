/*
 * Copyright 2013 Ruhr-University Bochum, Chair for Network and Data Security
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

import static junit.framework.Assert.fail;
import junit.framework.TestCase;
import org.sec2.backend.IGroupInfo;
import org.sec2.backend.IUserInfo;
import org.sec2.backend.exceptions.GroupAlreadyExistsException;
import org.sec2.backend.exceptions.InvalidGroupNameException;
import org.sec2.backend.exceptions.PermissionException;
import org.sec2.backend.exceptions.UserNotFoundException;
import org.sec2.backend.impl.AbstractUserManagementFactory;
import org.sec2.statictestdata.TestKeyProvider;

/**
 * Base class that creates a test group that can be used for tests.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * June 11, 2013
 */
public class AbstractGroupRequiringTests extends TestCase {

    /**
     * The test groups identifier.
     */
    protected static final String GROUPNAME = "testGroup";

    /**
     * Create test group.
     */
    @Override
    public void setUp() {
        try {
            AbstractUserManagementFactory.createDefault().createGroup(
                        TestKeyProvider.getInstance().getUserID(), GROUPNAME);
        } catch (GroupAlreadyExistsException e) {
            fail(e.getMessage());
        } catch (UserNotFoundException e) {
            fail(e.getMessage());
        } catch (InvalidGroupNameException e) {
            fail(e.getMessage());
        } catch (PermissionException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Adds the second test user to the test group.
     */
    protected void addSecondUserToTestGroup() {
        try {
            IGroupInfo group = AbstractUserManagementFactory.createDefault().
                    getGroupInfo(TestKeyProvider.getInstance().getUserID(),
                    GROUPNAME);
            IUserInfo user2 = AbstractUserManagementFactory.createDefault().
                    getUserInfo(TestKeyProvider.getInstance().getUser2ID());
            group.getMembers().add(user2);
            AbstractUserManagementFactory.createDefault().modifyGroup(
                    TestKeyProvider.getInstance().getUserID(), group);
        } catch (UserNotFoundException e) {
            fail("Test user does not exist in backend");
        } catch (PermissionException e) {
            fail("Test group is not accessible");
        }
    }

    /**
     * Delete test group.
     */
    @Override
    public void tearDown() {
        try {
            AbstractUserManagementFactory.createDefault().deleteGroup(
                    TestKeyProvider.getInstance().getUserID(), GROUPNAME);
        } catch (PermissionException e) {
            fail(e.getMessage());
        }
    }
}

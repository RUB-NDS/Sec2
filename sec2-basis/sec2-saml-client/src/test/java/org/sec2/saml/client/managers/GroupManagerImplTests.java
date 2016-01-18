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
package org.sec2.saml.client.managers;

import java.io.IOException;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import junit.framework.TestCase;
import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.managers.IGroupManager;
import org.sec2.managers.IUserManager;
import org.sec2.managers.ManagerProvider;
import org.sec2.managers.beans.Group;
import org.sec2.saml.client.SAMLClientBootstrap;
import org.sec2.saml.client.exceptions.SAML2MiddlewareProxyException;
import org.sec2.saml.exceptions.KeyserverException;
import org.sec2.statictestdata.TestKeyProvider;

/**
 * Tests for the group manager.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 14, 2013
 */
public class GroupManagerImplTests extends TestCase {

    /**
     * The manager to test.
     */
    private IGroupManager groupManager;

    /**
     * Bootstraps.
     *
     * @throws Exception whatever goes wrong
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SAMLClientBootstrap.bootstrap();
        groupManager = GroupManagerImpl.getInstance();

    }

    /**
     * Tests getting a group.
     */
    public void testGetGroup() {
        Group g = null;
        try {
            g = groupManager.getGroup("user@sec2.org");
        } catch (ExMiddlewareException e) {
            fail(e.toString());
        } catch (IOException e) {
            fail(e.toString());
        }
        assertNotNull(g);
        assertTrue(g.isSynced());
        assertEquals("user@sec2.org", g.getGroupName());
        assertEquals(1, g.getMembersINTERNAL().size());
        assertNotNull(g.getOwner());
        assertEquals("user@sec2.org", g.getOwner().getEmailAddress());
    }

    /**
     * Tests getting all groups a user is member of.
     */
    public void testGetGroupsForUser() {
        String[] groupNames = null;
        try {
            groupNames = groupManager.getGroupsForUser(ManagerProvider.
                    getInstance().getUserManager().getRegisteredUser().
                    getUserID());
        } catch (ExMiddlewareException e) {
            fail(e.toString());
        } catch (IOException e) {
            fail(e.toString());
        }
        assertNotNull(groupNames);
        assertTrue(groupNames.length >= 1);
    }

    /**
     * Tests creating and deleting a group.
     *
     * @throws Exception let JUnit catch the crash
     */
    public void testCreateAndDeleteGroup() throws Exception {
        Group g = new Group("TestCreateGroup", ManagerProvider.getInstance().
                getUserManager().getRegisteredUser());
        assertFalse(g.isSynced());

        try {
            groupManager.createGroup(g);
            assertTrue(g.isSynced());
            assertEquals(g, groupManager.getGroup("TestCreateGroup"));
            assertEquals(g.getGroupName(), "TestCreateGroup");
        } finally {
            groupManager.deleteGroup(g.getGroupName());
            assertFalse(g.isSynced());
        }

        try {
            groupManager.getGroup("TestCreateGroup");
        } catch (SAML2MiddlewareProxyException e) {
            assertTrue(e.getCause() instanceof KeyserverException);
            assertTrue(e.getCause().getMessage().contains("503"));
        }
    }

    /**
     * Tests adding and removing a user.
     *
     * @throws Exception let JUnit catch the crash
     */
    public void testAddAndRemoveUserFromGroup() throws Exception {
        IUserManager userManager =
                ManagerProvider.getInstance().getUserManager();
        Group group = groupManager.getGroup(
                userManager.getRegisteredUser().getEmailAddress());
        assertTrue(group.isSynced());
        group.addMember(userManager.getUser(
                TestKeyProvider.getInstance().getUser2ID()));
        assertFalse(group.isSynced());
        try {
            groupManager.updateGroup(group);
            assertTrue(group.isSynced());
        } finally {
            group.removeMember(userManager.getUser(
                    TestKeyProvider.getInstance().getUser2ID()));
            assertFalse(group.isSynced());
            groupManager.updateGroup(group);
        }
        assertTrue(group.isSynced());
    }
}

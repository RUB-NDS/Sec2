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

package org.sec2.managers;

import java.util.ArrayList;
import junit.framework.TestCase;

import org.sec2.managers.beans.Group;
import org.sec2.managers.beans.User;

/**
 * Test class for Group.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * May 18, 2013
 */
public final class GroupTests extends TestCase {

    private User owner, member;

     /**
     * Set up: create two Users for owner and group member.
     * (Called before every test case method.)
     */
    @Override
    public void setUp() {
        owner  = new User(new byte[] {0x01}, "owner@sec2.org");
        member = new User(new byte[] {0x02}, "member@sec2.org");
    }

    /**
     * Tear down: delete owner and group member.
     * (Called after every test case method.)
     */
    @Override
    public void tearDown() {
        owner = member = null;
    }

    /**
     * Tests creation of a new Group.
     */
    public void testConstructor() {
        Group testGroup;

        //Null-String as name
        try {
            testGroup = new Group(null, owner, new ArrayList<byte[]>());
            testGroup.hashCode();
            fail("Should have raised a NullPointerException");
        } catch (NullPointerException expected) {
            testGroup = null;
        }
        assertNull(testGroup);

        //Null-Pointer as owner
        try {
            testGroup = new Group("Test", null, new ArrayList<byte[]>());
            testGroup.hashCode();
            fail("Should have raised a NullPointerException");
        } catch (NullPointerException expected) {
            testGroup = null;
        }
        assertNull(testGroup);

        //Null-Pointer as members
        try {
            testGroup = new Group("Test", owner, null);
            testGroup.hashCode();
            fail("Should have raised a NullPointerException");
        } catch (NullPointerException expected) {
            testGroup = null;
        }
        assertNull(testGroup);

        //Should work
        testGroup = new Group("Test", owner, new ArrayList<byte[]>());
        assertEquals(testGroup.getGroupName(), "Test");
        assertEquals(testGroup.getOwner(), owner);
    }

    /**
     * Tests setting a groupname.
     */
    public void testGroupName() {
        Group testGroup = new Group("Test", owner, new ArrayList<byte[]>());
        //Null-String as name
        try {
            testGroup.setGroupName(null);
            fail("Should have raised a NullPointerException");
        } catch (NullPointerException expected) {
            testGroup = null;
        }
        assertNull(testGroup);

        testGroup = new Group("Test", owner, new ArrayList<byte[]>());
        testGroup.setSynced(true);
        testGroup.setGroupName("TestGroup");
        assertEquals(testGroup.getGroupName(), "TestGroup");
        assertFalse(testGroup.isSynced());
    }

    /**
     * Tests adding and removing members.
     */
    public void testMemberManagement() {
        Group testGroup = new Group("Test", owner, new ArrayList<byte[]>());

        //Null-Pointer as member
        try {
            testGroup.addMember(null);
            fail("Should have raised a NullPointerException!");
        } catch (NullPointerException expected) {
            testGroup = null;
        }
        assertNull(testGroup);

        testGroup = new Group("Test", owner, new ArrayList<byte[]>());
        testGroup.setSynced(true);
        assertEquals(testGroup.getMembersINTERNAL().size(), 1);
        assertTrue(testGroup.getMembersINTERNAL().contains(owner.getUserID()));

        assertTrue(testGroup.addMember(member));
        assertTrue(testGroup.getMembersINTERNAL().contains(member.getUserID()));
        assertEquals(testGroup.getMembersINTERNAL().size(), 2);
        assertFalse(testGroup.isSynced());

        testGroup.setSynced(true);
        assertFalse(testGroup.addMember(owner));
        assertTrue(testGroup.isSynced());
        assertTrue(testGroup.removeMember(member));
        assertEquals(testGroup.getMembersINTERNAL().size(), 1);
        assertFalse(testGroup.isSynced());

        try {
            assertFalse(testGroup.removeMember(owner));
            fail("This should have thrown an IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            assertEquals(testGroup.getMembersINTERNAL().size(), 1);
        }
        assertTrue(testGroup.getMembersINTERNAL().contains(owner.getUserID()));
    }

}

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

package org.sec2.managers;

import org.sec2.managers.beans.User;

import junit.framework.TestCase;

/**
 * Test class for User.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * June 24, 2012
 */
public final class UserTests extends TestCase {

    /**
     * Tests creation of a new User.
     */
    public void testConstructor() {
        User testUser;
        
        //empty ID
        try {
            testUser = new User(new byte[]{}, "test@sec2.org");
            testUser.hashCode();
            fail("Should have raised an IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            testUser = null;
        }
        assertNull(testUser);

        //Null-String as mail address
        try {
            testUser = new User(new byte[]{0x01}, null);
            testUser.hashCode();
            fail("Should have raised a NullPointerException");
        } catch (NullPointerException expected) {
            testUser = null;
        }
        assertNull(testUser);

        //malformed mail address
        try {
            testUser = new User(new byte[]{0x01}, "test@sec2");
            testUser.hashCode();
            fail("Should have raised an IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            testUser = null;
        }
        assertNull(testUser);

        //should work
        testUser = new User(new byte[]{0x01}, "test@sec2.org");
        assertEquals(testUser.getUserID()[0], 0x01);
        assertEquals(testUser.getEmailAddress(), "test@sec2.org");
    }

    /**
     * Tests setting a mail address.
     */
    public void testEmailAddress() {
        User testUser = new User(new byte[]{0x01}, "test@sec2.org");

        //Null-String as mail address
        try {
            testUser.setEmailAddress(null);
            fail("Should have raised a NullPointerException");
        } catch (NullPointerException expected) {
            testUser = null;
        }
        assertNull(testUser);

        //malformed mail address
        testUser = new User(new byte[]{0x01}, "test@sec2.org");
        try {
            testUser.setEmailAddress("test@sec2");
            fail("Should have raised an IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            testUser = null;
        }
        assertNull(testUser);

        testUser = new User(new byte[]{0x01}, "test@sec2.org");
        testUser.setEmailAddress("test@mail.org");
        assertEquals(testUser.getEmailAddress(), "test@mail.org");
    }
}

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
import java.util.List;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import junit.framework.TestCase;
import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.managers.IUserManager;
import org.sec2.managers.beans.User;
import org.sec2.saml.client.SAMLClientBootstrap;

/**
 * Tests for the user manager.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 14, 2013
 */
public class UserManagerImplTests extends TestCase {

    /**
     * The manager to test.
     */
    private IUserManager userManager;

    /**
     * Bootstraps.
     *
     * @throws Exception whatever goes wrong during super.setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SAMLClientBootstrap.bootstrap();
        userManager = UserManagerImpl.getInstance();
    }

    /**
     * Tests getting all known users.
     */
    public void testGetKnownUsers() {
        List<byte[]> list = null;
        try {
            list = userManager.getKnownUsers();
        } catch (ExMiddlewareException e) {
            fail(e.toString());
        } catch (IOException e) {
            fail(e.toString());
        }
        assertNotNull(list);
    }

    /**
     * Tests getting all known users.
     */
    public void testGetRegisteredUser() {
        User u = userManager.getRegisteredUser();
        assertNotNull(u);
    }

    /**
     * Tests if user objects are identical across the application.
     */
    public void testUserEquality() {
        User u1 = userManager.getRegisteredUser();
        User u2 = null;
        User u3 = null;
        try {
            u2 = userManager.getUser(u1.getUserID());
            u3 = userManager.getUser(u2.getEmailAddress());
        } catch (ExMiddlewareException e) {
            fail(e.toString());
        } catch (IOException e) {
            fail(e.toString());
        }
        assertEquals(u1, u2);
        assertEquals(u2, u3);
        assertTrue(u1 == u2);
        assertTrue(u2 == u3);
    }
}

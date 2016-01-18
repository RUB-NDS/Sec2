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
package org.sec2.saml.client.connector;

import java.io.IOException;
import java.util.Arrays;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import junit.framework.TestCase;
import org.opensaml.xml.util.Base64;
import org.sec2.exceptions.EntityUnknownException;
import org.sec2.saml.client.engine.SecurityProviderConnectorFactory;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.sec2.saml.xml.GroupResponseType;
import org.sec2.saml.xml.UserResponseType;
import org.sec2.statictestdata.TestKeyProvider;

/**
 * Tests for the KeyserverConnector when the connection is good.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 13, 2013
 */
public class KeyserverConnectorGoodTests extends TestCase {

    /**
     * The connector to test.
     */
    private IKeyserverConnector connector;

    /**
     * Set up: Get connector.
     * (Called before every test case method.)
     */
    @Override
    public void setUp() {
        try {
            connector = KeyserverConnectorTestFactory.getKeyserverConnector(
                    MagicKey.good.getKey());
        } catch (SAMLEngineException e) {
            e.log();
            fail(e.toString());
        }
    }

    /**
     * Tear down: Delete reference to connector.
     * (Called after every test case method.)
     */
    @Override
    public void tearDown() {
        connector = null;
    }

    /**
     * Tests the RegisteredUserID.
     */
    public void testGetRegisteredUserID() {
        byte[] expectedID = null;
        byte[] actualID = null;
        try {
            expectedID = SecurityProviderConnectorFactory.
                    getSecurityProviderConnector().getCurrentUserID();
            actualID = connector.getRegisteredUserID();
        } catch (EntityUnknownException e) {
            fail(e.toString());
        }
        assertNotNull(expectedID);
        assertNotNull(actualID);
        assertTrue(expectedID.length > 0);
        assertTrue(actualID.length > 0);
        assertTrue(Arrays.equals(expectedID, actualID));
    }

    /**
     * Tests fetching a user by mail.
     */
    public void testGetUserByMail() {
        UserResponseType u = null;
        try {
            u = connector.getUser("user@sec2.org");
        } catch (SAMLEngineException e) {
            e.log();
            fail(e.toString());
        } catch (IOException e) {
            fail(e.toString());
        }
        assertNotNull(u);
        assertEquals("user@sec2.org", u.getEmailAddress().getValue());
    }

    /**
     * Tests fetching a user by ID.
     */
    public void testGetUserByID() {
        UserResponseType u = null;
        try {
            u = connector.getUser(TestKeyProvider.getInstance().getUserID());
        } catch (SAMLEngineException e) {
            e.log();
            fail(e.toString());
        } catch (IOException e) {
            fail(e.toString());
        }
        assertNotNull(u);
        assertEquals("user@sec2.org", u.getEmailAddress().getValue());
        assertTrue(Arrays.equals(TestKeyProvider.getInstance().getUserID(),
                Base64.decode(u.getUserID().getValue())));
    }

    /**
     * Tests getting a group.
     */
    public void testGetGroup() {
        GroupResponseType g = null;
        try {
            g = connector.getGroup("user@sec2.org");
        } catch (SAMLEngineException e) {
            e.log();
            fail(e.toString());
        } catch (IOException e) {
            fail(e.toString());
        }
        assertNotNull(g);
        assertEquals("user@sec2.org", g.getGroupName().getValue());
    }
}

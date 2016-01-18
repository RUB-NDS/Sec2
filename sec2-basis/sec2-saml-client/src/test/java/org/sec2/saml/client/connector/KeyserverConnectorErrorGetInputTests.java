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
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import junit.framework.TestCase;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.sec2.statictestdata.TestKeyProvider;

/**
 * Tests for the KeyserverConnector when the connection returns no input.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 13, 2013
 */
public class KeyserverConnectorErrorGetInputTests extends TestCase {

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
                    MagicKey.errorGetInput.getKey());
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
     * Tests fetching a user by mail.
     */
    public void testGetUserByMail() {
        try {
            connector.getUser("user@sec2.org");
            fail("Should have failed with a SAMLEngineException "
                    + "(IOException as cause)");
        } catch (SAMLEngineException e) {
            assertNotNull(e);
            e.log();
            fail("Should have failed with an IOException, not with a "
                    + "SAMLEngineException");
        } catch (IOException e) {
            assertNotNull(e);
            assertTrue(e.getMessage().contains(
                    "Simulated error on getting InputStream"));
        }
    }

    /**
     * Tests fetching a user by ID.
     */
    public void testGetUserByID() {
        try {
            connector.getUser(TestKeyProvider.getInstance().getUserID());
            fail("Should have failed with a SAMLEngineException "
                    + "(IOException as cause)");
        } catch (SAMLEngineException e) {
            assertNotNull(e);
            e.log();
            fail("Should have failed with an IOException, not with a "
                    + "SAMLEngineException");
        } catch (IOException e) {
            assertNotNull(e);
            assertTrue(e.getMessage().contains(
                    "Simulated error on getting InputStream"));
        }
    }

    /**
     * Tests getting a group.
     */
    public void testGetGroup() {
        try {
            connector.getGroup("user@sec2.org");
            fail("Should have failed with a SAMLEngineException "
                    + "(IOException as cause)");
        } catch (SAMLEngineException e) {
            assertNotNull(e);
            e.log();
            fail("Should have failed with an IOException, not with a "
                    + "SAMLEngineException");
        } catch (IOException e) {
            assertNotNull(e);
            assertTrue(e.getMessage().contains(
                    "Simulated error on getting InputStream"));
        }
    }
}

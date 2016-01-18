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
package org.sec2.saml.client.engine;

import junit.framework.TestCase;
import org.sec2.saml.exceptions.SAMLEngineException;

/**
 * Tests for ClientSAMLEngine.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * Oktober 26, 2012
 */
public final class ClientSAMLEngineTests extends TestCase {

    /**
     * The ClientSAMLEngine which is tested.
     */
    private ClientSAMLEngine testEngine;

    /**
     * Set up: Get SAML Engine.
     * (Called before every test case method.)
     */
    @Override
    public void setUp() {
        try {
            testEngine = ClientSAMLEngine.getInstance();
        } catch (SAMLEngineException e) {
            e.log();
            fail(e.getMessage());
        }
    }

    /**
     * Tear down: Delete reference to SAML Engine.
     * (Called after every test case method.)
     */
    @Override
    public void tearDown() {
        testEngine = null;
    }

    public void testBla() {
        assertTrue(true);
    }
}

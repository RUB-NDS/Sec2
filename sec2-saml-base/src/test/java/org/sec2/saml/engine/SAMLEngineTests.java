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
package org.sec2.saml.engine;

import junit.framework.TestCase;
import org.sec2.saml.engine.mockups.MockupSAMLEngine;
import org.sec2.saml.exceptions.SAMLEngineException;

/**
 * Tests for the basic SAML engine.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * December 05, 2012
 */
public final class SAMLEngineTests extends TestCase {

    /**
     * The SAML Engine which is tested.
     */
    private SAMLEngine testEngine;

    /**
     * Set up: Get SAML Engine.
     */
    @Override
    public void setUp() {
        try {
            testEngine = MockupSAMLEngine.getInstance();
        } catch (SAMLEngineException e) {
            e.log();
            fail(e.toString());
        }
    }

    /**
     * Tear down: Delete reference to SAML Engine.
     */
    @Override
    public void tearDown() {
        testEngine = null;
    }

    /**
     * Tests that the root certificate cannot be overwritten.
     */
    public void testOverwriteRootCertificate() {
        try {
            testEngine.setTrustedRootCertificate(null);
            fail("Overwriting the root certificate with null "
                    + "should be impossible");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        } catch (SAMLEngineException e) {
            e.log();
            fail("Overwriting the root certificate with null "
                    + "should raise an IllegalArgumentException, not a "
                    + "SAMLEngineException");
        }
        try {
            testEngine.setTrustedRootCertificate(
                    testEngine.getTrustedRootCertificate());
            fail("Overwriting the root certificate "
                    + "should be impossible");
        } catch (SAMLEngineException e) {
            e.log();
            assertNotNull(e);
        }
    }
}

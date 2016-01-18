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
package org.sec2.saml.engine;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import junit.framework.TestCase;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.xml.io.MarshallingException;
import org.sec2.saml.XMLProcessingTestHelper;
import org.sec2.saml.engine.mockups.MockupSAMLEngine;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.sec2.saml.exceptions.SignatureEngineException;

/**
 * Tests for {@link SignatureEngine}.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 23, 2013
 */
public class SignatureEngineTests extends TestCase {

    /**
     * The engine to test.
     */
    private SignatureEngine engine;

    /**
     * Create processor and SAMLEngine.
     */
    @Override
    public void setUp() {
        try {
            engine = MockupSAMLEngine.getInstance().getSignatureEngine();
        } catch (SAMLEngineException e) {
            e.log();
            fail(e.toString());
        }
    }

    /**
     * Tests that an invalid signature is detected as such.
     */
    public void testInvalidSignatureChangedKeyIdentifier() {
        Assertion a = null;
        try {
            a = SAMLEngine.getXMLObject(Assertion.class);
            engine.signXMLObject(a);
            String xml = XMLHelper.getXMLString(a);
            // manipulate XML: change key identifier
            xml = xml.replace("7Z6zXQFZrg", "1234567890");
            a = XMLProcessingTestHelper.parseXMLElement(xml, Assertion.class);
        } catch (SAMLEngineException e) {
            e.log();
            fail(e.toString());
        } catch (MarshallingException e) {
            fail(e.toString());
        }
        try {
            engine.validate(a);
            fail("Should have failed with a SignatureEngineException. Did you "
                    + "update the test keys? If yes, update this test.");
        } catch (SignatureEngineException e) {
            assertTrue(e.getMessage().contains("failed"));
        }
    }

    /**
     * Tests that an invalid signature is detected as such.
     */
    public void testInvalidSignatureXMLModified() {
        Assertion a = null;
        try {
            a = SAMLEngine.getXMLObject(Assertion.class);
            engine.signXMLObject(a);
            String xml = XMLHelper.getXMLString(a);
            // manipulate XML: change key identifier
            xml = xml.replace("Version=\"2.0\">", "Version=\"2.0\" "
                    + "InResponseTo=\"bla\">");
            a = XMLProcessingTestHelper.parseXMLElement(xml, Assertion.class);
        } catch (SAMLEngineException e) {
            e.log();
            fail(e.toString());
        } catch (MarshallingException e) {
            fail(e.toString());
        }
        try {
            engine.validate(a);
            fail("Should have failed with a SignatureEngineException");
        } catch (SignatureEngineException e) {
            assertTrue(e.getMessage().contains("failed"));
        }
    }
}

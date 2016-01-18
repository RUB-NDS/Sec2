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
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.validation.ValidationException;
import org.sec2.saml.XMLProcessingTestHelper;
import org.sec2.saml.engine.mockups.MockupSAMLEngine;
import org.sec2.saml.exceptions.SAMLEngineException;

/**
 * Tests for the sec2 specific signature profile.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * December 05, 2012
 */
public class Sec2SignatureProfileValidatorTests extends TestCase {

    /**
     * Tests that a signature that signs a subtree is invalid.
     * Creates a Response with a nested assertion. The assertion is signed, but
     * not the Response (which is root). This is valid SAML, but not for sec2.
     */
    public void testNonRootSignature() {
        Response parsedReponse = null;
        try {
            SAMLEngine engine = MockupSAMLEngine.getInstance();
            Assertion a = SAMLEngine.getXMLObject(Assertion.class);
            a.setID("testid"); //necessary, because OpenSAML would use
                               //<ds:Reference URI=""> without an id. That
                               //would mean, that the root element is referenced
            engine.getSignatureEngine().signXMLObject(a);
            Response r = SAMLEngine.getXMLObject(Response.class);
            r.getAssertions().add(a);
            String xml = XMLHelper.getXMLString(r);
            parsedReponse = XMLProcessingTestHelper.
                    parseXMLElement(xml, Response.class);
        } catch (SAMLEngineException e) {
            e.log();
            fail(e.toString());
        } catch (MarshallingException e) {
            fail(e.toString());
        }

        Sec2SignatureProfileValidator validator =
                new Sec2SignatureProfileValidator();
        try {
            validator.validate(parsedReponse.getAssertions().get(0).
                    getSignature());
            fail("Should have failed with a ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getLocalizedMessage().contains("root element"));
        }
    }
}

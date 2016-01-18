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
package org.sec2.frontend.samlengine;

import java.lang.reflect.Field;
import static junit.framework.Assert.fail;
import junit.framework.TestCase;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.validation.ValidationException;
import org.sec2.frontend.SAMLBinding;
import org.sec2.frontend.XMLProcessingTestHelper;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.engine.XMLHelper;
import org.sec2.saml.exceptions.SAMLEngineException;

/**
 * Tests the signature processing on the keyserver.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 1, 2013
 */
public class KeyserverSignatureEngineTests extends TestCase {

    /**
     * The engine to test.
     */
    private KeyserverSignatureEngine engine;

    /**
     * Create processor and SAMLEngine.
     */
    @Override
    public void setUp() {
        try {
            engine = KeyserverSAMLEngine.getInstance().getSignatureEngine();
            SAMLBinding.getInstance(); //Sets the crypto providers
        } catch (SAMLEngineException e) {
            e.log();
            fail(e.toString());
        }
    }

    /**
     * Tests, that the real key is used for a signature, not the dummy key.
     */
    public void testCorrectKeyUsage() {
        Assertion a = null;
        try {
            a = SAMLEngine.getXMLObject(Assertion.class);
            engine.signXMLObject(a);
            String xml = XMLHelper.getXMLString(a);
            a = XMLProcessingTestHelper.parseXMLElement(xml, Assertion.class);
        } catch (SAMLEngineException e) {
            e.log();
            fail(e.toString());
        } catch (MarshallingException e) {
            fail(e.toString());
        }

        // Dirty access to a protected attribute... but it's only a test
        Credential c = null;
        Class clazz = null;
        try {
            clazz = engine.getClass().getSuperclass();
            Field f = clazz.getDeclaredField("signCredential");
            f.setAccessible(true);
            c = (Credential) f.get(engine);
        } catch (IllegalAccessException e) {
            fail("Field 'signCredential' of "
                    + "class " + clazz.getName()
                    + " is not accessible.");
        } catch (NoSuchFieldException e) {
            fail("Class " + clazz.getName() + " has no field "
                    + "'signCredential'");
        }

        SignatureValidator sigValidator = new SignatureValidator(c);
        try {
            sigValidator.validate(a.getSignature());
            fail("Dummy credential was used to create signature.");
        } catch (ValidationException e) {
            assertNotNull(e); //all good
        }
    }

    /**
     * Delete references.
     */
    @Override
    public void tearDown() {
        engine = null;
    }
}

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
package org.sec2.frontend.samlengine;

import static junit.framework.Assert.fail;
import junit.framework.TestCase;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SAMLObjectContentReference;
import org.opensaml.saml2.core.*;
import org.opensaml.xml.encryption.EncryptedData;
import org.opensaml.xml.encryption.EncryptedKey;
import org.opensaml.xml.encryption.EncryptionConstants;
import org.opensaml.xml.encryption.EncryptionMethod;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.KeyName;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.sec2.frontend.KeyserverFrontendConfig;
import org.sec2.frontend.exceptions.KeyserverSecurityException;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.engine.XMLHelper;
import org.slf4j.MDC;

/**
 * Tests non cryptographic properties of requests.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 20, 2012
 */
public class KeyserverRequestVerifierTests extends TestCase {

    /**
     * The verifier that is tested.
     */
    private KeyserverRequestVerifier verifier;

    /**
     * The AttributeQuery that is used for testing.
     */
    private AttributeQuery query;

    /**
     * The destination of the AttributeQuery.
     */
    private static final String DESTINATION = "http://localhost/";

    /**
     * Some random value used as ID.
     */
    private static final String ID = "f0263f9687c7a6a0";

    /**
     * The User's ID.
     */
    private static final String ISSUER =
            "BzkzKgElHV";

    /**
     * A timestamp that is no more valid.
     */
    private static final DateTime OUTDATED_TS = new DateTime().minusYears(10);

    /**
     * A timestamp that is not yet valid.
     */
    private static final DateTime FUTURE_TS = new DateTime().plusYears(10);

    /**
     * Create verifier and query.
     */
    @Override
    public void setUp() {
        verifier = new KeyserverRequestVerifier(
                KeyserverFrontendConfig.ALLOWED_TIMESTAMP_OFFSET);
        query = SAMLEngine.getSAMLBuilder(AttributeQuery.class,
                AttributeQuery.DEFAULT_ELEMENT_NAME).buildObject();
        query.setDestination(DESTINATION);
        query.setID(ID);
        query.setIssueInstant(new DateTime());
        query.setVersion(SAMLVersion.VERSION_20);
        query.setIssuer(SAMLEngine.getSAMLBuilder(Issuer.class,
                Issuer.DEFAULT_ELEMENT_NAME).buildObject());
        query.getIssuer().setValue(ISSUER);
        query.getAttributes().add(SAMLEngine.getSAMLBuilder(
                Attribute.class, Attribute.DEFAULT_ELEMENT_NAME).buildObject());
        query.getAttributes().get(0).getAttributeValues().add(SAMLEngine.
                getXMLBuilder(XSAny.class, XSAny.TYPE_NAME).
                buildObject(AttributeValue.DEFAULT_ELEMENT_NAME));
        ((XSAny) query.getAttributes().get(0).getAttributeValues().get(0)).
                getUnknownXMLObjects().add(SAMLEngine.getSAMLBuilder(
                EncryptedAttribute.class,
                EncryptedAttribute.DEFAULT_ELEMENT_NAME).buildObject());
        EncryptedAttribute encAttrib = (EncryptedAttribute) ((XSAny)
                query.getAttributes().get(0).getAttributeValues().get(0)).
                getUnknownXMLObjects().get(0);
        encAttrib.setEncryptedData(SAMLEngine.getXMLBuilder(EncryptedData.class,
                EncryptedData.DEFAULT_ELEMENT_NAME).
                buildObject(EncryptedData.DEFAULT_ELEMENT_NAME));
        encAttrib.getEncryptedData().setEncryptionMethod(SAMLEngine.
                getXMLBuilder(EncryptionMethod.class,
                EncryptionMethod.DEFAULT_ELEMENT_NAME).
                buildObject(EncryptionMethod.DEFAULT_ELEMENT_NAME));
        encAttrib.getEncryptedData().getEncryptionMethod().setAlgorithm(
                EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128_GCM);
        encAttrib.getEncryptedKeys().add(SAMLEngine.getXMLBuilder(
                EncryptedKey.class, EncryptedKey.DEFAULT_ELEMENT_NAME).
                buildObject(EncryptedKey.DEFAULT_ELEMENT_NAME));
        encAttrib.getEncryptedKeys().get(0).setEncryptionMethod(SAMLEngine.
                getXMLBuilder(EncryptionMethod.class,
                EncryptionMethod.DEFAULT_ELEMENT_NAME).
                buildObject(EncryptionMethod.DEFAULT_ELEMENT_NAME));
        encAttrib.getEncryptedKeys().get(0).getEncryptionMethod().setAlgorithm(
                EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP);
        query.setSignature(SAMLEngine.getXMLBuilder(Signature.class,
                Signature.DEFAULT_ELEMENT_NAME).
                buildObject(Signature.DEFAULT_ELEMENT_NAME));
        query.getSignature().setSignatureAlgorithm(
                SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
        query.getSignature().getContentReferences().add(
                new SAMLObjectContentReference(query));
        query.getSignature().setKeyInfo(SAMLEngine.getXMLBuilder(KeyInfo.class,
                KeyInfo.DEFAULT_ELEMENT_NAME).
                buildObject(KeyInfo.DEFAULT_ELEMENT_NAME));
        query.getSignature().getKeyInfo().getKeyNames().add(SAMLEngine.
                getXMLBuilder(KeyName.class, KeyName.DEFAULT_ELEMENT_NAME).
                buildObject(KeyName.DEFAULT_ELEMENT_NAME));
        query.getSignature().getKeyInfo().getKeyNames().get(0).setValue(ISSUER);
        updateDOM();
    }

    /**
     * Delete references to verifier and query.
     */
    @Override
    public void tearDown() {
        verifier = null;
        query = null;
        MDC.remove(KeyserverFrontendConfig.MDC_KEY_USER);
        MDC.remove(KeyserverFrontendConfig.MDC_KEY_REQUEST);
    }

    /**
     * Tests that the keyserver allows a correct message.
     */
    public void testCorrectRequest() {
        try {
            verifier.verify(query, DESTINATION);
        } catch (KeyserverSecurityException e) {
            fail("Should not have failed with " + e.toString());
        }
        assertEquals(verifier.getAllowedTimestampDifferenceInSeconds(),
                KeyserverFrontendConfig.ALLOWED_TIMESTAMP_OFFSET);
    }

    /**
     * Tests that a KeyserverRequestVerifier cannot be created with a negative
     * timestamp offset.
     */
    public void testNegativeTimestampOffset() {
        try {
            verifier = new KeyserverRequestVerifier(-1);
            fail("Should have failed with an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    /**
     * Tests that the keyserver denies a message without an issuer.
     */
    public void testNoIssuer() {
        query.setIssuer(null);
        try {
            verifier.verify(query, DESTINATION);
            fail("Should have failed with a KeyserverSecurityException");
        } catch (KeyserverSecurityException e) {
            assertTrue(e.getMessage().contains("Request has no Issuer"));
        }
    }

    /**
     * Tests that the keyserver denies a message without a KeyInfo.
     */
    public void testNoKeyInfo() {
        query.getSignature().setKeyInfo(null);
        try {
            verifier.verify(query, DESTINATION);
            fail("Should have failed with a KeyserverSecurityException");
        } catch (KeyserverSecurityException e) {
            assertTrue(e.getMessage().contains("Request has no KeyInfo"));
        }
    }

    /**
     * Tests that the keyserver denies a message with more than one KeyName.
     */
    public void testMultipleKeyNames() {
        query.getSignature().getKeyInfo().getKeyNames().add(SAMLEngine.
                getXMLBuilder(KeyName.class, KeyName.DEFAULT_ELEMENT_NAME).
                buildObject(KeyName.DEFAULT_ELEMENT_NAME));
        query.getSignature().getKeyInfo().getKeyNames().get(0).setValue(
                "anotherKeyName");
        try {
            verifier.verify(query, DESTINATION);
            fail("Should have failed with a KeyserverSecurityException");
        } catch (KeyserverSecurityException e) {
            assertTrue(e.getMessage().contains("Request has 2 KeyNames in its "
                    + "KeyInfo"));
        }
    }

    /**
     * Tests that the keyserver denies a message with a wrong issuer.
     */
    public void testWrongIssuer() {
        query.getIssuer().setValue("RogueIssuer");
        try {
            verifier.verify(query, DESTINATION);
            fail("Should have failed with a KeyserverSecurityException");
        } catch (KeyserverSecurityException e) {
            assertTrue(e.getMessage().contains("KeyName and Issuer are not "
                    + "equal"));
        }
    }

    /**
     * Tests that the keyserver denies a message without an ID.
     */
    public void testNoID() {
        query.setID(null);
        try {
            verifier.verify(query, DESTINATION);
            fail("Should have failed with a KeyserverSecurityException");
        } catch (KeyserverSecurityException e) {
            assertTrue(e.getMessage().contains("Request has no ID"));
        }
    }

    /**
     * Tests that the keyserver denies a message without a version.
     */
    public void testNoVersion() {
        query.setVersion(null);
        try {
            verifier.verify(query, DESTINATION);
            fail("Should have failed with a KeyserverSecurityException");
        } catch (KeyserverSecurityException e) {
            assertTrue(e.getMessage().contains("Request's SAML version is not "
                    + "2.0"));
        }
    }

    /**
     * Tests that the keyserver denies a message with a wrong version.
     */
    public void testWrongVersion() {
        query.setVersion(SAMLVersion.VERSION_11);
        try {
            verifier.verify(query, DESTINATION);
            fail("Should have failed with a KeyserverSecurityException");
        } catch (KeyserverSecurityException e) {
            assertTrue(e.getMessage().contains("Request's SAML version is not "
                    + "2.0"));
        }
    }

    /**
     * Tests that the keyserver denies a message without a destination.
     */
    public void testNoDestination() {
        query.setDestination(null);
        try {
            verifier.verify(query, DESTINATION);
            fail("Should have failed with a KeyserverSecurityException");
        } catch (KeyserverSecurityException e) {
            assertTrue(e.getMessage().contains("Request's destination is "
                    + "unknown"));
        }
    }

    /**
     * Tests that the keyserver throws an exception if no URL is provided.
     */
    public void testNoURL() {
        try {
            verifier.verify(query, null);
            fail("Should have failed with a IllegalArgumentException");
        } catch (KeyserverSecurityException e) {
            fail("Should have failed with a IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Request's intended destination "
                    + "must not be null"));
        }
    }

    /**
     * Tests that the keyserver denies a message with a wrong destination.
     */
    public void testWrongDestination() {
        try {
            verifier.verify(query, "http://sec2.org/");
            fail("Should have failed with a KeyserverSecurityException");
        } catch (KeyserverSecurityException e) {
            assertTrue(e.getMessage().contains("Request's destination is not "
                    + "the URL it was sent to"));
        }
    }

    /**
     * Tests that the keyserver allows a message that has a not string equal,
     * but URI equal destination.
     */
    public void testCorrectDestination() {
        query.setDestination("HTTP://localhost/"); // protocol upper case
        try {
            verifier.verify(query, DESTINATION);
        } catch (KeyserverSecurityException e) {
            fail("Should not have failed with " + e.toString());
        }
    }

    /**
     * Tests that a http-URI with the explicit port 80 is equal to a
     * missing port URI.
     */
    public void testCorrectDestinationWithPortAndPath() {
        query.setDestination("http://localhost:80/keyserver/");
        try {
            verifier.verify(query, "http://localhost/keyserver/");
        } catch (KeyserverSecurityException e) {
            fail("Should not have failed with " + e.toString());
        }
    }

    /**
     * Tests that the keyserver denies a message without a timestamp.
     */
    public void testNoTimestamp() {
        query.setIssueInstant(null);
        try {
            verifier.verify(query, DESTINATION);
            fail("Should have failed with a KeyserverSecurityException");
        } catch (KeyserverSecurityException e) {
            assertTrue(e.getMessage().contains("Request has no IssueInstant-"
                    + "timestamp"));
        }
    }

    /**
     * Tests that the keyserver denies a message with an outdated timestamp.
     */
    public void testOutdatedTimestamp() {
        query.setIssueInstant(OUTDATED_TS);
        try {
            verifier.verify(query, DESTINATION);
            fail("Should have failed with a KeyserverSecurityException");
        } catch (KeyserverSecurityException e) {
            assertTrue(e.getMessage().contains("Request is outdated"));
        }
    }

    /**
     * Tests that the keyserver denies a message with a not yet valid timestamp.
     */
    public void testFutureTimestamp() {
        query.setIssueInstant(FUTURE_TS);
        try {
            verifier.verify(query, DESTINATION);
            fail("Should have failed with a KeyserverSecurityException");
        } catch (KeyserverSecurityException e) {
            assertTrue(e.getMessage().contains("Request is not yet valid"));
        }
    }

    /**
     * Tests that the keyserver denies a message without an attribute.
     */
    public void testNoAttribute() {
        query.getAttributes().clear();
        try {
            verifier.verify(query, DESTINATION);
            fail("Should have failed with a KeyserverSecurityException");
        } catch (KeyserverSecurityException e) {
            assertTrue(e.getMessage().contains("Request contains 0 "
                    + "attributes"));
        }
    }

    /**
     * Tests that the keyserver denies a message without an AttributeValue.
     */
    public void testNoAttributeValue() {
        query.getAttributes().get(0).getAttributeValues().clear();
        try {
            verifier.verify(query, DESTINATION);
            fail("Should have failed with a KeyserverSecurityException");
        } catch (KeyserverSecurityException e) {
            assertTrue(e.getMessage().contains("Attribute contains 0 "
                    + "AttributeValues"));
        }
    }

    /**
     * Tests that the keyserver denies a message with a wrong AttributeValue.
     */
    public void testWrongAttributeValue() {
        query.getAttributes().get(0).getAttributeValues().clear();
        query.getAttributes().get(0).getAttributeValues().add(SAMLEngine.
                getXMLBuilder(XSAny.class,
                Attribute.DEFAULT_ELEMENT_NAME).
                buildObject(Attribute.DEFAULT_ELEMENT_NAME));
        try {
            verifier.verify(query, DESTINATION);
            fail("Should have failed with a KeyserverSecurityException");
        } catch (KeyserverSecurityException e) {
            assertTrue(e.getMessage().contains(
                    "Attribute element instead of an AttributeValue (XSAny)"));
        }
    }

    /**
     * Tests that the keyserver denies a message without an attribute.
     */
    public void testMultipleAttributeValueChildren() {
        ((XSAny) query.getAttributes().get(0).getAttributeValues().get(0)).
                getUnknownXMLObjects().add(SAMLEngine.getSAMLBuilder(
                EncryptedAttribute.class,
                EncryptedAttribute.DEFAULT_ELEMENT_NAME).buildObject());
        try {
            verifier.verify(query, DESTINATION);
            fail("Should have failed with a KeyserverSecurityException");
        } catch (KeyserverSecurityException e) {
            assertTrue(e.getMessage().contains(
                    "AttributeValue contains 2 children"));
        }
    }

    /**
     * Tests that the keyserver denies a message without an EncryptedAttribute.
     */
    public void testWrongAttributeValueChild() {
        ((XSAny) query.getAttributes().get(0).getAttributeValues().get(0)).
                getUnknownXMLObjects().clear();
        ((XSAny) query.getAttributes().get(0).getAttributeValues().get(0)).
                getUnknownXMLObjects().add(SAMLEngine.getSAMLBuilder(
                Attribute.class,
                Attribute.DEFAULT_ELEMENT_NAME).buildObject());
        try {
            verifier.verify(query, DESTINATION);
            fail("Should have failed with a KeyserverSecurityException");
        } catch (KeyserverSecurityException e) {
            assertTrue(e.getMessage().contains(
                    "Attribute element instead of an EncryptedAttribute"));
        }
    }

    /**
     * Tests that the keyserver denies a message using CBC mode.
     */
    public void testForbiddenEncryptionAlgorithm() {
        ((EncryptedAttribute) ((XSAny) query.getAttributes().get(0).
                getAttributeValues().get(0)).getUnknownXMLObjects().get(0)).
                getEncryptedData().getEncryptionMethod().
                setAlgorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128);
        updateDOM();
        try {
            verifier.verify(query, DESTINATION);
            fail("Should have failed with a KeyserverSecurityException");
        } catch (KeyserverSecurityException e) {
            assertTrue(e.getMessage().contains("GCM-enabled algorithm"));
        }
    }

    /**
     * Tests that the keyserver denies a message using RSAwithSHA1.
     */
    public void testForbiddenSignatureAlgorithm() {
        query.getSignature().setSignatureAlgorithm(
                SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
        updateDOM();
        try {
            verifier.verify(query, DESTINATION);
            fail("Should have failed with a KeyserverSecurityException");
        } catch (KeyserverSecurityException e) {
            assertTrue(e.getMessage().contains("RSA and a SHA2-family-hash"));
        }
    }

    /**
     * Tests that the keyserver denies a message using MD5.
     */
    public void testForbiddenDigestAlgorithm() {
        ((SAMLObjectContentReference) query.getSignature().
                getContentReferences().get(0)).setDigestAlgorithm(
                SignatureConstants.ALGO_ID_DIGEST_NOT_RECOMMENDED_MD5);
        try {
            verifier.verify(query, DESTINATION);
            fail("Should have failed with a KeyserverSecurityException");
        } catch (KeyserverSecurityException e) {
            assertTrue(e.getMessage().contains("SHA2-family-hash"));
        }
    }

    /**
     * Tests that the keyserver denies an encrypted key using PKCS#1.5.
     */
    public void testForbiddenKeyTransportAlgorithm() {
        ((EncryptedAttribute) ((XSAny) query.getAttributes().get(0).
                getAttributeValues().get(0)).getUnknownXMLObjects().get(0)).
                getEncryptedKeys().get(0).getEncryptionMethod().setAlgorithm(
                EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSA15);
        updateDOM();
        try {
            verifier.verify(query, DESTINATION);
            fail("Should have failed with a KeyserverSecurityException");
        } catch (KeyserverSecurityException e) {
            assertTrue(e.getMessage().contains("RSA-OAEP-MGF1P"));
        }
    }

    /**
     * marshalls & updates the DOM.
     */
    private void updateDOM() {
        try {
            XMLHelper.getXMLString(query);
        } catch (MarshallingException e) {
            fail(e.toString());
        }
    }
}

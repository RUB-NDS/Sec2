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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import org.opensaml.Configuration;
import org.opensaml.saml2.core.*;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.util.Base64;
import org.sec2.frontend.BackendHolder;
import org.sec2.frontend.KeyserverFrontendConfig;
import org.sec2.frontend.exceptions.XMLProcessException;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.engine.XMLHelper;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.sec2.saml.xml.Sec2ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * The SAML engine that is specialized to the needs of the keyserver.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 07, 2012
 */
public final class KeyserverSAMLEngine extends SAMLEngine {

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(
            KeyserverSAMLEngine.class);

    /**
     * Signature engine that supports typical XML signature operations.
     */
    private final KeyserverSignatureEngine signatureEngine;

    /**
     * Cipher engine that supports typical XML encryption operations.
     */
    private final KeyserverCipherEngine cipherEngine;

    /**
     * Generator for prepared SAML elements.
     */
    private final KeyserverPreparedElementGenerator prepElementGenerator;

    /**
     * Verifier that checks whether an AttributeQuery meets all requirements
     * to allow further processing.
     */
    private final KeyserverRequestVerifier requestVerifier;

    /**
     * The ID of the keyserver.
     */
    private final byte[] keyserverID;

    /**
     * The ID of the keyserver in Base64 encoding.
     */
    private final String keyserverIDBase64;

    /**
     * Singleton constructor.
     * @throws SAMLEngineException if the registered user cannot be
     *          determined or if the root certificate cannot be set
     */
    private KeyserverSAMLEngine() throws SAMLEngineException {
        super();

        X509Certificate cert = BackendHolder.getBackend().
                getServerInfo().getSignaturePKC();
        //TODO: check the certificate
        this.setTrustedRootCertificate(cert);

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(
                    KeyserverFrontendConfig.DIGEST_ALGORITHM);
        } catch (final NoSuchAlgorithmException e) {
            logger.error("No MessageDigest for {} found, please check your "
                    + "security configuration",
                    KeyserverFrontendConfig.DIGEST_ALGORITHM);
            throw new SAMLEngineException(e);
        }
        keyserverID = digest.digest(cert.getPublicKey().getEncoded());
        keyserverIDBase64 = Base64.encodeBytes(keyserverID);

        requestVerifier = new KeyserverRequestVerifier(
                KeyserverFrontendConfig.ALLOWED_TIMESTAMP_OFFSET);

        try {
            signatureEngine =
                    new KeyserverSignatureEngine(
                            getTrustedRootCertificate(), this);
            cipherEngine =
                    new KeyserverCipherEngine(
                            getTrustedRootCertificate(), this);
            prepElementGenerator =
                    new KeyserverPreparedElementGenerator(cipherEngine);
        } catch (NoSuchAlgorithmException e) {
            throw new SAMLEngineException(e);
        }
    }

    /**
     * Singleton getter.
     * @return The singleton instance
     * @throws SAMLEngineException if the registered user cannot be
     *          determined or if the root certificate cannot be set
     */
    public static KeyserverSAMLEngine getInstance() throws SAMLEngineException {
        if (KeyserverSAMLEngineHolder.samlEngineException != null) {
            throw KeyserverSAMLEngineHolder.samlEngineException;
        }
        return KeyserverSAMLEngineHolder.instance;
    }

    /**
     * Parses an SAML-AttributeQuery from a W3C-DOM element source.
     *
     * @param root The W3C-DOM element to parse
     * @return the SAML-AttributeQuery
     * @throws XMLProcessException if the XML cannot be unmarshalled or
     *          it is no SAML-AttributeQuery
     */
    public AttributeQuery parseAttributeQuery(
            final Element root) throws XMLProcessException {
        AttributeQuery returnValue = null;
        UnmarshallerFactory unmarshallerFactory =
                Configuration.getUnmarshallerFactory();
        try {
            Unmarshaller unmarshaller =
                    unmarshallerFactory.getUnmarshaller(root);
            returnValue = (AttributeQuery) unmarshaller.unmarshall(root);
        } catch (UnmarshallingException e) {
            throw new XMLProcessException(
                    "Content of request is no valid SAML!", e);
        } catch (ClassCastException e) {
            throw new XMLProcessException(
                    "Content of request is no SAML AttributeQuery!", e);
        } catch (NullPointerException e) {
            throw new XMLProcessException(
                    "Content of request is no SAML AttributeQuery!", e);
        }
        return returnValue;
    }

    /**
     * Creates a Response with an Assertion containing an EncryptedAttribute
     * that holds the content provided, signs the request and returns the
     * serialized XML. This is the main function of the
     * KeyserverSAMLEngine; it puts the pieces together.
     *
     * @param content The content to transmit
     * @param clientID the recipient's ID
     * @param requestID the request's ID
     * @param key The key used to encrypt the data
     * @return the serialized XML together with the encryption key
     * @throws SAMLEngineException if something goes wrong
     *          (can't be more precise, look into the exception for details ;) )
     */
    public String createResponse(final Sec2ResponseMessage content,
            final String clientID, final String requestID, final Credential key)
            throws SAMLEngineException {
        Response response = this.getPreparedElementGenerator().
                buildBasicResponse(this.getEntityIDBase64(), clientID,
                requestID, StatusCode.SUCCESS_URI, null);
        Assertion assertion = this.getPreparedElementGenerator().
                buildBasicAssertion(this.getEntityIDBase64());
        response.getAssertions().add(assertion);
        assertion.getAttributeStatements().add(SAMLEngine.getXMLObject(
                AttributeStatement.class));

        Attribute attribute = this.getPreparedElementGenerator().
                buildAttributeWithEncryptedContent(content,
                Base64.decode(clientID), key).getFirst();

        assertion.getAttributeStatements().get(0).getAttributes().
                add(attribute);
        String xml;
        try {
            this.getSignatureEngine().signXMLObject(response);
            xml = XMLHelper.getXMLString(response);
        } catch (MarshallingException e) {
            throw new SAMLEngineException(e);
        }
        return xml;
    }

    /** {@inheritDoc} */
    @Override
    public KeyserverSignatureEngine getSignatureEngine() {
        return signatureEngine;
    }

    /** {@inheritDoc} */
    @Override
    public KeyserverCipherEngine getCipherEngine() {
        return cipherEngine;
    }

    /** {@inheritDoc} */
    @Override
    public KeyserverPreparedElementGenerator getPreparedElementGenerator() {
        return prepElementGenerator;
    }

    /** {@inheritDoc} */
    @Override
    public byte[] getEntityID() {
        return Arrays.copyOf(keyserverID, keyserverID.length);
    }

    /** {@inheritDoc} */
    @Override
    public String getEntityIDBase64() {
        return keyserverIDBase64;
    }

    /**
     * @return the requestVerifier
     */
    public KeyserverRequestVerifier getRequestVerifier() {
        return requestVerifier;
    }

    /**
     * Nested class holding Singleton instance.
     */
    private static class KeyserverSAMLEngineHolder {
        /**
         * The singleton instance.
         */
        private static KeyserverSAMLEngine instance;

        /**
         * ConfigurationException that might have been thrown during
         * creation of KeyserverSAMLEngine instance.
         */
        private static SAMLEngineException samlEngineException;

        static {
            try {
                instance = new KeyserverSAMLEngine();
            } catch (SAMLEngineException e) {
                e.log();
                samlEngineException = e;
            }
        }
    }
 }

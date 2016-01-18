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

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import org.opensaml.Configuration;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeQuery;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.util.Pair;
import org.sec2.exceptions.EntityUnknownException;
import org.sec2.saml.client.SAMLClientConfig;
import org.sec2.saml.client.exceptions.SecurityProviderException;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.engine.XMLHelper;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.sec2.saml.xml.Sec2RequestMessage;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * The SAML engine that is specialized to the needs of the client.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * December 17, 2012
 */
public final class ClientSAMLEngine extends SAMLEngine {

    /**
     * The digest of the signature public key of the keyserver.
     */
    private final byte[] keyserverID;

    /**
     * The SecurityProviderConnector used to access the public keys on the
     * smartcard.
     */
    private final ISecurityProviderConnector connector;

    /**
     * Signature engine that supports typical XML signature operations.
     */
    private final ClientSignatureEngine signatureEngine;

    /**
     * Cipher engine that supports typical XML encryption operations.
     */
    private final ClientCipherEngine cipherEngine;

    /**
     * Generator for prepared SAML elements.
     */
    private final ClientPreparedElementGenerator prepElementGenerator;

    /**
     * Verifier that checks whether an Response meets all requirements
     * to allow further processing.
     */
    private final ClientResponseVerifier responseVerifier;

    /**
     * Singleton constructor.
     * @throws SAMLEngineException if the registered user cannot be
     *          determined or if the root certificate cannot be set
     */
    private ClientSAMLEngine() throws SAMLEngineException {
        super();
        // get SecurityProviderConnector;
        // makes sure the card knows the registered user
        try {
            connector = SecurityProviderConnectorFactory.
                    getSecurityProviderConnector();
        } catch (EntityUnknownException e) {
            LoggerFactory.getLogger(ClientSAMLEngine.class)
                    .debug("Current user is unknown", e);
            throw new SAMLEngineException(e);
        }
        // set root certificate if it validates
        this.setKeyserverCertificateAsRoot();

        responseVerifier = new ClientResponseVerifier(
                SAMLClientConfig.ALLOWED_TIMESTAMP_OFFSET);

        // create sub-engines
        try {
            signatureEngine =
                    new ClientSignatureEngine(
                            getTrustedRootCertificate(), this);
            cipherEngine =
                    new ClientCipherEngine(
                            getTrustedRootCertificate(), this);
            prepElementGenerator =
                    new ClientPreparedElementGenerator(cipherEngine);
            MessageDigest digest = MessageDigest.getInstance(
                    SAMLClientConfig.DIGEST_ALGORITHM);
            keyserverID = digest.digest(getTrustedRootCertificate().
                    getPublicKey().getEncoded());
        } catch (EntityUnknownException e) {
            throw new SAMLEngineException(e);
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
    public static ClientSAMLEngine getInstance() throws SAMLEngineException {
        if (ClientSAMLEngineHolder.samlEngineException != null) {
            throw ClientSAMLEngineHolder.samlEngineException;
        }
        return ClientSAMLEngineHolder.instance;
    }

    /**
     * Reads the keyserver's trusted signature public key from the token and
     * sets the corresponding certificate as root if it validates.
     *
     * @throws SAMLEngineException if something goes wrong
     *          (can't be more precise, look into the exception for details ;) )
     */
    private void setKeyserverCertificateAsRoot()
            throws SAMLEngineException {
        //    /** //TODO: check this
//     * The distinguished name of the certificate authority, default
//     * "Sec2".
//     */
//    public static final String CA_NAME = "C=DE,L=Aachen,O=Utimaco,"
//            + "CN=Sec2 Signature";
        try {
            // get trusted public key
            final PublicKey trustedKey =
                    connector.getKeyserverSignaturePublicKey();
            if (trustedKey == null) {
                throw new SAMLEngineException(
                        "Trusted keyserver signing key is null");
            }
            // get untrusted certificate and verify it using the trusted key
            X509Certificate untrustedCert =
                    connector.getUntrustedKeyserverSignatureCertificate();
            untrustedCert.verify(trustedKey);
            //Check other attributes of the cert
            try {
                untrustedCert.checkValidity();
            } catch (CertificateExpiredException e) {
                throw new SAMLEngineException(e);
            } catch (CertificateNotYetValidException e) {
                throw new SAMLEngineException(e);
            }
            if (untrustedCert.getKeyUsage() == null) {
                throw new SAMLEngineException("Certificate has no specified "
                        + "KeyUsage");
            }
            // if we reach this point, we have a trusted certificate
            this.setTrustedRootCertificate(untrustedCert); //line is fun to read
        } catch (final SecurityProviderException e) {
            throw new SAMLEngineException(e);
        } catch (final GeneralSecurityException e) {
            throw new SAMLEngineException(e);
        } catch (final ClassCastException e) {
            throw new SAMLEngineException(e);
        }
    }

    /**
     * Creates an AttributeQuery with an EncryptedAttribute that holds the
     * content provided, signs the request and returns the signed XML
     * together with the encryption key. This is the main function of the
     * ClientSAMLEngine; it puts the pieces together.
     *
     * @param content The content to transmit
     * @return the signed XML together with the encryption key
     * @throws SAMLEngineException if something goes wrong
     *          (can't be more precise, look into the exception for details ;) )
     */
    public Pair<AttributeQuery, Credential> createAttributeQuery(
            final Sec2RequestMessage content) throws SAMLEngineException {
        Attribute attribute;
        Pair<Attribute, Credential> attributeKeyPair =
                this.getPreparedElementGenerator().
                buildAttributeWithEncryptedContent(content,
                this.getKeyserverID());
        attribute = attributeKeyPair.getFirst();
        AttributeQuery attributeQuery = this.getPreparedElementGenerator().
                buildBasicAttributeQuery(this.getEntityIDBase64());
        attributeQuery.getAttributes().add(attribute);
        try {
            this.getSignatureEngine().signXMLObject(attributeQuery);
        } catch (MarshallingException e) {
            throw new SAMLEngineException(e);
        }
        return new Pair<AttributeQuery, Credential>(attributeQuery,
                attributeKeyPair.getSecond());
    }

    /**
     * Parses a SAML-Response from a W3C-DOM element source.
     *
     * @param xml The W3C-DOM element to parse
     * @return the SAML-Response
     * @throws SAMLEngineException if the XML cannot be unmarshalled or
     *          it is no SAML-Response
     */
    public Response parseResponse(final String xml) throws SAMLEngineException {
        Response returnValue = null;
        Element root = XMLHelper.parseXMLElement(xml);
        UnmarshallerFactory unmarshallerFactory =
                Configuration.getUnmarshallerFactory();
        try {
            Unmarshaller unmarshaller =
                    unmarshallerFactory.getUnmarshaller(root);
            returnValue = (Response) unmarshaller.unmarshall(root);
        } catch (UnmarshallingException e) {
            throw new SAMLEngineException(
                    "Content is no valid SAML!", e);
        } catch (ClassCastException e) {
            throw new SAMLEngineException(
                    "Content is no SAML Response!", e);
        } catch (NullPointerException e) {
            throw new SAMLEngineException(
                    "Content is no SAML Response!", e);
        }
        return returnValue;
    }

    /** {@inheritDoc} */
    @Override
    public ClientSignatureEngine getSignatureEngine() {
        return signatureEngine;
    }

    /** {@inheritDoc} */
    @Override
    public ClientCipherEngine getCipherEngine() {
        return cipherEngine;
    }

    /** {@inheritDoc} */
    @Override
    public ClientPreparedElementGenerator getPreparedElementGenerator() {
        return prepElementGenerator;
    }

    /** {@inheritDoc} */
    @Override
    public byte[] getEntityID() {
        return connector.getCurrentUserID();
    }

    /** {@inheritDoc} */
    @Override
    public String getEntityIDBase64() {
        return connector.getCurrentUserIDBase64();
    }

    /**
     * @return the keyserverID
     */
    public byte[] getKeyserverID() {
        return Arrays.copyOf(keyserverID, keyserverID.length);
    }

    /**
     * @return the responseVerifier
     */
    public ClientResponseVerifier getResponseVerifier() {
        return responseVerifier;
    }

    /**
     * Nested class holding Singleton instance.
     */
    private static class ClientSAMLEngineHolder {
        /**
         * The singleton instance.
         */
        private static ClientSAMLEngine instance;

        /**
         * SAMLEngineException that might have been thrown during
         * creation of ClientSAMLEngine instance.
         */
        private static SAMLEngineException samlEngineException;

        static {
            try {
                instance = new ClientSAMLEngine();
            } catch (SAMLEngineException e) {
                e.log();
                samlEngineException = e;
            }
        }
    }
 }

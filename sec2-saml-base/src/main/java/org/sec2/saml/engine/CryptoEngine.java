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

import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.security.x509.*;
import org.sec2.saml.Sec2SAMLBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class that manages a trusted root certificate and validates
 * new certificates against this trust anchor.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 02, 2012
 */
public abstract class CryptoEngine {

    /*
     * make sure that everything gets bootstrapped
     */
    static {
        try {
            Sec2SAMLBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            throw new Error("A serious misconfiguration of OpenSAML was found",
                    e);
        }
    }

    /**
     * An x.509 certificate that is trusted.
     */
    private final X509Certificate trustedRootCertificate;

    /**
     * Trust engine that validates new certificates.
     */
    private final PKIXX509CredentialTrustEngine trustEngine;

    /**
     * SAMLEngine that provides information about the entity.
     */
    private final SAMLEngine samlEngine;

    /**
     * Constructor.
     * @param rootCert A trusted root certificate. Establishing trust to this
     *          certificate is out of scope of this class. The caller has to
     *          make sure that trust is given.
     * @param newSAMLEngine SAMLEngine that provides information about the
     *          entity.
     */
    public CryptoEngine(final X509Certificate rootCert,
            final SAMLEngine newSAMLEngine) {
        if (rootCert == null) {
            throw new IllegalArgumentException("Root certificate may not be "
                    + "null");
        }
        trustedRootCertificate = rootCert;
        trustEngine = new PKIXX509CredentialTrustEngine(getPKIXResolver());
        this.samlEngine = newSAMLEngine;
    }

    /**
     * Convenient access to the current entities ID.
     * @return the current entities ID
     */
    protected final byte[] getEntityID() {
        return samlEngine.getEntityID();
    }

    /**
     * Convenient access to the current entities ID in Base64 encoding.
     * @return the current entities ID in Base64 encoding
     */
    protected final String getEntityIDBase64() {
        return samlEngine.getEntityIDBase64();
    }

    /**
     * @return the trustedRootCertificate
     */
    protected final X509Certificate getTrustedRootCertificate() {
        return trustedRootCertificate;
    }

    /**
     * @return A resolver that holds the trust anchor
     */
    protected final PKIXValidationInformationResolver getPKIXResolver() {
        PKIXValidationInformation trustAnchor =
                new BasicPKIXValidationInformation(Collections.singleton(
                this.trustedRootCertificate), // root certificate is anchor
                null, // no certificate revocation list is used
                0);  // no intermediary certificates allowed
        PKIXValidationInformationResolver resolver =
                new StaticPKIXValidationInformationResolver(
                Collections.singletonList(trustAnchor), // only 1 anchor
                null); // no trusted names //TODO: insert names?
        return resolver;
    }

    /**
     * Checks if a new certificate is trusted.
     * @param credential A certificate to test
     * @return true, if the certificate is trusted, false otherwise
     * @throws org.opensaml.xml.security.SecurityException if there is a problem
     *          validating the certificate
     */
    protected final boolean isTrustedCredential(final X509Credential credential)
            throws org.opensaml.xml.security.SecurityException {
        if (trustEngine.validate(credential, null)
                && checkCertificateInfo(credential.getEntityCertificate())) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a certificate meets non-cryptographic criteria.
     * @param cert The certificate to check
     * @return true, if all criteria a met, false otherwise
     */
    protected boolean checkCertificateInfo(final X509Certificate cert) {
        boolean result = true;
        Logger logger = getLogger();
        try {
            cert.checkValidity();
        } catch (CertificateExpiredException e) {
            logger.warn("Certificate is expired", e);
            result = false;
        } catch (CertificateNotYetValidException e) {
            logger.warn("Certificate is not yet valid", e);
            result = false;
        }
        if (cert.getKeyUsage() == null) {
            logger.warn("Certificate has no specified KeyUsage");
            result = false;
        }
        return result;
    }

    /**
     * Get an SLF4J Logger.
     *
     * @return a Logger instance
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(CryptoEngine.class);
    }

    /**
     * Enum for the keyUsage extension of an x.509 certificate.
     * See RFC 5280
     */
    public enum KeyUsage {
        /**
         * Certificate is used for verifying digital signatures,
         * other than signatures on certificates (bit 5) and CRLs (bit 6), such
         * as those used in an entity authentication service, a data origin
         * authentication service, and/or an integrity service.
         */
        digitalSignature        (0),
        /**
         * Certificate is used to verify digital signatures, other than
         * signatures on certificates (bit 5) and CRLs (bit 6), used to provide
         * a non-repudiation service that protects against the signing entity
         * falsely denying some action.
         */
        nonRepudiation          (1),
        /**
         * Certificate is used for enciphering private or secret keys, i.e.,
         * for key transport.
         */
        keyEncipherment         (2),
        /**
         * Certificate is used for directly enciphering raw user data without
         * the use of an intermediate symmetric cipher.
         */
        dataEncipherment        (3),
        /**
         * Certificate is used for key agreement, for example, when a
         * Diffie-Hellman key is to be used for key management.
         */
        keyAgreement            (4),
        /**
         * Certificate is used for verifying signatures on public key
         * certificates.
         */
        keyCertSign             (5),
        /**
         * Certificate is used for verifying signatures on certificate
         * revocation lists.
         */
        cRLSign                 (6),
        /**
         * When the encipherOnly bit is asserted and the keyAgreement bit is
         * also set, the subject public key may be used only for enciphering
         * data while performing key agreement.
         */
        encipherOnly            (7),
        /**
         * When the decipherOnly bit is asserted and the keyAgreement bit is
         * also set, the subject public key may be used only for deciphering
         * data while performing key agreement.
         */
        decipherOnly            (8);

        /**
         * The index of the bit in the bit-array.
         */
        private int index;

        /**
         * Constructor.
         * @param pIndex The index of the bit in the bit-array
         */
        private KeyUsage(final int pIndex) {
                this.index = pIndex;
        }

        /**
         * @return The index of the bit in the bit-array.
         */
        public int index() {
                return index;
        }
    }
}

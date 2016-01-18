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
package org.sec2.frontend.processors;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import org.opensaml.xml.security.x509.X509Util;
import org.sec2.backend.exceptions.InvalidUserPKCException;
import org.sec2.backend.exceptions.UserAlreadyExistsException;
import org.sec2.frontend.exceptions.BackendProcessException;
import org.sec2.saml.xml.RegisterUser;
import org.opensaml.xml.util.Base64;
import org.sec2.backend.IUserInfo;
import org.sec2.backend.exceptions.UserNotFoundException;
import org.sec2.frontend.BackendHolder;
import org.sec2.frontend.KeyserverFrontendConfig;
import org.sec2.saml.xml.UserInfo;

/**
 * A processor for RegisterUser requests.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * July 25, 2013
 */
class RegisterUserProcessor
            extends AbstractUserInfoRespondingProcessor<RegisterUser> {

    /** {@inheritDoc} */
    @Override
    public final UserInfo process(final RegisterUser sec2message,
            final byte[] clientID, final String requestID)
            throws BackendProcessException {
        X509Certificate signCert = this.parseCertificate(
                sec2message.getSignatureCertificate().getValue(), clientID,
                requestID);
        X509Certificate encCert = this.parseCertificate(
                sec2message.getEncryptionCertificate().getValue(), clientID,
                requestID);
        if (!Arrays.equals(getHash(signCert.getPublicKey().getEncoded(),
                clientID, requestID), clientID)) {
            this.getLogger().error("The client's ID '{}' does not match the "
                    + "ID computed from the certificate '{}'",
                    Base64.encodeBytes(clientID), Base64.encodeBytes(
                    getHash(signCert.getPublicKey().getEncoded(), clientID,
                    requestID)));
            throw new BackendProcessException("The client's ID does not match "
                    + "the ID computed from the certificate",
                    BackendProcessException.Impact.INVALID_INPUT,
                    Base64.encodeBytes(clientID), requestID);
        }

        try {
            BackendHolder.getBackend().register(encCert, signCert);
        } catch (InvalidUserPKCException e) {
            throw new BackendProcessException(e,
                    BackendProcessException.Impact.INVALID_INPUT,
                    Base64.encodeBytes(clientID), requestID);
        } catch (UserAlreadyExistsException e) {
            throw new BackendProcessException(e,
                    BackendProcessException.Impact.INVALID_INPUT,
                    Base64.encodeBytes(clientID), requestID);
        }

        IUserInfo user;
        try {
            user = BackendHolder.getBackend().getUserInfo(clientID);
        } catch (UserNotFoundException e) {
            this.getLogger().warn("Registering user '{}' was successful, but "
                    + "user could not be found afterwards",
                    Base64.encodeBytes(clientID));
            throw new BackendProcessException(e,
                    BackendProcessException.Impact.NOT_FOUND,
                    Base64.encodeBytes(clientID), requestID);
        }

        return this.createUserInfo(user);
    }

    /**
     * Parses a certificate from a Base64 encoded byte array.
     * In the end, it uses not-yet-commons-ssl. If the input is a certificate
     * in whatever encoding, it is supposed to work
     * (ASN.1 DER, PKCS12, PEM, etc.).
     *
     * @param certBase64 a Base64 encoded certificate
     * @param clientID The client's ID
     * @param requestID The request's ID
     * @return the certificate
     * @throws BackendProcessException if something is wrong with the
     *          certificate
     */
    private X509Certificate parseCertificate(final String certBase64,
            final byte[] clientID, final String requestID)
            throws BackendProcessException {
        Collection<X509Certificate> certs;
        try {
            certs = X509Util.decodeCertificate(Base64.decode(certBase64));
        } catch (CertificateException e) {
            this.getLogger().error("Certificate could not be parsed", e);
            throw new BackendProcessException(
                    "Certificate could not be parsed", e,
                    BackendProcessException.Impact.INVALID_INPUT,
                    Base64.encodeBytes(clientID), requestID);
        }

        if (certs.isEmpty()) {
            this.getLogger().error("No certificate found");
            throw new BackendProcessException("No certificate found",
                    BackendProcessException.Impact.INVALID_INPUT,
                    Base64.encodeBytes(clientID), requestID);
        }

        if (certs.size() != 1) {
            this.getLogger().warn("Found {} certificates rather than a single "
                    + "one. Using the first in the list.", certs.size());
        }

        X509Certificate cert = certs.iterator().next();
        try {
            cert.checkValidity();
        } catch (CertificateException e) {
            throw new BackendProcessException(e,
                    BackendProcessException.Impact.INVALID_INPUT,
                    Base64.encodeBytes(clientID), requestID);
        }
        return cert;
    }

    /**
     * Helper method for getting a hash value.
     * @param rawData the data to hash
     * @return the hash
     * @param clientID The client's ID
     * @param requestID The request's ID
     * @throws BackendProcessException if the hash cannot be computed
     */
    private byte[] getHash(final byte[] rawData, final byte[] clientID,
            final String requestID) throws BackendProcessException {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(
                    KeyserverFrontendConfig.DIGEST_ALGORITHM);
        } catch (final NoSuchAlgorithmException e) {
            throw new BackendProcessException(e,
                    BackendProcessException.Impact.PROCESSING_ERROR,
                    Base64.encodeBytes(clientID), requestID);
        }
        digest.update(rawData);
        return digest.digest();
    }
}

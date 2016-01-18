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

import java.security.*;
import java.security.cert.X509Certificate;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.Pair;
import org.sec2.saml.client.exceptions.SecurityProviderException;
import org.sec2.statictestdata.TestKeyProvider;
import org.slf4j.LoggerFactory;

/**
 * Dummy SecurityProviderConnector that allows testing without an actual
 * smartcard.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 05, 2012
 */
//FIXME: Remove class for final version
public final class SecurityProviderConnectorDummy
        implements ISecurityProviderConnector {

    /**
     * Singleton constructor.
     */
    private SecurityProviderConnectorDummy() {
    }

    /**
     * Singleton getter.
     *
     * @return The singleton instance
     */
    protected static SecurityProviderConnectorDummy getInstance() {
        return SecurityProviderConnectorDummyHolder.instance;
    }

    /**
     * Returns the signature public key from the keystore.
     *
     * @return the signature public key
     * @throws SecurityProviderException if the SecurityProvider cannot be
     * accessed
     */
    @Override
    public PublicKey getSignaturePublicKey() throws SecurityProviderException {
        return TestKeyProvider.getInstance().getUserSignKey().getPublic();
    }

    /**
     * Returns the encryption public key from the keystore.
     *
     * @return the encryption public key
     * @throws SecurityProviderException if the SecurityProvider cannot be
     * accessed
     */
    @Override
    public PublicKey getEncryptionPublicKey() throws SecurityProviderException {
        return TestKeyProvider.getInstance().getUserEncKey().getPublic();
    }

    /**
     * Returns the keyserver's signature public key from the keystore.
     *
     * @return the keyserver's signature public key
     * @throws SecurityProviderException if the SecurityProvider cannot be
     * accessed
     */
    @Override
    public PublicKey getKeyserverSignaturePublicKey()
            throws SecurityProviderException {
        return TestKeyProvider.getInstance().getKeyserverSignKey().getPublic();
    }

    /**
     * Returns the encryption private key from the keystore.
     *
     * @return the encryption private key
     * @throws SecurityProviderException if the SecurityProvider cannot be
     * accessed
     * @throws UnsupportedOperationException if the underlying crypto system
     * does not allows to access the private key
     */
    @Override
    public PrivateKey getEncryptionPrivateKey()
            throws SecurityProviderException, UnsupportedOperationException {
        return TestKeyProvider.getInstance().getUserEncKey().getPrivate();
    }

    /**
     * Returns the signature private key from the keystore.
     *
     * @return the signature private key
     * @throws SecurityProviderException if the SecurityProvider cannot be
     * accessed
     * @throws UnsupportedOperationException if the underlying crypto system
     * does not allows to access the private key
     */
    public PrivateKey getSignaturePrivateKey() throws SecurityProviderException,
            UnsupportedOperationException {
        return TestKeyProvider.getInstance().getUserSignKey().getPrivate();
    }

    /**
     * Returns the untrusted signature certificate of the keyserver.
     *
     * @return the untrusted signature certificate of the keyserver
     * @throws SecurityProviderException if the SecurityProvider cannot be
     * accessed
     */
    @Override
    public X509Certificate getUntrustedKeyserverSignatureCertificate()
            throws SecurityProviderException {
        return TestKeyProvider.getInstance().getKeyserverSignCert();
    }

    /**
     * Returns the untrusted encryption certificate of the keyserver.
     *
     * @return the untrusted encryption certificate of the keyserver
     * @throws SecurityProviderException if the SecurityProvider cannot be
     * accessed
     */
    @Override
    public X509Certificate getUntrustedKeyserverEncryptionCertificate()
            throws SecurityProviderException {
        return TestKeyProvider.getInstance().getKeyserverEncCert();
    }

    /**
     * @return the currentUserIDBase64
     */
    @Override
    public String getCurrentUserIDBase64() {
        return TestKeyProvider.getInstance().getUserIDBase64();
    }

    /**
     * @return the currentUserID
     */
    @Override
    public byte[] getCurrentUserID() {
        return Base64.decode(getCurrentUserIDBase64());
    }

    /**
     * Nested class holding Singleton instance.
     */
    private static class SecurityProviderConnectorDummyHolder {

        /**
         * The singleton instance.
         */
        private static SecurityProviderConnectorDummy instance =
                new SecurityProviderConnectorDummy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeEncryptedGroupKey(final String groupName,
            final byte[] encGroupKey) throws SecurityProviderException {
        LoggerFactory.getLogger(this.getClass()).debug(
                "Key for group '{}' received for storage. "
                + "This is a NOP-implementation, nothing will be stored",
                groupName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pair<X509Certificate, X509Certificate> generateClientCertificates(
            final String email) throws SecurityProviderException {
        return new Pair<X509Certificate, X509Certificate>(
                TestKeyProvider.getInstance().getUserSignCert(),
                TestKeyProvider.getInstance().getUserEncCert());
    }
}

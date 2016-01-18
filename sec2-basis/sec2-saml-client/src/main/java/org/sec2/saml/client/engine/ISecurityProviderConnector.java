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

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import org.opensaml.xml.util.Pair;
import org.sec2.saml.client.exceptions.SecurityProviderException;

/**
 * Set of methods to access the sec2 clientside crypto infrastructure.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 05, 2012
 */
public interface ISecurityProviderConnector {

    /**
     * @return the currentUserID
     */
    byte[] getCurrentUserID();

    /**
     * @return the currentUserIDBase64
     */
    String getCurrentUserIDBase64();

    /**
     * Returns the encryption public key from the keystore.
     *
     * @return the encryption public key
     * @throws SecurityProviderException if the SecurityProvider cannot be
     *          accessed
     */
    PublicKey getEncryptionPublicKey() throws SecurityProviderException;

    /**
     * Returns the encryption private key from the keystore.
     *
     * @return the encryption private key
     * @throws SecurityProviderException if the SecurityProvider cannot be
     *          accessed
     * @throws UnsupportedOperationException if the underlying crypto system
     *          does not allows to access the private key
     */
    PrivateKey getEncryptionPrivateKey() throws SecurityProviderException,
            UnsupportedOperationException;

    /**
     * Returns the keyserver's signature public key from the keystore.
     *
     * @return the keyserver's signature public key
     * @throws SecurityProviderException if the SecurityProvider cannot be
     *          accessed
     */
    PublicKey getKeyserverSignaturePublicKey() throws SecurityProviderException;

    /**
     * Returns the signature public key from the keystore.
     *
     * @return the signature public key
     * @throws SecurityProviderException if the SecurityProvider cannot be
     *          accessed
     */
    PublicKey getSignaturePublicKey() throws SecurityProviderException;

    /**
     * Returns the signature private key from the keystore.
     *
     * @return the signature private key
     * @throws SecurityProviderException if the SecurityProvider cannot be
     *          accessed
     * @throws UnsupportedOperationException if the underlying crypto system
     *          does not allows to access the private key
     */
    PrivateKey getSignaturePrivateKey() throws SecurityProviderException,
            UnsupportedOperationException;

    /**
     * Returns the untrusted encryption certificate of the keyserver.
     *
     * @return the untrusted encryption certificate of the keyserver
     * @throws SecurityProviderException if the SecurityProvider cannot be
     *          accessed
     */
    X509Certificate getUntrustedKeyserverEncryptionCertificate()
            throws SecurityProviderException;

    /**
     * Returns the untrusted signature certificate of the keyserver.
     *
     * @return the untrusted signature certificate of the keyserver
     * @throws SecurityProviderException if the SecurityProvider cannot be
     *          accessed
     */
    X509Certificate getUntrustedKeyserverSignatureCertificate()
            throws SecurityProviderException;

    /**
     * Stores an encrypted group key in the keystore.
     * @param groupName The group's name
     * @param encGroupKey The encrypted group key
     * @throws SecurityProviderException if the keystore encounteres an error
     */
    void storeEncryptedGroupKey(String groupName, byte[] encGroupKey)
            throws SecurityProviderException;
    
    /**
     * Generates self-signed certificates.
     * @param email The email address to store in the certificates
     * @return A pair of certificates
     * @throws SecurityProviderException if the keystore encounteres an error
     */
    Pair<X509Certificate, X509Certificate> generateClientCertificates(
            String email) throws SecurityProviderException;
}

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
package org.sec2.saml.client.engine;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import junit.framework.TestCase;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.Pair;
import org.sec2.saml.client.SAMLClientConfig;
import org.sec2.saml.engine.CryptoEngine;

/**
 * Tests for the smartcard accessor.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * September 02, 2013
 */
public class SecurityProviderConnectorImplTests extends TestCase {

    /**
     * Tests the constructor.
     */
    public void testConstructor() throws Exception {
        SecurityProviderConnectorImpl.getInstance();
    }

    /**
     * Tests getting the signature public key.
     */
    public void testGetSignaturePublicKey() throws Exception {
        PublicKey pubk = null;
        pubk = SecurityProviderConnectorImpl.getInstance().
                getSignaturePublicKey();
        assertNotNull(pubk);
    }

    /**
     * Tests getting the encryption public key.
     */
    public void testGetEncryptionPublicKey() throws Exception {
        PublicKey pubk = null;
        pubk = SecurityProviderConnectorImpl.getInstance().
                getEncryptionPublicKey();
        assertNotNull(pubk);
    }

    /**
     * Tests getting the keyserver's signature public key.
     */
    public void testGetKeyserverSignaturePublicKey() throws Exception {
        PublicKey pubk = null;
        pubk = SecurityProviderConnectorImpl.getInstance().
                getKeyserverSignaturePublicKey();
        assertNotNull(pubk);
    }

    /**
     * Tests that the CryptoConnector does not return a signature private key.
     */
    public void testGetSignaturePrivateKey() throws Exception {
        PrivateKey privk = null;
        try {
            privk = SecurityProviderConnectorImpl.getInstance().
                    getSignaturePrivateKey();
            fail("Should have raised an UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertNotNull(e);
        }
        assertNull(privk);
    }

    /**
     * Tests that the CryptoConnector does not return an encryption private key.
     */
    public void testGetEncryptionPrivateKey() throws Exception {
        PrivateKey privk = null;
        try {
            privk = SecurityProviderConnectorImpl.getInstance().
                    getEncryptionPrivateKey();
            fail("Should have raised an UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertNotNull(e);
        }
        assertNull(privk);
    }

    /**
     * Tests getting the keyserver's signature certificate.
     */
    public void testGetUntrustedKeyserverSignatureCertificate()
            throws Exception {
        Certificate cert = null;
        cert = SecurityProviderConnectorImpl.getInstance().
                getUntrustedKeyserverSignatureCertificate();
        assertNotNull(cert);
    }

    /**
     * Tests getting the keyserver's encryption certificate.
     */
    public void testGetUntrustedKeyserverEncryptionCertificate()
            throws Exception {
        Certificate cert = null;
        cert = SecurityProviderConnectorImpl.getInstance().
                getUntrustedKeyserverEncryptionCertificate();
        assertNotNull(cert);
    }

    /**
     * Tests the raw user ID.
     */
    public void testUserID() throws Exception {
        byte[] expectedID = null;
        byte[] actualID = null;
        expectedID = getHash(SecurityProviderConnectorImpl.getInstance().
                getSignaturePublicKey().getEncoded());
        actualID = SecurityProviderConnectorImpl.getInstance().
                getCurrentUserID();
        assertNotNull(expectedID);
        assertNotNull(actualID);
        assertTrue(expectedID.length > 0);
        assertTrue(actualID.length > 0);
        assertTrue(Arrays.equals(expectedID, actualID));
    }

    /**
     * Tests the encoded user ID.
     */
    public void testUserIDBase64() throws Exception {
        String expectedID = null;
        String actualID = null;
        expectedID = Base64.encodeBytes(getHash(
                SecurityProviderConnectorImpl.getInstance().
                getSignaturePublicKey().getEncoded()));
        actualID = SecurityProviderConnectorImpl.getInstance().
                getCurrentUserIDBase64();
        assertNotNull(expectedID);
        assertNotNull(actualID);
        assertTrue(expectedID.length() > 0);
        assertTrue(actualID.length() > 0);
        assertTrue(expectedID.equals(actualID));
    }

    /**
     * Tests getting self-signed certificates.
     */
    public void testGetCertificates() throws Exception {
        Pair<X509Certificate, X509Certificate> certs =
                SecurityProviderConnectorImpl.getInstance().
                generateClientCertificates("newuser@sec2.org");
        X509Certificate signCert = certs.getFirst();
        assertNotNull(signCert);
        assertEquals(3, signCert.getVersion());
        signCert.checkValidity();
        assertEquals(-1, signCert.getBasicConstraints()); // no CA-certificate
        assertTrue(signCert.getKeyUsage()[
                CryptoEngine.KeyUsage.digitalSignature.index()]);
        assertFalse(signCert.getKeyUsage()[
                CryptoEngine.KeyUsage.keyEncipherment.index()]);
        signCert.verify(signCert.getPublicKey());

        X509Certificate encCert = certs.getSecond();
        assertNotNull(encCert);
        assertEquals(3, encCert.getVersion());
        encCert.checkValidity();
        assertEquals(-1, encCert.getBasicConstraints()); // no CA-certificate
        assertFalse(encCert.getKeyUsage()[
                CryptoEngine.KeyUsage.digitalSignature.index()]);
        assertTrue(signCert.getKeyUsage()[
                CryptoEngine.KeyUsage.keyEncipherment.index()]);
        encCert.verify(signCert.getPublicKey());
    }

    /**
     * Helper method for getting a hash value.
     *
     * @param rawData the data to hash
     * @return the hash
     */
    private byte[] getHash(final byte[] rawData) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(
                    SAMLClientConfig.DIGEST_ALGORITHM);
        } catch (final NoSuchAlgorithmException e) {
            fail(e.toString());
        }
        assertNotNull(digest);
        digest.update(rawData);
        return digest.digest();
    }
}

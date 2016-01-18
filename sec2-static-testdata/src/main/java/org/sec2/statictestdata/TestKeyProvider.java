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
package org.sec2.statictestdata;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.security.*;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;

/**
 * A helper class to provide test-keys and -certificates.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * July 25, 2013
 */
public final class TestKeyProvider {

    /**
     * Path to test keys and certs.
     */
    private static final String BASE_PATH = "/certificates/";
    
    /**
     * The type of certificates used.
     */
    private static final String CERTIFICATE_TYPE = "X.509";
    
    /**
     * The encoding used for streams.
     */
    private static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * The hash algorithm used to hash the public key.
     */
    private static final String DIGEST_ALGORITHM = "SHA-256";
    
    /**
     * The hash of the keyserver's public key.
     */
    private byte[] keyserverID;

    /**
     * The Base64 encoded hash of the keyserver's public key.
     */
    private String keyserverIDBase64;

    /**
     * The hash of the first user's public key.
     */
    private byte[] user1ID;

    /**
     * The Base64 encoded hash of the first user's public key.
     */
    private String user1IDBase64;

    /**
     * The hash of the second user's public key.
     */
    private byte[] user2ID;

    /**
     * The Base64 encoded hash of the second user's public key.
     */
    private String user2IDBase64;

    /**
     * The hash of the third user's public key.
     */
    private byte[] user3ID;

    /**
     * The Base64 encoded hash of the third user's public key.
     */
    private String user3IDBase64;

    /**
     * RSA signing key for the keyserver.
     */
    private KeyPair keyserverSignKey;

    /**
     * RSA encryption key for the keyserver.
     */
    private KeyPair keyserverEncKey;

    /**
     * RSA signing key for the first user.
     */
    private KeyPair user1SignKey;

    /**
     * RSA encryption key for the first user.
     */
    private KeyPair user1EncKey;

    /**
     * RSA signing key for the second user.
     */
    private KeyPair user2SignKey;

    /**
     * RSA encryption key for the second user.
     */
    private KeyPair user2EncKey;

    /**
     * RSA signing key for the third user.
     */
    private KeyPair user3SignKey;

    /**
     * RSA encryption key for the third user.
     */
    private KeyPair user3EncKey;

    /**
     * RSA signing certificate for the keyserver.
     */
    private X509Certificate keyserverSignCert;

    /**
     * RSA encryption certificate for the keyserver.
     */
    private X509Certificate keyserverEncCert;

    /**
     * RSA signing certificate for the first user.
     */
    private X509Certificate user1SignCert;

    /**
     * RSA encryption certificate for the first user.
     */
    private X509Certificate user1EncCert;

    /**
     * RSA signing certificate for the second user.
     */
    private X509Certificate user2SignCert;

    /**
     * RSA encryption certificate for the second user.
     */
    private X509Certificate user2EncCert;

    /**
     * RSA signing certificate for the third user.
     */
    private X509Certificate user3SignCert;

    /**
     * RSA encryption certificate for the third user.
     */
    private X509Certificate user3EncCert;

    /**
     * Singleton constructor.
     */
    private TestKeyProvider() { }

    /**
     * Returns the requested keypair.
     *
     * @param path The path to the private key
     * @return the requested keypair
     */
    private KeyPair getKeyPair(final String path) {
        KeyFactory keyFactory = getKeyFactory();
        byte[] privKeyBytes;
        try {
            BufferedInputStream is = new BufferedInputStream(
                    this.getClass().getResourceAsStream(path));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int size;
            byte[] buffer = new byte[128];
            try {
                while ((size = is.read(buffer)) >= 0) {
                  out.write(buffer, 0, size);
                }
                privKeyBytes = out.toByteArray();
            } finally {
                is.close();
                out.close();
            }

            KeySpec ks = new PKCS8EncodedKeySpec(privKeyBytes);
            RSAPrivateCrtKey privk =
                    (RSAPrivateCrtKey) keyFactory.generatePrivate(ks);
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(
                    privk.getModulus(), privk.getPublicExponent());
            RSAPublicKey pubk =
                    (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
            return new KeyPair(pubk, privk);
        } catch (IOException e) {
            throw new Error(e); // Today's a good day to die
        } catch (InvalidKeySpecException e) {
            throw new Error(e); // Today's a good day to die
        }
    }

    /**
     * Returns the requested certificate.
     *
     * @param path The path to the certificate
     * @return the requested certificate
     */
    private X509Certificate getCertificate(final String path) {
        String certString;
        try {
            certString = inputstreamToString(
                    this.getClass().getResourceAsStream(path), null);
        } catch (IOException e) {
            throw new Error(e); // Today's a good day to die
        }
        try {
            CertificateFactory certFactory =
                    CertificateFactory.getInstance(
                    CERTIFICATE_TYPE);
            X509Certificate untrustedCert = (X509Certificate)
                    certFactory.generateCertificate(
                    new ByteArrayInputStream(certString.getBytes(
                    DEFAULT_ENCODING)));
            try {
                untrustedCert.checkValidity();
            } catch (CertificateExpiredException e) {
                throw new RuntimeException(
                        "You need to generate new test keys and certificates!"
                        + " See 'src/test/resources/README'"
                        + " for instructions.", e);
            }
            return untrustedCert;
        } catch (GeneralSecurityException e) {
            throw new Error(e); // Today's a good day to die
        } catch (UnsupportedEncodingException e) {
            throw new Error(e); // Today's a good day to die
        }
    }

    /**
     * Helper method for getting a hash value.
     * @param rawData the data to hash
     * @return the hash
     */
    private byte[] getHash(final byte[] rawData) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(
                    DIGEST_ALGORITHM);
        } catch (final NoSuchAlgorithmException e) {
            throw new Error(e); // Today's a good day to die
        }
        return digest.digest(rawData);
    }

    /**
     * Singleton getter.
     * @return The singleton instance
     */
    public static TestKeyProvider getInstance() {
        return KeyProviderHolder.instance;
    }

    /**
     * @return A key factory
     */
    private KeyFactory getKeyFactory() {
        try {
            return KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e); // Today's a good day to die
        }
    }

    /**
     * @return the keyserverID
     */
    public byte[] getKeyserverID() {
        if (keyserverID == null) {
            keyserverID = this.getHash(
                    this.getKeyserverSignCert().getPublicKey().getEncoded());
        }
        return Arrays.copyOf(keyserverID, keyserverID.length);
    }

    /**
     * @return the keyserverIDBase64
     */
    public String getKeyserverIDBase64() {
        if (keyserverIDBase64 == null) {
            keyserverIDBase64 = Base64.encodeBytes(this.getKeyserverID());
        }
        return keyserverIDBase64;
    }

    /**
     * @return the user1ID
     */
    public byte[] getUserID() {
        if (user1ID == null) {
            user1ID = this.getHash(
                    this.getUserSignCert().getPublicKey().getEncoded());
        }
        return Arrays.copyOf(user1ID, user1ID.length);
    }

    /**
     * @return the user1IDBase64
     */
    public String getUserIDBase64() {
        if (user1IDBase64 == null) {
            user1IDBase64 = Base64.encodeBytes(this.getUserID());
        }
        return user1IDBase64;
    }

    /**
     * @return the user2ID
     */
    public byte[] getUser2ID() {
        if (user2ID == null) {
            user2ID = this.getHash(
                    this.getUser2SignCert().getPublicKey().getEncoded());
        }
        return Arrays.copyOf(user2ID, user2ID.length);
    }

    /**
     * @return the user2IDBase64
     */
    public String getUser2IDBase64() {
        if (user2IDBase64 == null) {
            user2IDBase64 = Base64.encodeBytes(this.getUser2ID());
        }
        return user2IDBase64;
    }

    /**
     * @return the user3ID
     */
    public byte[] getUser3ID() {
        if (user3ID == null) {
            user3ID = this.getHash(
                    this.getUser3SignCert().getPublicKey().getEncoded());
        }
        return Arrays.copyOf(user3ID, user3ID.length);
    }

    /**
     * @return the user3IDBase64
     */
    public String getUser3IDBase64() {
        if (user3IDBase64 == null) {
            user3IDBase64 = Base64.encodeBytes(this.getUser3ID());
        }
        return user3IDBase64;
    }

    /**
     * @return the keyserverSignKey
     */
    public KeyPair getKeyserverSignKey() {
        if (keyserverSignKey == null) {
            keyserverSignKey = this.getKeyPair(
                    BASE_PATH + "sec2.server.sign.key.pkcs8");
        }
        return keyserverSignKey;
    }

    /**
     * @return the keyserverEncKey
     */
    public KeyPair getKeyserverEncKey() {
        if (keyserverEncKey == null) {
            keyserverEncKey = this.getKeyPair(
                    BASE_PATH + "sec2.server.enc.key.pkcs8");
        }
        return keyserverEncKey;
    }

    /**
     * @return the user1SignKey
     */
    public KeyPair getUserSignKey() {
        if (user1SignKey == null) {
            user1SignKey = this.getKeyPair(
                    BASE_PATH + "sec2.client.sign.key.pkcs8");
        }
        return user1SignKey;
    }

    /**
     * @return the user1EncKey
     */
    public KeyPair getUserEncKey() {
        if (user1EncKey == null) {
            user1EncKey = this.getKeyPair(
                    BASE_PATH + "sec2.client.enc.key.pkcs8");
        }
        return user1EncKey;
    }

    /**
     * @return the user2SignKey
     */
    public KeyPair getUser2SignKey() {
        if (user2SignKey == null) {
            user2SignKey = this.getKeyPair(
                    BASE_PATH + "sec2.client2.sign.key.pkcs8");
        }
        return user2SignKey;
    }

    /**
     * @return the user2EncKey
     */
    public KeyPair getUser2EncKey() {
        if (user2EncKey == null) {
            user2EncKey = this.getKeyPair(
                    BASE_PATH + "sec2.client2.enc.key.pkcs8");
        }
        return user2EncKey;
    }

    /**
     * @return the user3SignKey
     */
    public KeyPair getUser3SignKey() {
        if (user3SignKey == null) {
            user3SignKey = this.getKeyPair(
                    BASE_PATH + "sec2.client3.sign.key.pkcs8");
        }
        return user3SignKey;
    }

    /**
     * @return the user3EncKey
     */
    public KeyPair getUser3EncKey() {
        if (user3EncKey == null) {
            user3EncKey = this.getKeyPair(
                    BASE_PATH + "sec2.client3.enc.key.pkcs8");
        }
        return user3EncKey;
    }

    /**
     * @return the keyserverSignCert
     */
    public X509Certificate getKeyserverSignCert() {
        if (keyserverSignCert == null) {
            keyserverSignCert = this.getCertificate(
                    BASE_PATH + "sec2.server.sign.crt");
        }
        return keyserverSignCert;
    }

    /**
     * @return the keyserverEncCert
     */
    public X509Certificate getKeyserverEncCert() {
        if (keyserverEncCert == null) {
            keyserverEncCert = this.getCertificate(
                    BASE_PATH + "sec2.server.enc.crt");
        }
        return keyserverEncCert;
    }

    /**
     * @return the user1SignCert
     */
    public X509Certificate getUserSignCert() {
        if (user1SignCert == null) {
            user1SignCert = this.getCertificate(
                    BASE_PATH + "sec2.client.sign.crt");
        }
        return user1SignCert;
    }

    /**
     * @return the user1EncCert
     */
    public X509Certificate getUserEncCert() {
        if (user1EncCert == null) {
            user1EncCert = this.getCertificate(
                    BASE_PATH + "sec2.client.enc.crt");
        }
        return user1EncCert;
    }

    /**
     * @return the user2SignCert
     */
    public X509Certificate getUser2SignCert() {
        if (user2SignCert == null) {
            user2SignCert = this.getCertificate(
                    BASE_PATH + "sec2.client2.sign.crt");
        }
        return user2SignCert;
    }

    /**
     * @return the user2EncCert
     */
    public X509Certificate getUser2EncCert() {
        if (user2EncCert == null) {
            user2EncCert = this.getCertificate(
                    BASE_PATH + "sec2.client2.enc.crt");
        }
        return user2EncCert;
    }

    /**
     * @return the user3SignCert
     */
    public X509Certificate getUser3SignCert() {
        if (user3SignCert == null) {
            user3SignCert = this.getCertificate(
                    BASE_PATH + "sec2.client3.sign.crt");
        }
        return user3SignCert;
    }

    /**
     * @return the user3EncCert
     */
    public X509Certificate getUser3EncCert() {
        if (user3EncCert == null) {
            user3EncCert = this.getCertificate(
                    BASE_PATH + "sec2.client3.enc.crt");
        }
        return user3EncCert;
    }

    /**
     * Nested class holding Singleton instance.
     */
    private static class KeyProviderHolder {
        /**
         * The singleton instance.
         */
        private static TestKeyProvider instance =
                new TestKeyProvider();
    }
    
    /**
     * Taken from OpenSAML's DatatypeHelper.inputstreamToString().
     * 
     * Reads an input stream into a string. The provide stream is <strong>not</strong> closed.
     * 
     * @param input the input stream to read
     * @param decoder character decoder to use, if null, system default character set is used
     * 
     * @return the string read from the stream
     * 
     * @throws IOException thrown if there is a problem reading from the stream and decoding it
     */
    private String inputstreamToString(InputStream input,
            CharsetDecoder decoder) throws IOException {
        CharsetDecoder charsetDecoder = decoder;
        if (decoder == null) {
            charsetDecoder = Charset.defaultCharset().newDecoder();
        }

        BufferedReader reader = new BufferedReader
                (new InputStreamReader(input, charsetDecoder));

        StringBuilder stringBuffer = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            stringBuffer.append(line).append("\n");
            line = reader.readLine();
        }

        reader.close();

        return stringBuffer.toString();
    }
}

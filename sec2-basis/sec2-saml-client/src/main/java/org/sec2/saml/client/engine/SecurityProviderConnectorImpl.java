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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import org.joda.time.DateTime;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.Pair;
import org.sec2.exceptions.EntityUnknownException;
import org.sec2.saml.client.SAMLClientConfig;
import org.sec2.saml.client.exceptions.SecurityProviderException;
import org.sec2.saml.xml.EmailAddressType;
import org.sec2.securityprovider.mobileclient.MobileClientProvider;
import org.sec2.securityprovider.serviceparameter.PublicKeyType;
import org.sec2.statictestdata.TestKeyProvider;
import org.sec2.token.keys.GroupKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x509.BasicConstraints;
import org.spongycastle.asn1.x509.KeyUsage;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;
import org.spongycastle.asn1.x509.X509Extension;
import org.spongycastle.cert.CertIOException;
import org.spongycastle.cert.X509v3CertificateBuilder;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;

/**
 * Simplifies access to the sec2 crypto infrastructure.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 26, 2013
 */
public final class SecurityProviderConnectorImpl
        implements ISecurityProviderConnector {

    /**
     * Class logger.
     */
    private final Logger log = LoggerFactory.getLogger(
            SecurityProviderConnectorImpl.class);
    /**
     * The length (in bits) of the serial numbers for generated certificates.
     */
    private static final int CERT_SERIAL_LENGTH = 64;
    /**
     * The keystore that holds the trusted public key, certificates, etc.
     */
    private KeyStore keystore;
    /**
     * The hash of the registered user's public key.
     */
    private byte[] currentUserID;
    /**
     * The Base64 encoded hash of the registered user's public key.
     */
    private String currentUserIDBase64;

    /**
     * Singleton constructor.
     *
     * @throws EntityUnknownException if the SecurityProvider cannot be accessed
     */
    private SecurityProviderConnectorImpl() throws EntityUnknownException {
        // Get signature public key of the user
        PublicKey signPublicKey;
        try {
            try {
                keystore = KeyStore.getInstance(
                        SAMLClientConfig.KEYSTORE_TYPE);
                keystore.load(null, null);
                signPublicKey = this.getSignaturePublicKey();
            } catch (GeneralSecurityException e) {
                throw new SecurityProviderException(e);
            } catch (final IOException e) {
                throw new SecurityProviderException(e);
            }
        } catch (SecurityProviderException e) {
            throw new EntityUnknownException(e);
        }

        // Get digest of signature public key of the user
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(
                    SAMLClientConfig.DIGEST_ALGORITHM);
        } catch (final NoSuchAlgorithmException e) {
            throw new EntityUnknownException(e);
        }
        this.currentUserID = digest.digest(signPublicKey.getEncoded());

        // Get Base64 encoded digest of signature public key of the user
        this.currentUserIDBase64 = Base64.encodeBytes(this.currentUserID);
    }

    /**
     * Singleton getter.
     *
     * @return The singleton instance
     * @throws EntityUnknownException if the SecurityProvider cannot be accessed
     */
    protected static SecurityProviderConnectorImpl getInstance()
            throws EntityUnknownException {
        if (SecurityProviderConnectorHolder.exception != null) {
            throw SecurityProviderConnectorHolder.exception;
        }
        return SecurityProviderConnectorHolder.instance;
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
        return this.getKey(PublicKeyType.CLIENT_SIGNATURE);
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
        return this.getKey(PublicKeyType.CLIENT_ENCRYPTION);
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
        //return this.getKey(PublicKeyType.SERVER_SIGNATURE);
        //FIXME: remove dirty hack
        return this.getUntrustedKeyserverSignatureCertificate().getPublicKey();
    }

    /**
     * Returns the requested public key from the keystore.
     *
     * @param key the public key to return
     * @return the requested public key
     * @throws SecurityProviderException if the SecurityProvider cannot be
     * accessed
     */
    private PublicKey getKey(final PublicKeyType key)
            throws SecurityProviderException {
        try {
            return (PublicKey) keystore.getKey(key.name(), null);
        } catch (GeneralSecurityException e) {
            throw new SecurityProviderException(e);
        }
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
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
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
     * Stores an encrypted group key in the keystore.
     *
     * @param groupName The group's name
     * @param encGroupKey The encrypted group key
     * @throws SecurityProviderException is the keystore encountered an error
     */
    @Override
    public void storeEncryptedGroupKey(final String groupName,
            final byte[] encGroupKey) throws SecurityProviderException {
        try {
            if (!keystore.isKeyEntry(groupName)) {
                GroupKey gk = new GroupKey(encGroupKey,
                        groupName.getBytes(SAMLClientConfig.DEFAULT_ENCODING));
                keystore.setKeyEntry(groupName, gk, null, null);
            }
        } catch (KeyStoreException e) {
            throw new SecurityProviderException(e);
        } catch (UnsupportedEncodingException e) {
            throw new SecurityProviderException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pair<X509Certificate, X509Certificate> generateClientCertificates(
            final String pEmail) throws SecurityProviderException {
        X509Certificate certSign;
        X509Certificate certEnc;

        String email = pEmail.trim();
        // validate email address
        Matcher matcher = EmailAddressType.EMAIL_ADDRESS_PATTERN.matcher(email);
        if (!matcher.matches()) {
            throw new SecurityProviderException("Email address is invalid");
        }

        log.info("Generating new certificates for user {}", email);

        // set validity period
        Date yesterday = new DateTime().minusDays(1).toDate();
        Date tomorrow = new DateTime().plusDays(1).toDate();

        ContentSigner sigGen;
        try {
            sigGen = new JcaContentSignerBuilder("SHA1withRSA").
                    setProvider(MobileClientProvider.PROVIDER_NAME).build(null);
        } catch (OperatorCreationException e) {
            throw new SecurityProviderException(e);
        }

        /*
         * Generate signature certificate
         */
        //set subject and issuer
        String certSubjectSign = new MessageFormat(
                SAMLClientConfig.CERT_SUBJECT).format(
                new Object[]{email.trim(), "Sign"});
        X500Name subjectSign = new X500Name(certSubjectSign);
        log.debug("Signature certificate subject: {}", subjectSign);

        // set serial number
        BigInteger serialSign = new BigInteger(
                CERT_SERIAL_LENGTH, new SecureRandom());
        log.debug("Signature certificate serial: {}", serialSign);

        // set public key
        SubjectPublicKeyInfo subjectPubKeyInfoSign =
                new SubjectPublicKeyInfo(ASN1Sequence.getInstance(
                this.getSignaturePublicKey().getEncoded()));

        X509v3CertificateBuilder certGenSign = new X509v3CertificateBuilder(
                subjectSign, serialSign, yesterday, tomorrow, subjectSign,
                subjectPubKeyInfoSign);

        try {
            certGenSign.addExtension(X509Extension.basicConstraints, false,
                    new BasicConstraints(false)); // no CA certificate
            certGenSign.addExtension(X509Extension.keyUsage, true,
                    new KeyUsage(KeyUsage.digitalSignature));
        } catch (CertIOException e) {
            throw new SecurityProviderException(e);
        }

        try {
            certSign = convertBCCertToJavaCert(
                    certGenSign.build(sigGen).toASN1Structure());
        } catch (CertificateException e) {
            throw new SecurityProviderException(e);
        } catch (IOException e) {
            throw new SecurityProviderException(e);
        }
        log.debug("Signature certificate: {}", certSign);


        /*
         * Generate encryption certificate
         */
        //set subject and issuer
        String certSubjectEnc = new MessageFormat(
                SAMLClientConfig.CERT_SUBJECT).format(
                new Object[]{email.trim(), "Enc"});
        X500Name subjectEnc = new X500Name(certSubjectEnc);
        log.debug("Encryption certificate subject: {}", subjectEnc);

        // set serial number
        BigInteger serialEnc = new BigInteger(
                CERT_SERIAL_LENGTH, new SecureRandom());
        log.debug("Encryption certificate serial: {}", serialEnc);

        // set public key
        SubjectPublicKeyInfo subjectPubKeyInfoEnc =
                new SubjectPublicKeyInfo(ASN1Sequence.getInstance(
                this.getEncryptionPublicKey().getEncoded()));

        X509v3CertificateBuilder certGenEnc = new X509v3CertificateBuilder(
                subjectSign, serialEnc, yesterday, tomorrow, subjectEnc,
                subjectPubKeyInfoEnc);

        try {
            certGenEnc.addExtension(X509Extension.basicConstraints, false,
                    new BasicConstraints(false)); // no CA certificate
            certGenEnc.addExtension(X509Extension.keyUsage, true,
                    new KeyUsage(KeyUsage.keyEncipherment));
        } catch (CertIOException e) {
            throw new SecurityProviderException(e);
        }

        try {
            certEnc = convertBCCertToJavaCert(
                    certGenEnc.build(sigGen).toASN1Structure());
        } catch (CertificateException e) {
            throw new SecurityProviderException(e);
        } catch (IOException e) {
            throw new SecurityProviderException(e);
        }
        log.debug("Encryption certificate: {}", certEnc);

        return new Pair<X509Certificate, X509Certificate>(certSign, certEnc);
    }

    /**
     * Converts a BouncyCastle-certificate object into a Java certificate.
     *
     * @param pBCCert the BouncyCastle-certificate
     * @return the Java certificate
     * @throws CertificateException if the certificate cannot be created
     * @throws IOException if the BouncyCastle-certificate cannot be encoded
     */
    private X509Certificate convertBCCertToJavaCert(
            final org.spongycastle.asn1.x509.Certificate pBCCert)
            throws CertificateException, IOException {
        CertificateFactory cf = CertificateFactory.getInstance(
                SAMLClientConfig.CERTIFICATE_TYPE);
        // Read Certificate
        InputStream is = new ByteArrayInputStream(pBCCert.getEncoded());
        X509Certificate theCert = (X509Certificate) cf.generateCertificate(is);
        is.close();
        return theCert;
    }

    /**
     * @return the currentUserID
     */
    @Override
    public byte[] getCurrentUserID() {
        return Arrays.copyOf(currentUserID, currentUserID.length);
    }

    /**
     * @return the currentUserIDBase64
     */
    @Override
    public String getCurrentUserIDBase64() {
        return currentUserIDBase64;


    }

    /**
     * Nested class holding Singleton instance.
     */
    private static class SecurityProviderConnectorHolder {

        /**
         * The singleton instance.
         */
        private static SecurityProviderConnectorImpl instance;
        /**
         * EntityUnknownException that might have been thrown during creation of
         * the SecurityProviderConnectorImpl.
         */
        private static EntityUnknownException exception;

        static {
            try {
                instance = new SecurityProviderConnectorImpl();
            } catch (EntityUnknownException e) {
                exception = e;
            }
        }
    }
}

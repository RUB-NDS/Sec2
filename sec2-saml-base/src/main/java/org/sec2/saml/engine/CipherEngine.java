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

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.crypto.SecretKey;
import org.opensaml.saml2.encryption.Decrypter;
import org.opensaml.saml2.encryption.Encrypter;
import org.opensaml.xml.encryption.EncryptionParameters;
import org.opensaml.xml.encryption.KeyEncryptionParameters;
import org.opensaml.xml.encryption.SimpleRetrievalMethodEncryptedKeyResolver;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.CredentialResolver;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.criteria.KeyNameCriteria;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoCredentialResolver;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.Pair;
import org.sec2.saml.SAMLBaseConfig;
import org.sec2.saml.exceptions.CipherEngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base engine that supports typical XML encryption operations.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * Oktober 31, 2012
 */
public abstract class CipherEngine extends CryptoEngine {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(CipherEngine.class);

    /**
     * Keysize of the dummy key. This is only used to make sure that the
     * KeyGenerator does not whine about a small keysize.
     */
    private static final int DUMMY_KEYSIZE = 512;

    /**
     * A dummy Keypair that OpenSAML thinks is used for decryption.
     */
    private Credential dummyCredential;

    /**
     * Holds the dummy credential.
     */
    private final StaticKeyInfoCredentialResolver decryptionCredentialResolver;

    /**
     * Constructor.
     * @param rootCert A trusted root certificate. Establishing trust to this
     *          certificate is out of scope of this class. The caller has to
     *          make sure that trust is given.
     * @param newSAMLEngine SAMLEngine that provides information about the
     *          entity.
     * @throws CipherEngineException if the encryption algorithm is not
     *          supported
     */
    public CipherEngine(final X509Certificate rootCert,
            final SAMLEngine newSAMLEngine) throws CipherEngineException {
        super(rootCert, newSAMLEngine);
        createDummyCredential();
        decryptionCredentialResolver =
                new StaticKeyInfoCredentialResolver(dummyCredential);
    }

    /**
     * @return The CredentialResolver used to resolve the credentials used to
     *          encrypt.
     * @throws CipherEngineException if the KeyInfoResolver cannot access
     *          the keys or if the credential is invalid in some way
     */
    protected abstract CredentialResolver getEncryptionCredentialResolver()
            throws CipherEngineException;

    /**
     * @return The CredentialResolver used to resolve the credentials used to
     *          decrypt.
     */
    protected final KeyInfoCredentialResolver
            getDecryptionCredentialResolver() {
        return decryptionCredentialResolver;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean checkCertificateInfo(final X509Certificate cert) {
        boolean result = super.checkCertificateInfo(cert);

        if (!cert.getKeyUsage()[KeyUsage.keyEncipherment.index()]) {
            log.warn("Certificate is not intended to be used for "
                    + "key encipherment");
            result = false;
        }
        if (cert.getKeyUsage()[KeyUsage.digitalSignature.index()]
                || cert.getKeyUsage()[KeyUsage.nonRepudiation.index()]
                || cert.getKeyUsage()[KeyUsage.keyCertSign.index()]
                || cert.getKeyUsage()[KeyUsage.cRLSign.index()]) {
            log.warn("A certificate for key encipherment must not be "
                    + "used for digital signatures");
            result = false;
        }
        return result;
    }

    /**
     * Create dummy keypair.
     * @throws CipherEngineException if the cipher algorithm is not
     *          supported
     */
    private void createDummyCredential() throws CipherEngineException {
        KeyPairGenerator keyGen;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new CipherEngineException(
                    "Cipher algorithm not supported", e);
        }
        keyGen.initialize(DUMMY_KEYSIZE);
        KeyPair pair = keyGen.generateKeyPair();
        BasicCredential dummy = new BasicCredential();
        dummy.setPrivateKey(pair.getPrivate());
        dummy.setPublicKey(pair.getPublic());
        dummy.setUsageType(UsageType.ENCRYPTION);
        dummyCredential = dummy;
    }

    /**
     * @return the dummyCredential
     */
    protected final Credential getDummyCredential() {
        return dummyCredential;
    }

    /**
     * @param recipientKeyDigest the digest of the recipient's public key
     * @param dataKey A symmetric key that is encrypted using the recipient's
     *          public key, may be null for a random key
     * @return An encrypter and the symmetric credential it uses
     *          for encryption
     * @throws CipherEngineException if there is an error processing the
     *          public key's digest
     */
    public final Pair<Encrypter, Credential> getEncrypter(
            final byte[] recipientKeyDigest, final Credential dataKey)
            throws CipherEngineException {
        Credential localKey = dataKey;
        if (localKey == null) {
            try {
                SecretKey randomKey = SecurityHelper.generateSymmetricKey(
                    SAMLBaseConfig.XML_ENCRYPTION_ALGORITHM_NS);
                BasicCredential cred = new BasicCredential();
                cred.setSecretKey(randomKey);
                cred.setUsageType(UsageType.ENCRYPTION);
                localKey = cred;
            } catch (GeneralSecurityException e) {
                throw new CipherEngineException(e);
            }
        }
        CriteriaSet criteriaSet = new CriteriaSet(); //TODO: Add something
        criteriaSet.add(new KeyNameCriteria(
                Base64.encodeBytes(recipientKeyDigest)));
        Pair<Encrypter, Credential> encryptionPair;
        try {
            encryptionPair = getEncrypter(getEncryptionCredentialResolver().
                    resolveSingle(criteriaSet), localKey);
        } catch (SecurityException e) {
            throw new CipherEngineException(e);
        }
        return encryptionPair;
    }

    /**
     * @param keyEncCredential The credential that is used to encrypt a
     *          symmetric key
     * @param key A symmetric key that is encrypted using the keyEncCredential
     * @return An encrypter and the random symmetric credential it uses
     *          for encryption
     * @throws CipherEngineException if the SecretKey cannot be created
     */
    protected final Pair<Encrypter, Credential> getEncrypter(
            final Credential keyEncCredential, final Credential key)
            throws CipherEngineException {
        EncryptionParameters encParams = new EncryptionParameters();
        encParams.setAlgorithm(SAMLBaseConfig.XML_ENCRYPTION_ALGORITHM_NS);
        encParams.setEncryptionCredential(key);
        //TODO: Set KeyInfoGenerator
        return new Pair<Encrypter, Credential>(
                new Encrypter(encParams,
                    this.getKeyEncryptionParameters(keyEncCredential)), key);
    }

    /**
     * @return KeyEncryptionParameters, that define the key encryption
     * credential. May be an empty list, if no key encryption key is to be
     * included by the encrypter.
     * @param keyEncCredential The credential that is used to encrypt a
     *          symmetric key
     */
    protected abstract List<KeyEncryptionParameters> getKeyEncryptionParameters(
            final Credential keyEncCredential);

    /**
     * @return A decrypter that uses the dummy credential
     */
    protected final Decrypter getDecrypter() {
        return new Decrypter(
                // we don't know the data decryption key in advance
                null,
                // resolver for key encryption keys
                this.getDecryptionCredentialResolver(),
                // key is to be searched via a RetrievalMethod child of the
                // EncryptedData/KeyInfo, which points via a same-document
                // fragment reference to an EncryptedKey
                // located elsewhere in the document
                new SimpleRetrievalMethodEncryptedKeyResolver());
    }
}

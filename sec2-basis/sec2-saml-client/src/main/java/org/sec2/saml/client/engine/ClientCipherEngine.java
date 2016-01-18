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

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.EncryptedAttribute;
import org.opensaml.saml2.encryption.Decrypter;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.encryption.KeyEncryptionParameters;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.CredentialResolver;
import org.opensaml.xml.security.credential.StaticCredentialResolver;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoCredentialResolver;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.sec2.exceptions.EntityUnknownException;
import org.sec2.saml.SAMLBaseConfig;
import org.sec2.saml.client.exceptions.SecurityProviderException;
import org.sec2.saml.engine.CipherEngine;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.exceptions.CipherEngineException;

/**
 * Cipher engine that supports typical XML encryption operations
 * on the client.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * December 17, 2012
 */
public final class ClientCipherEngine extends CipherEngine {

    /**
     * The SecurityProviderConnector used to access the public keys on the
     * smartcard.
     */
    private final ISecurityProviderConnector connector;

    /**
     * Holds the keyserver's encryption certificate.
     */
    private StaticCredentialResolver encryptionCredentialResolver;

    /**
     * Constructor.
     * @param rootCert A trusted root certificate. Establishing trust to this
     *          certificate is out of scope of this class. The caller has to
     *          make sure that trust is given.
     * @param newSAMLEngine SAMLEngine that provides information about the
     *          entity.
     * @throws CipherEngineException if the encryption algorithm is not
     *          supported
     * @throws EntityUnknownException if the registered user cannot be
     *          determined
     */
    public ClientCipherEngine(final X509Certificate rootCert,
            final SAMLEngine newSAMLEngine)
            throws CipherEngineException, EntityUnknownException {
        super(rootCert, newSAMLEngine);
        connector = SecurityProviderConnectorFactory.
                getSecurityProviderConnector();
    }

    /** {@inheritDoc} */
    @Override
    protected synchronized CredentialResolver
            getEncryptionCredentialResolver() throws CipherEngineException {
        if (encryptionCredentialResolver == null) {
            X509Certificate untrustedCert;
            try {
                untrustedCert = connector.
                        getUntrustedKeyserverEncryptionCertificate();
            } catch (SecurityProviderException e) {
                throw new CipherEngineException(e);
            }
            BasicX509Credential credential = new BasicX509Credential();
            credential.setEntityCertificate(untrustedCert);
            credential.setUsageType(UsageType.SIGNING);
            try {
                if (this.isTrustedCredential(credential)) {
                    encryptionCredentialResolver =
                            new StaticKeyInfoCredentialResolver(credential);
                } else {
                    throw new CipherEngineException("Keyserver's encryption "
                        + "certificate is untrusted");
                }
            } catch (org.opensaml.xml.security.SecurityException e) {
                throw new CipherEngineException(e);
            }
        }
        return encryptionCredentialResolver;
    }

    /** {@inheritDoc} */
    @Override
    protected List<KeyEncryptionParameters> getKeyEncryptionParameters(
            final Credential keyEncCredential) {
        List<KeyEncryptionParameters> keks =
                new ArrayList<KeyEncryptionParameters>();
        KeyEncryptionParameters kekParams = new KeyEncryptionParameters();
        kekParams.setEncryptionCredential(keyEncCredential);
        kekParams.setAlgorithm(SAMLBaseConfig.XML_ENCRYPTION_KEYTRANSPORT_NS);
        keks.add(kekParams);
        return keks;
    }

    /**
     * Decrypts an EncryptedAttribute using a specific key.
     *
     * @param encElement the EncryptedAttribute to decrypt
     * @param key the key to decrypt
     * @return the plaintext Attribute
     * @throws CipherEngineException if something goes wrong while decryption
     */
    public Attribute decrypt(final EncryptedAttribute encElement,
            final Credential key) throws CipherEngineException {
        // we know the key, so no magic resolver needed
        Decrypter decrypter = new Decrypter(
                new StaticKeyInfoCredentialResolver(key), null, null);
        try {
            return decrypter.decrypt(encElement);
        } catch (DecryptionException e) {
            throw new CipherEngineException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected boolean checkCertificateInfo(final X509Certificate cert) {
        boolean result = super.checkCertificateInfo(cert);
        //Logger logger = getLogger();
        //TODO: more checks
        return result;
    }
}

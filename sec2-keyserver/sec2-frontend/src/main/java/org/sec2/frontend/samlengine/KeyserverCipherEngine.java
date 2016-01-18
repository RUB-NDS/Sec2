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
package org.sec2.frontend.samlengine;

import java.security.Key;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import javax.crypto.SecretKey;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.EncryptedAttribute;
import org.opensaml.saml2.encryption.Decrypter;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.encryption.EncryptedKey;
import org.opensaml.xml.encryption.KeyEncryptionParameters;
import org.opensaml.xml.security.credential.*;
import org.opensaml.xml.util.Pair;
import org.sec2.saml.engine.CipherEngine;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.exceptions.CipherEngineException;

/**
 * Cipher engine that supports typical XML encryption operations
 * on the keyserver.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * December 17, 2012
 */
public final class KeyserverCipherEngine extends CipherEngine {

    /**
     * Holds the user's encryption certificate.
     */
    private KeyserverBackendKeyInfoCredentialResolver
            encryptionCredentialResolver;

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
    public KeyserverCipherEngine(final X509Certificate rootCert,
            final SAMLEngine newSAMLEngine) throws CipherEngineException {
        super(rootCert, newSAMLEngine);
    }

    /** {@inheritDoc} */
    @Override
    protected synchronized CredentialResolver
            getEncryptionCredentialResolver() throws CipherEngineException {
        if (encryptionCredentialResolver == null) {
            encryptionCredentialResolver =
                    KeyserverBackendKeyInfoCredentialResolver.getInstance();
        }
        return encryptionCredentialResolver;
    }

    /** {@inheritDoc} */
    @Override
    protected List<KeyEncryptionParameters> getKeyEncryptionParameters(
            final Credential keyEncCredential) {
        return Collections.EMPTY_LIST;
    }

    /**
     * Decrypts an EncryptedAttribute.
     *
     * @param encElement the EncryptedAttribute to decrypt
     * @return the plaintext Attribute and the decryption key
     * @throws CipherEngineException if something goes wrong while decryption
     */
    public Pair<Attribute, Credential> decrypt(
            final EncryptedAttribute encElement) throws CipherEngineException {
        Decrypter decrypter = getDecrypter();
        Attribute decElement;
        Key plainKey;

        // Things could be so easy if there was no need to return the key
        try {
            decElement = decrypter.decrypt(encElement);
            // encElement.getEncryptedKeys().size() == 1 is checked by
            // KeyserverRequestVerifier
            EncryptedKey encKey = encElement.getEncryptedKeys().get(0);
            String keyAlgo = encElement.getEncryptedData().
                    getEncryptionMethod().getAlgorithm();
            plainKey = decrypter.decryptKey(encKey, keyAlgo);
        } catch (DecryptionException e) {
            throw new CipherEngineException(e);
        }

        if (!(plainKey instanceof SecretKey)) {
            throw new CipherEngineException("EncryptedKey contains an "
                    + "asymmetric key");
        }

        SecretKey key = (SecretKey) plainKey;
        BasicCredential cred = new BasicCredential();
        cred.setSecretKey(key);
        cred.setUsageType(UsageType.ENCRYPTION);

        return new Pair<Attribute, Credential>(decElement, cred);
    }
}

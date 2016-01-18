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
package org.sec2.saml.engine.mockups;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import org.opensaml.xml.encryption.KeyEncryptionParameters;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.CredentialResolver;
import org.opensaml.xml.security.credential.StaticCredentialResolver;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoCredentialResolver;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.sec2.saml.SAMLBaseConfig;
import org.sec2.saml.engine.CipherEngine;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.exceptions.CipherEngineException;
import org.sec2.statictestdata.TestKeyProvider;

/**
 * A mockup for a (client's) CipherEngine for testing the
 * abstract super class.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * December 05, 2012
 */
public final class MockupCipherEngine extends CipherEngine {

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
     */
    public MockupCipherEngine(final X509Certificate rootCert,
            final SAMLEngine newSAMLEngine) throws CipherEngineException {
        super(rootCert, newSAMLEngine);
    }

    /** {@inheritDoc} */
    @Override
    protected synchronized CredentialResolver
            getEncryptionCredentialResolver() throws CipherEngineException {
        if (encryptionCredentialResolver == null) {
            BasicX509Credential credential = new BasicX509Credential();
            credential.setEntityCertificate(
                    TestKeyProvider.getInstance().getKeyserverEncCert());
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
}

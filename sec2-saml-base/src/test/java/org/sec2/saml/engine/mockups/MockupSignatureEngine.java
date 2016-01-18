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

import java.security.cert.*;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoCredentialResolver;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.engine.SignatureEngine;
import org.sec2.saml.exceptions.SignatureEngineException;
import org.sec2.statictestdata.TestKeyProvider;

/**
 * A mockup for a (client's) SignatureEngine for testing the abstract super
 * class.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * December 05, 2012
 */
public final class MockupSignatureEngine extends SignatureEngine {

    /**
     * CredentialResolver that will be used by the signature trust engine to
     * resolve keys.
     */
    private StaticKeyInfoCredentialResolver signatureKeyInfoResolver;

    /**
     * Constructor.
     *
     * @param rootCert A trusted root certificate. Establishing trust to this
     * certificate is out of scope of this class. The caller has to make sure
     * that trust is given.
     * @param newSAMLEngine SAMLEngine that provides information about the
     * entity.
     * @throws SignatureEngineException if the root certificate cannot be set
     */
    public MockupSignatureEngine(final X509Certificate rootCert,
            final SAMLEngine newSAMLEngine) throws SignatureEngineException {
        super(rootCert, newSAMLEngine);
        TestKeyProvider keys = TestKeyProvider.getInstance();
        BasicCredential cred = new BasicCredential();
        cred.setEntityId(keys.getUserIDBase64());
        cred.setUsageType(UsageType.SIGNING);
        cred.setPublicKey(keys.getUserSignKey().getPublic());
        cred.setPrivateKey(keys.getUserSignKey().getPrivate());
        this.setSignCredential(cred);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized KeyInfoCredentialResolver getSignatureKeyInfoResolver() throws SignatureEngineException {
        if (signatureKeyInfoResolver == null) {
            BasicX509Credential credential = new BasicX509Credential();
            credential.setEntityCertificate(
                    TestKeyProvider.getInstance().getKeyserverSignCert());
            credential.setUsageType(UsageType.SIGNING);
            try {
                if (this.isTrustedCredential(credential)) {
                    signatureKeyInfoResolver =
                            new StaticKeyInfoCredentialResolver(credential);
                } else {
                    throw new SignatureEngineException("Keyserver's signature "
                            + "certificate is untrusted");
                }
            } catch (org.opensaml.xml.security.SecurityException e) {
                throw new SignatureEngineException(e);
            }
        }
        return signatureKeyInfoResolver;
    }
}

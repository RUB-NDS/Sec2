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

import java.security.cert.*;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoCredentialResolver;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.sec2.exceptions.EntityUnknownException;
import org.sec2.saml.client.exceptions.SecurityProviderException;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.engine.SignatureEngine;
import org.sec2.saml.exceptions.SignatureEngineException;

/**
 * Signature engine that supports typical XML signature operations
 * on the client.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * Oktober 29, 2012
 */
public final class ClientSignatureEngine extends SignatureEngine {

    /**
     * The SecurityProviderConnector used to access the public keys on the
     * smartcard.
     */
    private final ISecurityProviderConnector connector;

    /**
     * CredentialResolver that will be used by the signature trust engine
     * to resolve keys.
     */
    private StaticKeyInfoCredentialResolver signatureKeyInfoResolver;

    /**
     * Constructor.
     *
     * @param rootCert A trusted root certificate. Establishing trust to this
     *          certificate is out of scope of this class. The caller has to
     *          make sure that trust is given.
     * @param newSAMLEngine SAMLEngine that provides information about the
     *          entity.
     * @throws EntityUnknownException if the registered user cannot be
     *          determined
     * @throws SignatureEngineException if the root certificate cannot be set
     */
    public ClientSignatureEngine(final X509Certificate rootCert,
            final SAMLEngine newSAMLEngine)
            throws EntityUnknownException, SignatureEngineException {
        super(rootCert, newSAMLEngine);
        connector = SecurityProviderConnectorFactory.
                getSecurityProviderConnector();
        try {
            BasicCredential cred = new BasicCredential();
            cred.setEntityId(this.getEntityIDBase64());
            cred.setUsageType(UsageType.SIGNING);
            cred.setPublicKey(connector.getSignaturePublicKey());
            cred.setPrivateKey(connector.getSignaturePrivateKey());
            this.setSignCredential(cred);
        } catch (UnsupportedOperationException e) {
            this.setSignCredential(createDummyCredential(
                    this.getEntityIDBase64()));
        } catch (SecurityProviderException e) {
            throw new SignatureEngineException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected synchronized KeyInfoCredentialResolver
            getSignatureKeyInfoResolver() throws SignatureEngineException {
        if (signatureKeyInfoResolver == null) {
            X509Certificate untrustedCert;
            try {
                untrustedCert = connector.
                        getUntrustedKeyserverSignatureCertificate();
            } catch (SecurityProviderException e) {
                throw new SignatureEngineException(e);
            }
            BasicX509Credential credential = new BasicX509Credential();
            credential.setEntityCertificate(untrustedCert);
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

    /** {@inheritDoc} */
    @Override
    protected boolean checkCertificateInfo(final X509Certificate cert) {
        boolean result = super.checkCertificateInfo(cert);
        //Logger logger = getLogger();
        //TODO: more checks
        return result;
    }
}

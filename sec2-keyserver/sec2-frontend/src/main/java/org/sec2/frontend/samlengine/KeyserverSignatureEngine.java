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

import java.security.cert.*;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.signature.SignatureTrustEngine;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.engine.SignatureEngine;
import org.sec2.saml.exceptions.SignatureEngineException;

/**
 * Signature engine that supports typical XML signature operations
 * on the keyserver.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 07, 2012
 */
public final class KeyserverSignatureEngine extends SignatureEngine {

    /**
     * CredentialResolver that will be used by the signature trust engine
     * to resolve keys.
     */
    private KeyserverBackendKeyInfoCredentialResolver signatureKeyInfoResolver;

    /**
     * A trust engine that checks XML signatures and tries to establish trust
     * to the credentials used.
     */
    private SignatureTrustEngine signatureTrustEngine;

    /**
     * Constructor.
     *
     * @param rootCert A trusted root certificate. Establishing trust to this
     *          certificate is out of scope of this class. The caller has to
     *          make sure that trust is given.
     * @param newSAMLEngine SAMLEngine that provides information about the
     *          entity.
     * @throws SignatureEngineException if the root certificate cannot be set
     */
    public KeyserverSignatureEngine(final X509Certificate rootCert,
            final SAMLEngine newSAMLEngine) throws SignatureEngineException {
        super(rootCert, newSAMLEngine);
        this.setSignCredential(
                this.createDummyCredential(this.getEntityIDBase64()));
    }

    /** {@inheritDoc} */
    @Override
    protected synchronized SignatureTrustEngine getSignatureTrustEngine()
            throws SignatureEngineException {
        if (this.signatureTrustEngine == null) {
            this.signatureTrustEngine = new ExplicitKeySignatureTrustEngine(
                    /* using the same resolver for untrusted and trusted
                     * credentials basically deactivates the TrustEngine's
                     * functionality. As long as there is no other way to
                     * establish trust to the certificates, this is the painless
                     * way to circumvent a complete refactoring of the
                     * SAML engine's architecture */
                    this.getSignatureKeyInfoResolver(),
                    this.getSignatureKeyInfoResolver());
        }
        return this.signatureTrustEngine;
    }

    /** {@inheritDoc} */
    @Override
    protected synchronized KeyInfoCredentialResolver
            getSignatureKeyInfoResolver() throws SignatureEngineException {
        if (signatureKeyInfoResolver == null) {
            signatureKeyInfoResolver =
                    KeyserverBackendKeyInfoCredentialResolver.getInstance();
        }
        return signatureKeyInfoResolver;
    }
}

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

import java.security.NoSuchAlgorithmException;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.sec2.statictestdata.TestKeyProvider;

/**
 * A mockup for a (client's) SAML engine for testing the abstract super class.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * December 05, 2012
 */
public final class MockupSAMLEngine extends SAMLEngine {

    /**
     * Signature engine that supports typical XML signature operations.
     */
    private final MockupSignatureEngine signatureEngine;

    /**
     * Cipher engine that supports typical XML encryption operations.
     */
    private final MockupCipherEngine cipherEngine;

    /**
     * Generator for prepared SAML elements.
     */
    private final MockupPreparedElementGenerator prepElementGenerator;

    /**
     * Singleton constructor.
     * @throws SAMLEngineException hopefully, it does not ;)
     */
    private MockupSAMLEngine() throws SAMLEngineException {
        super();
        this.setTrustedRootCertificate(
                TestKeyProvider.getInstance().getKeyserverSignCert());
        // create sub-engines
        try {
            signatureEngine =
                    new MockupSignatureEngine(
                            getTrustedRootCertificate(), this);
            cipherEngine =
                    new MockupCipherEngine(
                            getTrustedRootCertificate(), this);
            prepElementGenerator =
                    new MockupPreparedElementGenerator(cipherEngine);
        } catch (NoSuchAlgorithmException e) {
            throw new SAMLEngineException(e);
        }
    }

    /**
     * Singleton getter.
     * @return The singleton instance
     * @throws SAMLEngineException hopefully, it does not ;)
     */
    public static MockupSAMLEngine getInstance() throws SAMLEngineException {
        if (ClientSAMLEngineHolder.samlEngineException != null) {
            throw ClientSAMLEngineHolder.samlEngineException;
        }
        return ClientSAMLEngineHolder.instance;
    }

    /** {@inheritDoc} */
    @Override
    public MockupSignatureEngine getSignatureEngine() {
        return signatureEngine;
    }

    /** {@inheritDoc} */
    @Override
    public MockupCipherEngine getCipherEngine() {
        return cipherEngine;
    }

    /** {@inheritDoc} */
    @Override
    public MockupPreparedElementGenerator getPreparedElementGenerator() {
        return prepElementGenerator;
    }

    /** {@inheritDoc} */
    @Override
    public byte[] getEntityID() {
        return TestKeyProvider.getInstance().getUserID();
    }

    /** {@inheritDoc} */
    @Override
    public String getEntityIDBase64() {
        return TestKeyProvider.getInstance().getUserIDBase64();
    }

    /**
     * Nested class holding Singleton instance.
     */
    private static class ClientSAMLEngineHolder {
        /**
         * The singleton instance.
         */
        private static MockupSAMLEngine instance;

        /**
         * SAMLEngineException that might have been thrown during
         * creation of MockupSAMLEngine instance.
         */
        private static SAMLEngineException samlEngineException;

        static {
            try {
                instance = new MockupSAMLEngine();
            } catch (SAMLEngineException e) {
                e.log();
                samlEngineException = e;
            }
        }
    }
 }

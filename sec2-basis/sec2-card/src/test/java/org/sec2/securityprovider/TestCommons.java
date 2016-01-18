/*
 * Copyright 2011 Sec2 Consortium
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit written
 * permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://www.sec2.org
 */
package org.sec2.securityprovider;

import java.security.Provider;
import java.util.ArrayList;
import java.util.List;
import org.sec2.securityprovider.serviceparameter.CipherAlgorithm;
import org.sec2.securityprovider.serviceparameter.DigestAlgorithm;
import org.sec2.securityprovider.serviceparameter.PaddingAlgorithm;
import org.sec2.securityprovider.serviceparameter.SignatureAlgorithm;

/**
 * Commonly used objects by the unit tests.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1 Jun 10, 2011
 */
public abstract class TestCommons {

    /**
     * Associated provider.
     */
    private Provider provider = null;
    /**
     * ************************************************************************
     * Algorithms used in unit tests *
     * ************************************************************************
     */
    private static List<DigestAlgorithm> digestAlgorithms;
    private static List<CipherAlgorithm> cipherAlgorithms;
    private static List<PaddingAlgorithm> paddingAlgorithms;
    private static List<SignatureAlgorithm> signatureAlgorithms;

    /**
     * ************************************************************************
     * Initialization *
     * ************************************************************************
     */
    /**
     * Static initalization.
     */
    static {
        digestAlgorithms = new ArrayList<DigestAlgorithm>(3);
        getDigestAlgorithms().add(DigestAlgorithm.MD5);
        getDigestAlgorithms().add(DigestAlgorithm.SHA1);
        getDigestAlgorithms().add(DigestAlgorithm.SHA256);
    }

    static {
        cipherAlgorithms = new ArrayList<CipherAlgorithm>(2);
        getCipherAlgorithms().add(CipherAlgorithm.AES);
        getCipherAlgorithms().add(CipherAlgorithm.RSA);
    }

    static {
        paddingAlgorithms = new ArrayList<PaddingAlgorithm>(3);
        getPaddingAlgorithms().add(PaddingAlgorithm.NONE);
        getPaddingAlgorithms().add(PaddingAlgorithm.PKCS7);
        getPaddingAlgorithms().add(PaddingAlgorithm.ZERO);
    }

    static {
        signatureAlgorithms = new ArrayList<SignatureAlgorithm>(3);
        getSignatureAlgorithms().add(SignatureAlgorithm.MD5withRSA);
        getSignatureAlgorithms().add(SignatureAlgorithm.SHA1withRSA);
        getSignatureAlgorithms().add(SignatureAlgorithm.SHA256withRSA);
    }

    /**
     * Protected constrcutor - utility class only.
     */
    protected TestCommons() {
    }

    /**
     * ************************************************************************
     * Getter *
     * ************************************************************************
     */
    /**
     * Getter for the provider.
     *
     * @return Configured provider.
     */
    public Provider getProvider() {
        return provider;
    }

    /**
     * Getter for the digest algorithms.
     *
     * @return Digest algorithsm that should be tested.
     */
    public static List<DigestAlgorithm> getDigestAlgorithms() {
        return digestAlgorithms;
    }

    /**
     * Getter for the Cipher algorithms.
     *
     * @return CipherAlgorithsm that should be tested.
     */
    public static List<CipherAlgorithm> getCipherAlgorithms() {
        return cipherAlgorithms;
    }

    /**
     * Getter for the PaddingAlgorithms.
     *
     * @return PaddingAlgorithsm that should be tested.
     */
    public static List<PaddingAlgorithm> getPaddingAlgorithms() {
        return paddingAlgorithms;
    }

    /**
     * Getter for the Signature algorithms.
     *
     * @return Signature algorithsm that should be tested.
     */
    public static List<SignatureAlgorithm> getSignatureAlgorithms() {
        return signatureAlgorithms;
    }

    /**
     * ************************************************************************
     * Setter *
     * ************************************************************************
     */
    /**
     * Setter for the provider.
     *
     * @param provider Provider to be configured.
     */
    protected void setProvider(Provider provider) {
        this.provider = provider;
    }
}

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
package org.sec2.securityprovider.serviceparameter;

/**
 * Enum of all supported signature algorithms.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 * Nov 28, 2011
 */
public enum SignatureAlgorithm implements IServiceParameter {

    /**
     * RSA SHA1 algorithm.
     */
    SHA1withRSA(CipherAlgorithm.RSA, DigestAlgorithm.SHA1),
    /**
     * RSA SHA256 algorithm.
     */
    SHA256withRSA(CipherAlgorithm.RSA, DigestAlgorithm.SHA256),
    /**
     * RSA MD5 algorithm.
     */
    MD5withRSA(CipherAlgorithm.RSA, DigestAlgorithm.MD5);
    /**
     * Digest algorithm of this enum value.
     */
    private final DigestAlgorithm digestAlgorithm;
    /**
     * Cipher algorithm of this enum value.
     */
    private final CipherAlgorithm cipherAlgorithm;

    /**
     * Initializes the enum.
     *
     * @param cipher Cipher algorithm of this enum value
     * @param digest Digest algorithm of this enum value
     */
    private SignatureAlgorithm(final CipherAlgorithm cipher,
            final DigestAlgorithm digest) {
        this.cipherAlgorithm = cipher;
        this.digestAlgorithm = digest;
    }

    /**
     * Get the digest algorithm.
     *
     * @return Digest algorithm
     */
    public DigestAlgorithm getDigestAlgorithm() {
        return this.digestAlgorithm;
    }
}

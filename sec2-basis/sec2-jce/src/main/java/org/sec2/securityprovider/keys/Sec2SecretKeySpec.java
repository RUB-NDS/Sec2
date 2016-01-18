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
package org.sec2.securityprovider.keys;

import java.security.spec.KeySpec;
import org.sec2.securityprovider.serviceparameter.CipherAlgorithm;

/**
 * Key specification for cluster/document key creation.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 * Dec 7, 2011
 */
public final class Sec2SecretKeySpec implements KeySpec {

    /**
     * Key length.
     */
    private final int keyLength;
    /**
     * Desired algorithm.
     */
    private final CipherAlgorithm cipherAlgorithm;
    /**
     * Key type - only Document or Cluster key type are valid.
     */
    private final KeyType keyType;

    /**
     * Initializes a Sec2 secret key spec.
     *
     * @param length Key length
     * @param algorithm Cipher algorithm
     * @param type Key type
     */
    public Sec2SecretKeySpec(final int length, final CipherAlgorithm algorithm,
            final KeyType type) {
        this.keyLength = length;
        // deep copying unnecessary....

        if (algorithm == null) {
            throw new IllegalArgumentException("Algorithm MUST not be NULL.");
        }
        if (!(algorithm.name().equals(CipherAlgorithm.AES.name()))) {
            throw new IllegalArgumentException("Only AES is possible for"
                    + " Cluster and Document- Keys");
        }
        this.cipherAlgorithm = algorithm;

        if (type == null
                || (type != KeyType.CLUSTER && type != KeyType.DOCUMENT)) {
            throw new IllegalArgumentException(
                    "Only Cluster or Document key types are allowed.");
        }
        this.keyType = type;
    }

    /**
     * Getter for the key length.
     *
     * @return Key length
     */
    public int getKeyLength() {
        return keyLength;
    }

    /**
     * Getter for the cipher algorithm.
     *
     * @return Cipher algorithm
     */
    public CipherAlgorithm getCipherAlgorithm() {
        return cipherAlgorithm;
    }

    /**
     * Getter for the key type.
     *
     * @return Key type
     */
    public KeyType getKeyType() {
        return keyType;
    }
}

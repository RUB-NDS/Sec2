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
package org.sec2.token;

/**
 * Enum of all supported cipher algorithms.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 * Nov 28, 2011
 */
public enum CipherAlgorithm {

    /**
     * RSA algorithm.
     */
    RSA(1),
    /**
     * AES algorithm.
     */
    AES(16);
    /**
     * Block size of this cipher in bytes.
     */
    private int blockSize;

    /**
     * Create a CipherAlgorithm value with the given block size.
     *
     * @param blocksize Block size of the cipher or 0 if this value is not a
     * block cipher
     */
    private CipherAlgorithm(final int blocksize) {
        this.blockSize = blocksize;
    }

    /**
     * Gets the block size of this cipher.
     *
     * @return Block size of this cipher or 0 if this is not a block cipher
     */
    public int getBlockSize() {
        return this.blockSize;
    }

    /**
     * Get the cipher algorithm with the handled ID.
     *
     * @param id ID of the desired cipher algorithm - can be access via
     * ordinal().
     *
     * @return Key type associated with the handled ID
     */
    public static CipherAlgorithm getCipherAlgorithm(final byte id) {
        CipherAlgorithm[] algos = values();

        if (id >= algos.length) {
            throw new IllegalArgumentException(
                    "No cipher algorithm with this id");
        }

        return algos[id];
    }
}

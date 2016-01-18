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

import org.sec2.securityprovider.crypto.IPadding;
import org.sec2.securityprovider.crypto.NoPadding;
import org.sec2.securityprovider.crypto.PKCS7Padding;
import org.sec2.securityprovider.crypto.ZeroPadding;

/**
 * Enum of all supported message padding algorithms.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1 Feb 17, 2012
 */
public enum PaddingAlgorithm implements IServiceParameter {

    /**
     * No padding.
     */
    NONE(new NoPadding()),
    /**
     * Zero padding (padding contains only zeros).
     */
    ZERO(new ZeroPadding()),
    /**
     * PKCS7 padding (number of padded bytes).
     */
    PKCS7(new PKCS7Padding());
    /**
     * Padding algorithm.
     */
    private IPadding paddingAlgorithm;

    /**
     * Constructor for the padding algorithm.
     *
     * @param algorithm Chosen padding algorithm implementation.
     */
    private PaddingAlgorithm(final IPadding algorithm) {
        paddingAlgorithm = algorithm;
    }

    /**
     * Prepares a padding block of the requested size.
     *
     * @param blocks Number of blocks (bytes)
     * @return Padding
     */
    public byte[] pad(final int blocks) {
        return paddingAlgorithm.pad(blocks);
    }
}

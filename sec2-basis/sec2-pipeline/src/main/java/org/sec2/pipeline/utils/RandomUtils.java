/*
 * Copyright 2011 Ruhr-University Bochum, Chair for Network and Data Security
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://nds.rub.de/research/projects/sec2/
 */
package org.sec2.pipeline.utils;

import java.security.SecureRandom;
import org.sec2.pipeline.datatypes.XMLSecurityConstants;

/**
 *
 * @author Jan Temme - jan.temme@rub.de version 0.1
 */
public final class RandomUtils {

    /**
     * SecureRandom for generating Nonces
     */
    private SecureRandom sr;
    /**
     * Singleton reference
     */
    private static volatile RandomUtils instance = null;

    /**
     * Private Constructor
     */
    private RandomUtils() {
        sr = new SecureRandom();
    }

    /**
     *
     * @return Instance of a EncryptedKeyProcessor
     */
    public static RandomUtils getInstance() {
        if (instance == null) {
            instance = new RandomUtils();
        }
        return instance;
    }

    /**
     * Generate initialization vector of an appropriate size
     *
     * @param algorithm
     * @return
     */
    public byte[] generateIV(final String algorithm) throws IllegalArgumentException {
        byte[] iv;
        XMLSecurityConstants.Algorithm a = XMLSecurityConstants.Algorithm.fromString(algorithm);
        if (a == XMLSecurityConstants.Algorithm.AES128CBC
                || a == XMLSecurityConstants.Algorithm.AES192CBC
                || a == XMLSecurityConstants.Algorithm.AES256CBC) {
            iv = new byte[16];
        } else if (a == XMLSecurityConstants.Algorithm.AES128GCM
                || a == XMLSecurityConstants.Algorithm.AES192GCM
                || a == XMLSecurityConstants.Algorithm.AES256GCM) {
            iv = new byte[12];
        } else {
            throw new IllegalArgumentException("The algorithm " + algorithm
                    + " is not supported.");
        }
        sr.nextBytes(iv);
        return iv;
    }
}

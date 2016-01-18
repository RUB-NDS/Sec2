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
package org.sec2.saml.engine;

import java.security.NoSuchAlgorithmException;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;

/**
 * Generates identifiers using random data obtained from a
 * {@link java.security.SecureRandom} instance and attaches a prefix.
 * Singleton implementation to prevent that somebody continously creates
 * new instances resulting in a freshly seeded
 * {@link java.security.SecureRandom} instance.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * Oktober 30, 2012
 */
public final class PrefixedIdentifierGenerator extends
        SecureRandomIdentifierGenerator {

    /**
     * Byte-Length of the random part of an XML ID, default: 16.
     */
    private static final int ID_RANDOMPART_LENGTH = 16;

    /**
     * The assumed length of a prefix string, default: 16.
     * Used for the intial size of a StringBuilder's buffer. Not a critical
     * value since a StringBuilder enlarges its buffer dynamically if needed.
     * A prefix is assumend to have max. 16 chars.
     */
    private static final int ID_PREFIX_LENGTH = 16;

    /**
     * Singleton constructor.
     *
     * @throws NoSuchAlgorithmException thrown if the SHA1PRNG algorithm
     *          is not supported by the JVM
     */
    private PrefixedIdentifierGenerator() throws NoSuchAlgorithmException {
        super();
    }

    /**
     * Singleton getter.
     * @return The singleton instance
     * @throws NoSuchAlgorithmException thrown if the SHA1PRNG algorithm
     *          is not supported by the JVM
     */
    public static PrefixedIdentifierGenerator getInstance()
            throws NoSuchAlgorithmException {
        if (PrefixedIdentifierGeneratorHolder.exception != null) {
            throw PrefixedIdentifierGeneratorHolder.exception;
        }
        return PrefixedIdentifierGeneratorHolder.instance;
    }

    /**
     * Generates a 16 byte random identifier with a prefix.
     * If prefix is null, an underscore is used as prefix to prevent
     * an ID that begins with a digit which would be an invalid XML id.
     * If prefix itsself begins with a digit, another underscore is added.
     * @param prefix A prefix that is put in front of the id, separated with
     * an underscore ('_').
     * @return the random id
     */
    public String generatePrefixedIdentifier(final String prefix) {
        return this.generatePrefixedIdentifier(ID_RANDOMPART_LENGTH, prefix);
    }

    /**
     * Generates a random identifier with a prefix.
     * If prefix is null, an underscore is used as prefix to prevent
     * an ID that begins with a digit which would be an invalid XML id.
     * If prefix itsself begins with a digit, another underscore is added.
     * @param size number of bytes in the identifier
     * @param prefix A prefix that is put in front of the id, separated with
     * an underscore ('_').
     * @return the random id
     */
    public String generatePrefixedIdentifier(final int size,
            final String prefix) {
        // Remember that this is a hex-string where every byte forms 2 chars
        StringBuilder builder = new StringBuilder(
                ID_PREFIX_LENGTH + size * 2);
        if (prefix != null) {
            // Prevent a prefix with a digit as first char
            if (Character.isDigit(prefix.charAt(0))) {
                builder.append('_');
            }
            builder.append(prefix);
        }
        builder.append(this.generateIdentifier(size));
        return builder.toString();
    }

    /**
     * Nested class holding Singleton instance.
     */
    private static class PrefixedIdentifierGeneratorHolder {
        /**
         * The singleton instance.
         */
        private static PrefixedIdentifierGenerator instance;

        /**
         * NoSuchAlgorithmException that might have been thrown during
         * creation of the PrefixedIdentifierGenerator.
         */
        private static NoSuchAlgorithmException exception;

        static {
            try {
                instance = new PrefixedIdentifierGenerator();
            } catch (NoSuchAlgorithmException e) {
                exception = e;
            }
        }
    }
 }

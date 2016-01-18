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
package org.sec2.pipeline.datatypes;

import org.sec2.core.XMLConstants;

/**
 * XML Security specification constants
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @date Aug 20, 2012
 * @version 0.1
 *
 */
public final class XMLSecurityConstants {

    /**
     * enum considering algorithms and their URIs
     *
     */
    public enum Algorithm {

        AES128CBC("AES/CBC/NoPadding", AES_128_CBC, XMLConstants.AES_128_CBC, false),
        AES192CBC("AES/CBC/NoPadding", AES_192_CBC, XMLConstants.AES_192_CBC, false),
        AES256CBC("AES/CBC/NoPadding", AES_256_CBC, XMLConstants.AES_256_CBC, false),
        AES128GCM("AES/GCM/NoPadding", AES_128_GCM, XMLConstants.AES_128_GCM, true),
        AES192GCM("AES/GCM/NoPadding", AES_192_GCM, XMLConstants.AES_192_GCM, true),
        AES256GCM("AES/GCM/NoPadding", AES_256_GCM, XMLConstants.AES_256_GCM, true);
        /**
         * java algorithm URI
         */
        public final String javaURI;
        /**
         * xml encryption algorithm URI
         */
        public final String xmlURI;
        /**
         * sec2 URI
         */
        public final String sec2URI;
        /**
         * streaming
         */
        public final boolean isStreaming;
        /**
         * secret key specification URI (AES constant), could be extended when
         * including new algorithms
         */
        public final String secretKeySpecURI = "AES";

        /**
         * Encryption algorithm
         *
         * @param javaURI
         * @param xmlURI
         * @param sec2URI
         */
        Algorithm(final String javaURI, final String xmlURI,
                final String sec2URI, final boolean isStreaming) {
            this.javaURI = javaURI;
            this.xmlURI = xmlURI;
            this.sec2URI = sec2URI;
            this.isStreaming = isStreaming;
        }

        /**
         * returns algorithm from a URI string
         *
         * @param uri
         * @return
         */
        public static Algorithm fromString(final String uri) {
            if (uri != null) {
                for (Algorithm a : Algorithm.values()) {
                    if (uri.equals(a.javaURI) || uri.equals(a.xmlURI)
                            || uri.equals(a.sec2URI)) {
                        return a;
                    }
                }
            }
            throw new IllegalArgumentException("No Algorithm with URI " + uri
                    + " could be found.");
        }
    }
    /**
     * XML Encryption prefix
     */
    public static final String XMLENC_PREFIX = "xenc11";
    /**
     * XML Encryption namespace
     */
    public static final String XMLENC_NS = "http://www.w3.org/2009/xmlenc11#";
    /**
     * XML Signature prefix
     */
    public static final String XMLSIG_PREFIX = "ds";
    /**
     * XML Signature namespace
     */
    public static final String XMLSIG_NS = "http://www.w3.org/2000/09/xmldsig#";
    /**
     * AES 128 GCM URI
     */
    public static final String AES_128_GCM =
            "http://www.w3.org/2009/xmlenc11#aes128-gcm";
    /**
     * AES 192 GCM URI
     */
    public static final String AES_192_GCM =
            "http://www.w3.org/2009/xmlenc11#aes192-gcm";
    /**
     * AES 256 GCM URI
     */
    public static final String AES_256_GCM =
            "http://www.w3.org/2009/xmlenc11#aes256-gcm";
    /**
     * AES 128 CBC URI
     */
    public static final String AES_128_CBC =
            "http://www.w3.org/2001/04/xmlenc#aes128-cbc";
    /**
     * AES 192 CBC URI
     */
    public static final String AES_192_CBC =
            "http://www.w3.org/2001/04/xmlenc#aes192-cbc";
    /**
     * AES 256 CBC URI
     */
    public static final String AES_256_CBC =
            "http://www.w3.org/2001/04/xmlenc#aes1256-cbc";
//    /**
//     * AES 128 Key Wrap URI
//     */
//    public static final String AES_128_KW =
//            "http://www.w3.org/2001/04/xmlenc#kw-aes128";
//    /**
//     * AES 192 Key Wrap URI
//     */
//    public static final String AES_192_KW =
//            "http://www.w3.org/2001/04/xmlenc#kw-aes192";
//    /**
//     * AES 256 Key Wrap URI
//     */
//    public static final String AES_256_KW =
//            "http://www.w3.org/2001/04/xmlenc#kw-aes256";

    private XMLSecurityConstants() {
    }
}

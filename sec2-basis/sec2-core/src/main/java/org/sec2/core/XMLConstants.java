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

package org.sec2.core;

/**
 * Constants in the Sec2 spec
 *
 * @author  Juraj Somorovsky - juraj.somorovsky@rub.de
 * @date    Aug 17, 2012
 * @version 0.1
 *
 */
public final class XMLConstants {
    
    /** sec2 Algorithm attribute */
    public static final String SEC2_ALGORITHM = "Algorithm";
    
    /** sec2 Groups element */
    public static final String SEC2_GROUPS = "groups";

    /** sec2 middleware namespace */
    public static final String SEC2_NS = "http://sec2.org/2012/03/middleware";
    
    /** sec2 middleware prefix */
    public static final String SEC2_PREFIX = "sec2";
    
    /** AES128 KW for keywrap, AES128 GCM for data encryption, C14-X
     * Canonicalization
     */
    public static final String AES_128_GCM = "AES128GCM";

    /** AES192 KW for keywrap, AES192 GCM for data encryption, C14-X
     * Canonicalization
     */
    public static final String AES_192_GCM = "AES192GCM";

    /** AES256 KW for keywrap, AES256 GCM for data encryption, C14-X
     * Canonicalization
     */
    public static final String AES_256_GCM = "AES256GCM";

    /** AES128 KW for keywrap, AES128 CBC for data encryption, C14-X
     * Canonicalization
     */
    public static final String AES_128_CBC = "AES128CBC";

    /** AES192 KW for keywrap, AES192 CBC for data encryption, C14-X
     * Canonicalization
     */
    public static final String AES_192_CBC = "AES192CBC";

    /** AES256 KW for keywrap, AES256 CBC for data encryption, C14-X
     * Canonicalization
     */
    public static final String AES_256_CBC = "AES256CBC";

    /** default algorithm */
    public static final String DEFAULT_ALGORITHM = AES_128_CBC;
}

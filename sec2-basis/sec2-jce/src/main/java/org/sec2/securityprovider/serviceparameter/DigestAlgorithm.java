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
 * Enum of all supported message digest algorithms.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 * Jun 10, 2011
 */
public enum DigestAlgorithm implements IServiceParameter {

    /**
     * SHA1 algorithm.
     */
    SHA1,
    /**
     * SHA256 algorithm.
     */
    SHA256,
    /**
     * MD5 algorithm.
     */
    MD5;
}

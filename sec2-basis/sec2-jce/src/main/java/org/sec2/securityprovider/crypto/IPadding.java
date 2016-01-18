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
package org.sec2.securityprovider.crypto;

/**
 * Padding algorithm interface.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1 Feb 17, 2012
 */
public interface IPadding {

    /**
     * Prepares a padding block of the requested size.
     *
     * @param blocks Number of blocks (bytes)
     * @return Padding
     */
    byte[] pad(final int blocks);
}

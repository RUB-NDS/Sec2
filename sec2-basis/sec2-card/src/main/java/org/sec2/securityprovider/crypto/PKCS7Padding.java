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
 * Implementation of the PKCS#7 padding.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 * Feb 17, 2012
 */
public final class PKCS7Padding implements IPadding {

    @Override
    public byte[] pad(final int blocks) {
        byte[] result = new byte[blocks];

        // TODO perhaps check for maximum padding size?
        for (int i = 0; i < blocks; i++) {
            result[i] = (byte) blocks;
        }

        return result;
    }
}

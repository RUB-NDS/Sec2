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
 * Supported operation modes.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 * Nov 30, 2011
 */
public enum CipherOperationMode implements IServiceParameter {

    /**
     * CBC mode.
     */
    CBC;

    /**
     * Get the cipher operation mode with the handled ID.
     *
     * @param id ID of the desired cipher operation mode - can be access via
     * ordinal()
     *
     * @return Cipher operation mode associated with the handled ID
     */
    public static CipherOperationMode getCipherOperationMode(final int id) {
        CipherOperationMode[] mode = values();

        if (id >= mode.length) {
            throw new IllegalArgumentException(
                    "No operation mode with this id");
        }

        return mode[id];
    }
}

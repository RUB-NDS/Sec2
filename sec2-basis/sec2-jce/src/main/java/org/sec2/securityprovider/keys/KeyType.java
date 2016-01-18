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
package org.sec2.securityprovider.keys;

/**
 * Enum of all supported key types.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1 Dec 5, 2011
 */
public enum KeyType {

    /**
     * Asym. Public key.
     */
    PUBLIC,
    /**
     * Asym. Private key.
     */
    PRIVATE,
    /**
     * Sym. Cluster key.
     */
    CLUSTER,
    /**
     * Sym. Document key.
     */
    DOCUMENT;

    /**
     * Get the key type with the handled ID.
     *
     * @param id ID of the desired key type - can be access via ordinal()
     *
     * @return Key type associated with the handled ID
     */
    public static KeyType getKeyType(final byte id) {
        KeyType[] type = values();

        if (id >= type.length) {
            throw new IllegalArgumentException("No key type with this id");
        }

        return type[id];
    }
}

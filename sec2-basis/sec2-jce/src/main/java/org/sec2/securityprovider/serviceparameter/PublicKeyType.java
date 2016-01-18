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
 * Enum of Names used for getting Public Keys from the Card
 * @author jtemme
 * @author tschreiber
 *
 * Sep 11, 2012
 */
public enum PublicKeyType implements IServiceParameter {
    /**
     * The Client's Public Key for Encryption, loaded from token
     */
    CLIENT_ENCRYPTION,  //was Encryption in Jan's Version
    /**
     * The Client's Public Key for Signature, loaded from token
     */
    CLIENT_SIGNATURE, //was Signature in Jan's Version
    /**
     * The Server's Public Key for Signature Verification loaded from token
     */
     SERVER_SIGNATURE, //was Server in Jan's Version
    /**
     * The Server's Public Key for Encryption 
     * The caller has to give this key to the SecurityProvider
     */
     SERVER_ENCRYPTON;
     
    /**
     * Get the key type with the handled ID.
     *
     * @param id ID of the desired key type - can be access via ordinal()
     *
     * @return Key type associated with the handled ID
     */
    public static PublicKeyType getPublicKeyType(final byte id) {
        PublicKeyType[] type = values();

        if (id >= type.length) {
            throw new IllegalArgumentException("No key type with this id");
        }

        return type[id];
    }
    
   /** Get the key type by the handled ID String, but throws no exception
     *
     * @param id  Name of the desired key type
     *
     * @return Keytype or null, if id does not match..
     */
    public static PublicKeyType findConstant(final String id) {
             PublicKeyType e;
            try {
             e= valueOf(id);
             
            } catch (IllegalArgumentException ex){
             e = null;
            }
             
        return e;
    }
}

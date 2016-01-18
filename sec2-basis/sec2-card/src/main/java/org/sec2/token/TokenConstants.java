/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.token;

/**
 *
 * @author benedikt
 */
public class TokenConstants {
    /*
     * Some shared constants.
     */
    // public final static short MAX_ID_LENGTH = 0x08;

    public final static short MAX_GROUPKEYS = 16;
    public final static byte POS_NOT_FOUND = (byte) 0xFF;

    /*
     * Some more lengths..
     */
    public static final byte CBC_BLOCK_LEN = 16;
    public static final byte SHA1_HASH_LEN = 20 + 15;
    public static final byte SHA256_HASH_LEN = 32 + 19;
    public static final byte MD5_HASH_LEN = 16 + 18;
    public static final short MAX_APDU_SIZE = 256;
    public static final byte GKEY_ID_LEN = 8;
    public static final byte GKEY_LEN = 32;
    public static final byte DKEY_LEN = 32;
    public final static byte[] DEFAULT_PIN = {'1', '2', '3', '4', '5'};
    public final static byte[] DEFAULT_PUK = {'7', '6', '5', '4', '3', '2',
        '1', '0'};
    public final static byte PIN_MAX_SIZE = 10;
    public final static byte PIN_MIN_SIZE = 4;
    public final static byte PUK_MAX_SIZE = 10;
    public final static byte PUK_MIN_SIZE = 8;
}

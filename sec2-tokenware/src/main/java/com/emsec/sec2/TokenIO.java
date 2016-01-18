package com.emsec.sec2;

public class TokenIO {
    /*
     * Class of this Applet
     */

    public final static byte CARD_CLA = (byte) 0x80;

    /*
     * UserKey commands.
     */
    private static final byte INS_UKEY_BASE = 0x10;
    public static final byte INS_UKEYS_GENERATE = INS_UKEY_BASE + 0;
    public static final byte INS_UKEYS_SIG_GET_MOD = INS_UKEY_BASE + 1;
    public static final byte INS_UKEYS_SIG_GET_EXP = INS_UKEY_BASE + 2;
    public static final byte INS_UKEYS_SIG_SIGN = INS_UKEY_BASE + 3;
    public static final byte INS_UKEYS_ENC_GET_MOD = INS_UKEY_BASE + 4;
    public static final byte INS_UKEYS_ENC_GET_EXP = INS_UKEY_BASE + 5;
    public static final byte INS_UKEYS_ENC_SIGN = INS_UKEY_BASE + 6;

    /*
     * PIN/PUK commands.
     */
    private static final byte INS_PIN_PUK_BASE = 0x20;
    public static final byte INS_PIN_VALIDATE = INS_PIN_PUK_BASE + 0;
    public static final byte INS_PUK_VALIDATE = INS_PIN_PUK_BASE + 1;
    public static final byte INS_PIN_SET = INS_PIN_PUK_BASE + 2;
    public static final byte INS_PUK_SET = INS_PIN_PUK_BASE + 3;
    public static final byte INS_PIN_LOGOUT = INS_PIN_PUK_BASE + 4;
    public static final byte INS_PUK_LOGOUT = INS_PIN_PUK_BASE + 5;

    /*
     * DocumentKey commands.
     */
    private static final byte INS_DKEY_BASE = 0x30;
    public static final byte INS_DKEY_GENERATE = INS_DKEY_BASE + 0;
    public static final byte INS_DKEY_ENCRYPT = INS_DKEY_BASE + 1;
    public static final byte INS_DKEY_DECRYPT = INS_DKEY_BASE + 2;

    /*
     * ClusterKey commands.
     */
    private static final byte INS_CKEY_BASE = 0x40;
    public static final byte INS_CKEY_FIND = INS_CKEY_BASE + 0;
    public static final byte INS_CKEY_GET_FREE_SLOT = INS_CKEY_BASE + 1;
    public static final byte INS_CKEY_SET_ID = INS_CKEY_BASE + 2;
    public static final byte INS_CKEY_SET_KEY = INS_CKEY_BASE + 3;
    public static final byte INS_CKEY_REMOVE = INS_CKEY_BASE + 4;
    public static final byte INS_CKEY_CLEAR_ALL = INS_CKEY_BASE + 5;
    public static final byte INS_CKEY_ENCRYPT = INS_CKEY_BASE + 6;
    public static final byte INS_CKEY_DECRYPT = INS_CKEY_BASE + 7;
    public static final byte INS_CKEY_GET_IDS = INS_CKEY_BASE + 8;

    /*
     * Random number command.
     */
    private static final byte INS_MISC_BASE = 0x50;
    public static final byte INS_MISC_RND_GET = INS_MISC_BASE + 0;
    public static final byte INS_MISC_VERSION_GET = INS_MISC_BASE + 1;
    public static final byte INS_MISC_STATUS_GET = INS_MISC_BASE + 2;

    /*
     * ServerKey commands.
     */
    private static final byte INS_SKEY_BASE = 0x60;
    public static final byte INS_SKEY_SET_MOD = INS_SKEY_BASE + 0;
    public static final byte INS_SKEY_SET_EXP = INS_SKEY_BASE + 1;
    public static final byte INS_SKEY_GET_MOD = INS_SKEY_BASE + 2;
    public static final byte INS_SKEY_GET_EXP = INS_SKEY_BASE + 3;

    /*
     * Base code for all error codes.
     */
    private final static short SW_BASE = 0x1000;

    /*
     * PIN/PUK errors
     */
    private final static short SW_PIN_PUK_BASE = SW_BASE + 0x0000;
    public final static short SW_PIN_VALIDATION_FAILED = SW_PIN_PUK_BASE + 0;
    public final static short SW_PUK_VALIDATION_FAILED = SW_PIN_PUK_BASE + 1;
    public final static short SW_PIN_PUK_AUTHENTICATION_REQUIRED = SW_PIN_PUK_BASE + 2;
    public final static short SW_PIN_WRONG_SIZE = SW_PIN_PUK_BASE + 3;
    public final static short SW_PUK_WRONG_SIZE = SW_PIN_PUK_BASE + 4;
    public final static short SW_PUK_SET_ONLY_ONCE = SW_PIN_PUK_BASE + 5;

    /*
     * ClusterKey errors
     */
    private final static short SW_CKEY_BASE = SW_BASE + 0x0010;
    public final static short SW_CKEY_NOT_SET = SW_CKEY_BASE + 0;
    public final static short SW_CKEY_WRONG_SIZE = SW_CKEY_BASE + 1;
    public final static short SW_CKEY_WRONG_ID_SIZE = SW_CKEY_BASE + 2;
    public final static short SW_CKEY_SLOT_COUNT_INVALID = SW_CKEY_BASE + 3;
    public final static short SW_CKEY_SLOT_INVALID = SW_CKEY_BASE + 4;
    public final static short SW_CKEY_SLOT_IN_USE = SW_CKEY_BASE + 5;
    public final static short SW_CKEY_SLOT_EMPTY = SW_CKEY_BASE + 6;
    public final static short SW_CKEY_DATA_INVALID = SW_CKEY_BASE + 7;

    /*
     * UserKey errors
     */
    private final static short SW_UKEY_BASE = SW_BASE + 0x0020;
    public final static short SW_UKEY_NOT_SET = SW_UKEY_BASE + 0;
    public final static short SW_UKEY_WRONG_SIZE = SW_UKEY_BASE + 1;
    public final static short SW_UKEY_HASH_INVALID = SW_UKEY_BASE + 2;

    /*
     * ServerKey errors.
     */
    private final static short SW_SKEY_BASE = SW_BASE + 0x0030;
    public static final short SW_SKEY_NOT_SET = SW_SKEY_BASE + 0;
    public static final short SW_SKEY_WRONG_SIZE = SW_SKEY_BASE + 1;

    /*
     * DocumentKey errors.
     */
    private final static short SW_DKEY_BASE = SW_BASE + 0x0040;
    public static final short SW_DKEY_NOT_SET = SW_DKEY_BASE + 0;
    public static final short SW_DKEY_WRONG_SIZE = SW_DKEY_BASE + 1;

    /*
     * Generic error codes.
     */
    private final static short SW_GENERAL_BASE = SW_BASE + 0x1000;
    public final static short SW_GENERAL_ERROR = SW_GENERAL_BASE + 0x000;
    public final static short SW_GENERAL_CRYPTO_ERROR = SW_GENERAL_BASE + 0x100;
    public final static short SW_GENERAL_RETURN_ERROR = SW_GENERAL_BASE + 0x200;
    public final static short SW_GENERAL_TRANSMISSION_ERROR = SW_GENERAL_BASE + 0x300;
    public final static short SW_DEBUG_ONLY = 0x1000;

    /*
     * Some shared constants.
     */
    // public final static short MAX_ID_LENGTH = 0x08;
    public final static short MAX_CLUSTERKEYS = 16;
    public final static byte POS_NOT_FOUND = (byte) 0xFF;

    /*
     * Some more lengths..
     */
    public static final byte CBC_BLOCK_LEN = 16;
    public static final byte SHA1_HASH_LEN = 20 + 15;
    public static final byte SHA256_HASH_LEN = 32 + 19;
    public static final byte MD5_HASH_LEN = 16 + 18;
    public static final short MAX_APDU_SIZE = 256;
    public static final byte CKEY_ID_LEN = 8;
    public final static byte[] DEFAULT_PIN = {'1', '2', '3', '4', '5'};
    public final static byte[] DEFAULT_PUK = {'7', '6', '5', '4', '3', '2',
        '1', '0'};
    public final static byte PIN_MAX_SIZE = 10;
    public final static byte PIN_MIN_SIZE = 4;
    public final static byte PUK_MAX_SIZE = 10;
    public final static byte PUK_MIN_SIZE = 8;
}

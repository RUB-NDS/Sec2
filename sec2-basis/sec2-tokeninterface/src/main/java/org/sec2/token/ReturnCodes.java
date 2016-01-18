package org.sec2.token;

public class ReturnCodes {
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

    /* OK */
    public static final int SW_SUCCESS = 0x9000;
}

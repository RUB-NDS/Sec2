/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.token.hwtoken;

/**
 *
 * @author benedikt
 */
public class InstructionCodes {
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
     * Random number commands.
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
}

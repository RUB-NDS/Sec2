package org.sec2.token.exceptions;

import org.sec2.token.ReturnCodes;

public class TokenException extends Exception {

    private String exceptionMsg = "Unknown problem when communicating with MSC device.";
    private int SW = 0;

    public TokenException(int sw) {
        exceptionMsg = "MSC reported problem: ";
        SW = sw;

        switch (sw) {
            case ReturnCodes.SW_CKEY_NOT_SET:
                exceptionMsg += "ClusterKey is not set";
                break;
            case ReturnCodes.SW_CKEY_WRONG_ID_SIZE:
                exceptionMsg += "ClusterKey ID has wrong size";
                break;
            case ReturnCodes.SW_CKEY_WRONG_SIZE:
                exceptionMsg += "ClusterKey has wrong size";
                break;
            case ReturnCodes.SW_CKEY_SLOT_COUNT_INVALID:
                exceptionMsg += "Slot count for ClusterKeyStore is invalid";
                break;
            case ReturnCodes.SW_CKEY_SLOT_EMPTY:
                exceptionMsg += "ClusterKey slot is empty";
                break;
            case ReturnCodes.SW_CKEY_SLOT_INVALID:
                exceptionMsg += "ClusterKey slot is invalid";
                break;
            case ReturnCodes.SW_CKEY_SLOT_IN_USE:
                exceptionMsg += "ClusterKey slot is in use";
                break;
            case ReturnCodes.SW_CKEY_DATA_INVALID:
                exceptionMsg += "Could not encrypt/decrypt with ClusterKey: data invalid (e.g., too short, IV missing, etc.)";
                break;

            case ReturnCodes.SW_UKEY_NOT_SET:
                exceptionMsg += "UserKey is not set";
                break;
            case ReturnCodes.SW_UKEY_WRONG_SIZE:
                exceptionMsg += "UserKey has wrong size";
                break;
            case ReturnCodes.SW_UKEY_HASH_INVALID:
                exceptionMsg += "Value to be signed (hash + identifier) has wrong length";
                break;

            case ReturnCodes.SW_SKEY_NOT_SET:
                exceptionMsg += "ServerKey is not set";
                break;
            case ReturnCodes.SW_SKEY_WRONG_SIZE:
                exceptionMsg += "ServerKey has wrong size";
                break;

            case ReturnCodes.SW_PIN_VALIDATION_FAILED:
                exceptionMsg += "Validation of PIN failed";
                break;
            case ReturnCodes.SW_PUK_VALIDATION_FAILED:
                exceptionMsg += "Validation of PUK failed";
                break;
            case ReturnCodes.SW_PIN_PUK_AUTHENTICATION_REQUIRED:
                exceptionMsg += "Authentication with PIN or PUK required";
                break;
            case ReturnCodes.SW_PIN_WRONG_SIZE:
                exceptionMsg += "PIN has wrong size";
                break;
            case ReturnCodes.SW_PUK_WRONG_SIZE:
                exceptionMsg += "PUK has wrong size";
                break;
            case ReturnCodes.SW_PUK_SET_ONLY_ONCE:
                exceptionMsg += "PUK may only be set once";
                break;

            case ReturnCodes.SW_GENERAL_CRYPTO_ERROR + 1:
                exceptionMsg += "CryptoException (ILLEGAL_VALUE)";
                break;
            case ReturnCodes.SW_GENERAL_CRYPTO_ERROR + 2:
                exceptionMsg += "CryptoException (UNINITIALIZED_KEY)";
                break;
            case ReturnCodes.SW_GENERAL_CRYPTO_ERROR + 3:
                exceptionMsg += "CryptoException (NO_SUCH_ALGORITHM)";
                break;
            case ReturnCodes.SW_GENERAL_CRYPTO_ERROR + 4:
                exceptionMsg += "CryptoException (INVALID_INIT)";
                break;
            case ReturnCodes.SW_GENERAL_CRYPTO_ERROR + 5:
                exceptionMsg += "CryptoException (ILLEGAL_USE)";
                break;

                
            case ReturnCodes.SW_GENERAL_CRYPTO_ERROR:
                exceptionMsg += "Unspecified crypto error";
                break;
            case ReturnCodes.SW_GENERAL_ERROR:
                exceptionMsg += "Unspecified error";
                break;

            case 0x6D00:
                exceptionMsg += "Command not supported";
                break;

            default:
                exceptionMsg += "Unknown error code (" + Integer.toHexString(sw)
                        + ")";
        }
    }

    public TokenException(String msg) {
        SW = 0;
        exceptionMsg = msg;
    }

    public TokenException() {
    }

    public int getSW() {
        return SW;
    }

    public String toString() {
        return exceptionMsg;
    }
}
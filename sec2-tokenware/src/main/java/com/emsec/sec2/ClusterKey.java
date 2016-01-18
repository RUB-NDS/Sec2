package com.emsec.sec2;

import javacard.framework.ISOException;
import javacard.security.AESKey;
import javacard.security.CryptoException;
import javacard.security.KeyBuilder;
import javacardx.crypto.Cipher;
import javacardx.framework.util.ArrayLogic;

public class ClusterKey {

    private static final byte CKEY_ALG = Cipher.ALG_AES_BLOCK_128_CBC_NOPAD;
    private byte[] keyId;
    private AESKey symKey;
    private short keyLen;
    private short idLen;
    private Cipher symCipher;

    /**
     * Constructor.
     *
     * @param keyLen length of AES key used, LENGTH_AES_128, LENGTH_AES_192,
     * LENGTH_AES_256 are supported
     * @param idLen expected length of keyId in bytes, must be at least 8 bytes
     * and maximal 32 bytes
     * @throws ISOException
     */
    ClusterKey(short keyLen, short idLen) throws ISOException {
        if (!((keyLen == KeyBuilder.LENGTH_AES_256)
                || (keyLen == KeyBuilder.LENGTH_AES_128) || (keyLen == KeyBuilder.LENGTH_AES_192))) {
            ISOException.throwIt(TokenIO.SW_CKEY_WRONG_SIZE);
        }
        if (idLen < 8 || idLen > 32) {
            ISOException.throwIt(TokenIO.SW_CKEY_WRONG_ID_SIZE);
        }

        this.idLen = idLen;
        /*
         * We want to store the keyLen in bytes.
         */
        this.keyLen = (short) (keyLen / 8);
        this.keyId = new byte[idLen];

        try {
            this.symCipher = Cipher.getInstance(CKEY_ALG, false);
            this.symKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES,
                    keyLen, false);
        } catch (Exception e) {
            ISOException.throwIt(TokenIO.SW_GENERAL_CRYPTO_ERROR);
        }

        short i;
        for (i = 0; i < idLen; i++) {
            this.keyId[i] = 0x00;
        }
    }

    /**
     * Set the key material, i.e. the AES key.
     *
     * @param buffer buffer where key is found
     * @param readOff offset into buffer
     * @param readLen number of keybytes in buffer
     * @throws ISOException
     */
    void setKey(byte[] buffer, short readOff, short readLen)
            throws ISOException {
        if (readLen != keyLen) {
            ISOException.throwIt(TokenIO.SW_CKEY_WRONG_SIZE);
        }

        try {
            symKey.setKey(buffer, readOff);
        } catch (Exception e) {
            ISOException.throwIt(TokenIO.SW_CKEY_WRONG_SIZE);
        }
    }

    /**
     * Set the keyId of this clusterKey.
     *
     * @param buffer buffer where id is found
     * @param readOff read offset into buffer
     * @param readLen number of bytes in buffer
     * @throws ISOException
     */
    void setId(byte[] buffer, short readOff, short readLen) throws ISOException {
        if (readLen != idLen) {
            ISOException.throwIt(TokenIO.SW_CKEY_WRONG_ID_SIZE);
        }

        try {
            ArrayLogic.arrayCopyRepack(buffer, readOff, readLen, keyId,
                    (short) 0);
        } catch (Exception e) {
            ISOException.throwIt(TokenIO.SW_CKEY_WRONG_ID_SIZE);
        }
    }

    /**
     * Get the keyId of this clusterKey.
     *
     * @param buffer buffer where id is written to
     * @param writeOff write offset into buffer
     * @return number of bytes written
     * @throws ISOException
     */
    short getId(byte[] buffer, short writeOff) throws ISOException {
        if (!isInitialized()) {
            ISOException.throwIt(TokenIO.SW_CKEY_NOT_SET);
        }

        try {
            ArrayLogic.arrayCopyRepack(keyId, (short) 0, idLen, buffer,
                    (short) writeOff);
        } catch (Exception e) {
            ISOException.throwIt(TokenIO.SW_CKEY_WRONG_ID_SIZE);
        }

        return idLen;
    }

    /**
     * Get the key material of this clusterKey. This function is private,
     * because clusterKeys *never* leave the card.
     *
     * @return AESKey holding the key material
     * @throws ISOException
     */
    private AESKey getKey() throws ISOException {
        if (!isInitialized()) {
            ISOException.throwIt(TokenIO.SW_CKEY_NOT_SET);
        }

        return symKey;
    }

    /**
     * En-/decrypt data with the clusterKey.
     *
     * @param mode indicate whether data is to be encrypted or decrypted,
     * accepts Cipher.MODE_DECRYPT and Cipher.MODE_ENCRYPT
     * @param buffer buffer where data is read from and output is written to
     * @param readOff read offset into buffer
     * @param readLen number of bytes to read, should be a multiple of
     * TokenIO.CBC_BLOCK_LEN
     * @param writeOff write offset into buffer
     * @return number of bytes written
     * @throws ISOException
     */
    private short doCipherOperation(byte mode, byte[] buffer, short readOff,
            short readLen, short writeOff) throws ISOException {
        if (!((mode == Cipher.MODE_DECRYPT) || (mode == Cipher.MODE_ENCRYPT))) {
            ISOException.throwIt(TokenIO.SW_GENERAL_CRYPTO_ERROR);
        }
        /*
         * We should at least provide two blocks, the first is treated as IV and
         * the second as payload data.
         */
        if (readLen < 2 * TokenIO.CBC_BLOCK_LEN) {
            ISOException.throwIt(TokenIO.SW_CKEY_DATA_INVALID);
        }

        symCipher.init(getKey(), mode, buffer, readOff,
                (short) TokenIO.CBC_BLOCK_LEN);

        /*
         * First block is treated as IV, payload data follows.
         */
        readOff = (short) (readOff + TokenIO.CBC_BLOCK_LEN);
        readLen = (short) (readLen - TokenIO.CBC_BLOCK_LEN);
        short writeLen = 0;

        try {
            byte[] decryptedData = new byte[readLen];
            writeLen = symCipher.doFinal(buffer, readOff, readLen,
                    decryptedData, (short) 0);
            ArrayLogic.arrayCopyRepack(decryptedData, (short) 0, writeLen,
                    buffer, (short) writeOff);
        } catch (CryptoException e) {
            ISOException.throwIt((short) (TokenIO.SW_GENERAL_CRYPTO_ERROR + e.getReason()));
        } catch (Exception e) {
            ISOException.throwIt(TokenIO.SW_GENERAL_ERROR);
        }

        return writeLen;
    }

    /**
     * Wrapper for doCipherOperation(), i.e., calls this function to do the
     * decryption of data.
     *
     * @param buffer buffer where data is read from and output is written to
     * @param readOff read offset into buffer
     * @param readLen number of bytes to read
     * @param writeOff write offset into buffer
     * @return number of bytes written
     * @throws ISOException
     */
    short decrypt(byte[] buffer, short readOff, short readLen, short writeOff)
            throws ISOException {
        return doCipherOperation(Cipher.MODE_DECRYPT, buffer, readOff, readLen,
                writeOff);
    }

    /**
     * Wrapper for doCipherOperation(), i.e., calls this function to do the
     * encryption of data.
     *
     * @param buffer buffer where data is read from and output is written to
     * @param readOff read offset into buffer
     * @param readLen number of bytes to read
     * @param writeOff write offset into buffer
     * @return number of bytes written
     * @throws ISOException
     */
    short encrypt(byte[] buffer, short readOff, short readLen, short writeOff)
            throws ISOException {
        return doCipherOperation(Cipher.MODE_ENCRYPT, buffer, readOff, readLen,
                writeOff);
    }

    /**
     * Check if the clusterKey is initialized. This is only the case, if a) the
     * key is set and b) the keyId is non-zero.
     *
     * @return true if key and keyId are set, false otherwise
     */
    boolean isInitialized() {
        short i;

        if (symKey.isInitialized()) {
            /*
             * Check if keyId is '000..0' which marks an uninitialized id.
             */
            boolean allZero = true;
            for (i = 0; i < idLen; i++) {
                if (!(allZero = (keyId[i] == 0x00))) {
                    break;
                }
            }
            return !allZero;
        }

        return false;
    }

    /**
     * Clear key material and id.
     */
    void clear() {
        if (!isInitialized()) {
            ISOException.throwIt(TokenIO.SW_CKEY_NOT_SET);
        }

        short i;
        for (i = 0; i < idLen; i++) {
            keyId[i] = 0x00;
        }
        symKey.clearKey();
    }
}

package com.emsec.sec2;

import javacard.framework.ISOException;
import javacard.security.KeyBuilder;
import javacard.security.RSAPublicKey;

public class ServerKey {

    private RSAPublicKey publicKey;
    private short keyLen;

    /**
     * Constructor.
     *
     * @param keyLen length of RSA-modulus, allowed values are LENGTH_RSA_512,
     * LENGTH_RSA_1024 and LENGTH_RSA_2048.
     * @throws ISOException
     */
    ServerKey(short keyLen) throws ISOException {
        if (!((keyLen == KeyBuilder.LENGTH_RSA_512)
                || (keyLen == KeyBuilder.LENGTH_RSA_1024) || (keyLen == KeyBuilder.LENGTH_RSA_2048))) {
            ISOException.throwIt(TokenIO.SW_UKEY_WRONG_SIZE);
        }

        this.publicKey = (RSAPublicKey) KeyBuilder.buildKey(
                KeyBuilder.TYPE_RSA_PUBLIC, keyLen, false);
        this.keyLen = (short) (keyLen / 8);
    }

    /**
     * Write the exponent.
     *
     * @param buffer buffer containing the exponent
     * @param readOff offset into buffer
     * @param readLen number of bytes in buffer
     * @throws ISOException
     */
    void setExponent(byte[] buffer, short readOff, short readLen)
            throws ISOException {
        try {
            publicKey.setExponent(buffer, readOff, readLen);
        } catch (Exception e) {
            ISOException.throwIt(TokenIO.SW_SKEY_WRONG_SIZE);
        }
    }

    /**
     * Write the modulus.
     *
     * @param buffer buffer where modulus is found
     * @param readOff read offset into buffer
     * @param readLen number of bytes to read
     * @throws ISOException
     */
    void setModulus(byte[] buffer, short readOff, short readLen)
            throws ISOException {
        if (readLen != keyLen) {
            ISOException.throwIt(TokenIO.SW_SKEY_WRONG_SIZE);
        }

        try {
            publicKey.setModulus(buffer, readOff, readLen);
        } catch (Exception e) {
            ISOException.throwIt(TokenIO.SW_SKEY_WRONG_SIZE);
        }
    }

    /**
     * Read the exponent.
     *
     * @param buffer buffer where exponent is written to
     * @param writeOff offset into buffer
     * @return number of bytes written
     * @throws ISOException
     */
    short getExponent(byte[] buffer, short writeOff) throws ISOException {
        if (!publicKey.isInitialized()) {
            ISOException.throwIt(TokenIO.SW_SKEY_NOT_SET);
        }

        short writeLen = 0;
        try {
            writeLen = publicKey.getExponent(buffer, writeOff);
        } catch (Exception e) {
            ISOException.throwIt(TokenIO.SW_SKEY_WRONG_SIZE);
        }

        return writeLen;
    }

    /**
     * Read the modulus.
     *
     * @param buffer buffer where modulus is written to
     * @param writeOff offset into buffer
     * @return number of bytes written
     * @throws ISOException
     */
    short getModulus(byte[] buffer, short writeOff) throws ISOException {
        if (!publicKey.isInitialized()) {
            ISOException.throwIt(TokenIO.SW_SKEY_NOT_SET);
        }

        short writeLen = 0;
        try {
            writeLen = publicKey.getModulus(buffer, writeOff);
        } catch (Exception e) {
            ISOException.throwIt(TokenIO.SW_SKEY_WRONG_SIZE);
        }

        return writeLen;
    }

    /**
     * Determine whether the serverKey has been set or not.
     *
     * @return true if a publicKey is set, false if not
     */
    boolean isInitialized() {
        return publicKey.isInitialized();
    }
}
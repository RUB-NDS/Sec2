package com.emsec.sec2;

import javacard.framework.ISOException;
import javacard.security.CryptoException;
import javacard.security.KeyBuilder;
import javacard.security.KeyPair;
import javacard.security.RSAPrivateCrtKey;
import javacard.security.RSAPublicKey;
import javacardx.crypto.Cipher;
import javacardx.framework.util.ArrayLogic;

public abstract class AUserKeyPair {

    private final static byte UKEY_TYPE = KeyPair.ALG_RSA_CRT;
    private KeyPair userKeyPair;
    private short keyLen;
    private boolean isInitialized;

    /**
     * Constructor of AUserKeyPair. Initializes KeyPair and Ciphers.
     *
     * @param keyLen Length of RSA Keypair, valid lengths are LENGTH_RSA_512,
     * LENGTH_RSA_1024, LENGTH_RSA_2048
     * @throws ISOException
     */
    AUserKeyPair(short keyLen) throws ISOException {
        if (!((keyLen == KeyBuilder.LENGTH_RSA_512)
                || (keyLen == KeyBuilder.LENGTH_RSA_1024)
                || (keyLen == KeyBuilder.LENGTH_RSA_2048))) {
            ISOException.throwIt(TokenIO.SW_UKEY_WRONG_SIZE);
        }

        try {
            this.userKeyPair = new KeyPair(UKEY_TYPE, keyLen);
        } catch (CryptoException e) {
            ISOException.throwIt((short) (TokenIO.SW_GENERAL_CRYPTO_ERROR
                    + e.getReason()));
        }

        this.keyLen = (short) (keyLen / 8);
        this.isInitialized = false;
    }

    /**
     * Get modulus of generated keypair.
     *
     * @param buffer target buffer where modulus is written to
     * @param writeOff offset in target buffer
     * @return number of bytes written (i.e. length of modulus)
     * @throws ISOException
     */
    short getModulus(byte[] buffer, short writeOff) throws ISOException {
        if (userKeyPair == null || !isInitialized) {
            ISOException.throwIt(TokenIO.SW_UKEY_NOT_SET);
        }

        RSAPublicKey publicKey = (RSAPublicKey) userKeyPair.getPublic();
        short len = publicKey.getModulus(buffer, writeOff);
        if (len == 0) {
            ISOException.throwIt(TokenIO.SW_UKEY_NOT_SET);
        }

        return len;
    }

    /**
     * Get exponent of keypair.
     *
     * @param buffer target buffer where keypair is written to
     * @param writeOff offset in target buffer
     * @return number of bytes written (i.e. length of exponent)
     * @throws ISOException
     */
    short getExponent(byte[] buffer, short writeOff) throws ISOException {
        if (!isInitialized) {
            ISOException.throwIt(TokenIO.SW_UKEY_NOT_SET);
        }

        RSAPublicKey publicKey = (RSAPublicKey) userKeyPair.getPublic();
        short len = publicKey.getExponent(buffer, writeOff);
        if (len == 0) {
            ISOException.throwIt(TokenIO.SW_UKEY_NOT_SET);
        }

        return len;
    }

    /**
     * Generate a new keypair.
     *
     * @throws ISOException
     */
    void generate() throws ISOException {
        try {
            userKeyPair.genKeyPair();
        } catch (CryptoException e) {
            ISOException.throwIt((short) (TokenIO.SW_GENERAL_CRYPTO_ERROR
                    + e.getReason()));
        }

        isInitialized = true;
    }

    /**
     * Decrypt/sign data encrypted with the public-key of this card.
     *
     * @param cipher cipher type to use, i.e. rsaEncCipher or rsaSigCipher
     * @param mode mode to use with the given cipher, either MODE_DECRYPT or
     * MODE_SIGN
     * @param buffer the buffer holds the encrypted data and after this function
     * the decrypted data
     * @param readOff offset in buffer where encrypted data is to be read
     * @param readLen length of encrypted data
     * @param writeOff offset in buffer where decrypted data is to be written
     * @return number of bytes written to buffer
     * @throws ISOException
     */
    public short doPrivateKeyOperation(Cipher cipher, byte mode,
            byte[] buffer, short readOff, short readLen, short writeOff)
            throws ISOException {
        if (!isInitialized) {
            ISOException.throwIt(TokenIO.SW_UKEY_NOT_SET);
        }

        short writeLen = 0;
        byte[] decryptedData = new byte[keyLen];

        try {
            RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) userKeyPair.getPrivate();

            /*
             * Decrypt/sign buffer with private key and store final result back
             * in the same buffer.
             */
            cipher.init(privateKey, mode);
            writeLen = cipher.doFinal(buffer, readOff, readLen, decryptedData,
                    (short) 0);
            ArrayLogic.arrayCopyRepack(decryptedData, (short) 0, writeLen,
                    buffer, (short) writeOff);
        } catch (CryptoException e) {
            ISOException.throwIt((short) (TokenIO.SW_GENERAL_CRYPTO_ERROR
                    + e.getReason()));
        } catch (Exception e) {
            ISOException.throwIt(TokenIO.SW_GENERAL_ERROR);
        }

        return writeLen;
    }

    /**
     * Determine whether a userKey has already been generated.
     *
     * @return true if a keypair exists, false if not
     */
    boolean isInitialized() {
        return isInitialized;
    }
}

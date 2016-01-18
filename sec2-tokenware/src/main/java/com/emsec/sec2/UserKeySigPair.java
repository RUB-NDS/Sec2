/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emsec.sec2;

import javacard.framework.ISOException;
import javacard.security.CryptoException;
import javacardx.crypto.Cipher;

/**
 *
 * @author benedikt
 */
public class UserKeySigPair extends AUserKeyPair {

    private final static byte UKEY_SIG_ALG = Cipher.ALG_RSA_PKCS1;
    private Cipher rsaSigCipher;

    /**
     * Constructur.
     * @param keyLen length of the assiciated signature keypair.
     * @throws ISOException 
     */
    public UserKeySigPair(short keyLen) throws ISOException {
        super(keyLen);

        try {
            this.rsaSigCipher = Cipher.getInstance(UKEY_SIG_ALG, false);
        } catch (CryptoException e) {
            ISOException.throwIt((short) (TokenIO.SW_GENERAL_CRYPTO_ERROR
                    + e.getReason()));
        }
    }

    /**
     * Sign a SHA1 value to produce a signature.
     *
     * @param buffer the buffer holds the hash value and after this function the
     * signature
     * @param readOff offset in buffer where hash is to be read
     * @param readLen length of hash (must be SHA1_HASH_LEN = 20)
     * @param writeOff offset in buffer where signature is written to
     * @return number of bytes written
     * @throws ISOException
     */
    short sign(byte[] buffer, short readOff, short readLen, short writeOff)
            throws ISOException {
        return super.doPrivateKeyOperation(rsaSigCipher, Cipher.MODE_ENCRYPT, buffer,
                readOff, readLen, writeOff);
    }
}

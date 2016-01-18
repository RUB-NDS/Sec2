/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emsec.sec2;

import javacard.framework.ISOException;
import javacard.security.CryptoException;
import javacard.security.KeyPair;
import javacardx.crypto.Cipher;

/**
 *
 * @author benedikt
 */
public class UserKeyEncPair extends AUserKeyPair {

    private final static byte UKEY_ENC_ALG = Cipher.ALG_RSA_PKCS1_OAEP;
    private Cipher rsaEncCipher;
    
    /**
     * Constructor. 
     * @param keyLen length of the associated encryption keypair.
     * @throws ISOException 
     */
    public UserKeyEncPair(short keyLen) throws ISOException {
        super(keyLen);
        
        try {
            this.rsaEncCipher = Cipher.getInstance(UKEY_ENC_ALG, false);
        } catch (CryptoException e) {
            ISOException.throwIt((short) (TokenIO.SW_GENERAL_CRYPTO_ERROR
                    + e.getReason()));
        }
    }

    /**
     * Decrypt data according to PKCS#1.5.
     *
     * @param buffer the buffer holds the encrypted data and after this function
     * the decrypted data
     * @param readOff offset in buffer where encrypted data is to be read
     * @param readLen length of encrypted data
     * @param writeOff offset in buffer where decrypted data is to be written
     * @return number of bytes written to buffer
     * @throws ISOException
     */
    short decrypt(byte[] buffer, short readOff, short readLen, short writeOff)
            throws ISOException {
        return super.doPrivateKeyOperation(rsaEncCipher, Cipher.MODE_DECRYPT, buffer,
                readOff, readLen, writeOff);
    }
}

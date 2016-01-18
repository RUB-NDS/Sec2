/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.token.swtoken;

import javax.crypto.Cipher;
import org.sec2.token.exceptions.TokenException;

/**
 *
 * @author benedikt
 */
public class UserKeyEncPair extends AUserKeyPair {

    private final static String UKEY_ENC_ALG = "RSA/ECB/OAEPWithSHA1AndMGF1Padding";

    public byte[] decrypt(byte[] in) throws TokenException {
        return super.doPrivateKeyOperation(in, Cipher.DECRYPT_MODE, UKEY_ENC_ALG);
    }
}

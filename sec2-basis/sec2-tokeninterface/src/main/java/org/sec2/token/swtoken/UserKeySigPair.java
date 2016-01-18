/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.token.swtoken;

import javax.crypto.Cipher;
import org.sec2.token.TokenConstants;
import org.sec2.token.exceptions.TokenException;

/**
 *
 * @author benedikt
 */
public class UserKeySigPair extends AUserKeyPair {

    private final static String UKEY_SIG_ALG = "RSA/None/PKCS1Padding";

    public byte[] sign(byte[] in) throws TokenException {
        if (!((in.length == TokenConstants.SHA1_HASH_LEN)
                || (in.length == TokenConstants.SHA256_HASH_LEN)
                || (in.length == TokenConstants.MD5_HASH_LEN))) {
            throw new TokenException("Hash data has invalid length");
        }
        return super.doPrivateKeyOperation(in, Cipher.ENCRYPT_MODE, UKEY_SIG_ALG);
    }
}

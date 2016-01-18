package org.sec2.token.swtoken;

import java.io.Serializable;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import javax.crypto.Cipher;
import org.sec2.token.ReturnCodes;
import org.sec2.token.exceptions.TokenException;
import org.sec2.token.keys.UserKey;
import org.spongycastle.jce.provider.BouncyCastleProvider;

public abstract class AUserKeyPair implements Serializable {

    private static final long serialVersionUID = 1L;
    private KeyPair userKeyPair;
    private boolean isInitialized;

    public AUserKeyPair() {
        if (Security.getProvider("SC") == null) {
            Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
        }
    }
    
    public byte[] getEncoded() {
        return userKeyPair.getPrivate().getEncoded();
    }
    
    public void generate(KeyPair kp){
        this.userKeyPair = kp;
        this.isInitialized = true;
    }

    public void generate(int KEY_SIZE) throws TokenException {
        try {
            KeyPairGenerator kg = KeyPairGenerator.getInstance("RSA");
            kg.initialize(KEY_SIZE);
            this.userKeyPair = kg.generateKeyPair();
            this.isInitialized = true;
        } catch (Exception e) {
            throw new TokenException(ReturnCodes.SW_GENERAL_CRYPTO_ERROR);
        }
    }

    public UserKey getPublicKey() throws TokenException {
        if (!this.isInitialized) {
            throw new TokenException(ReturnCodes.SW_UKEY_NOT_SET);
        }

        RSAPublicKey rsaKey = (RSAPublicKey) this.userKeyPair.getPublic();
        UserKey userKey = new UserKey(rsaKey.getModulus().toByteArray(), rsaKey.getPublicExponent().toByteArray());
        return userKey;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public byte[] doPrivateKeyOperation(byte[] in, int mode, String type) throws TokenException {
        if (!this.isInitialized) {
            throw new TokenException(ReturnCodes.SW_UKEY_NOT_SET);
        }

        byte[] out;
        Cipher rsaSigCipher;
        RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) this.userKeyPair.getPrivate();
        try {
            rsaSigCipher = Cipher.getInstance(type, new BouncyCastleProvider());
            rsaSigCipher.init(mode, privateKey);
            out = rsaSigCipher.doFinal(in);
        } catch (Exception e) {
            throw new TokenException(e.getMessage());
        }
        return out;
    }
}

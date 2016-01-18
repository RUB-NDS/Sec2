/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.rest.backend;

import CryptoServerJCE.CryptoServerProvider;
import java.security.Provider;
import java.security.Security;
import org.sec2.backend.provider.Sec2Provider;
import org.sec2.rest.RestException;

/**
 *
 * @author Thorsten Schreiber <thorsten.schreiber@rub.de>
 */
public class Cryptor {
    
    Provider p = new Sec2Provider(null);
    
    public static byte[] generateKey(){
        CryptoServerProvider cs = (CryptoServerProvider) Security
                .getProvider("CryptoServer");
        return cs.getCryptoServer().generateRandom(1,16);
        
    };

    public static byte[] decrypt(byte[] key, byte[] data) {
        return null;
    }

    public static byte[] encrypt(byte[] key, byte[] data) {
        return null;
    }

    public static byte[] unwrap(byte[] userId, byte[] wrappedskey) {
        return null;
    }

    public static byte[] wrap(byte[] userId, byte[] key) {
        return null;
    }
    
    public static byte[] sign(byte[] data){
        return null;
    }
    
    public static byte[] verify(byte[] userId,byte[] data,byte[] signature)
    throws RestException{
        return null;
    }
    
    
    
    
}

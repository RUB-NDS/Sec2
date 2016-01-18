/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.securityprovider.mobileClient;


import java.beans.XMLEncoder;
import java.io.*;
import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.PublicKey;
import java.text.ParseException;
import org.sec2.token.swtoken.AUserKeyPair;
import org.sec2.token.swtoken.UserKeySigPair;


/**
 *
 *  Reads the State file of the Krypto Token Card Software Simulation.
 * 
 * 
 * @author Thorsten Schreiber <thorsten.schreiber@rub.de>
 */

public class SoftwareTokenSpy {
    
    
    
    private KeyPair mEncryptionKeyPair;
    
    private KeyPair mSignatureKeyPair;
    
    private PublicKey mServerSignatureKey;
    
    private KeyPair extractKeyPair(AUserKeyPair cryptoKey){
        try
        {
                 Field f = UserKeySigPair.class.getDeclaredField("userKeyPair");
                 f.setAccessible(true);
                 
                 return (KeyPair) f.get(cryptoKey);
        } catch (Exception e){
            return null;
        }
         
    }
    
    private void loadTokenStorefromFile(String filename) 
                        throws FileNotFoundException, ParseException{
    File saveFile = new File(filename);
        if (saveFile.exists()) {
            try {
                FileInputStream file = new FileInputStream(filename);
                ObjectInputStream o = new ObjectInputStream(file);
                for (int i = 0; i <6;i++)
                    o.readObject();
                  
                 
         
                 
                 
                 
                 
                 int i =2;
                 
                //UserKeySigPair userKeySig = (UserKeySigPair) o.readObject();
                
                
            //    UserKeyEncPair userKeyEnc = (UserKeyEncPair) o.readObject();
            //    ServerKey serverKey = (ServerKey) o.readObject();
                
              
            //    userKeyEnc.
                
                
                o.close();
            } catch (Exception e) {
                e.printStackTrace();

            }
        }    
        
    }
    
    private SoftwareTokenSpy(){};
    
    public SoftwareTokenSpy(String filename) throws FileNotFoundException, ParseException{
        loadTokenStorefromFile(filename);
    }
   
    
    public KeyPair getEncryptionKeyPair(){
        return mEncryptionKeyPair;
    }
    
    public KeyPair getSignatureKeyPair(){
        return mSignatureKeyPair;
    }
    
        
    public PublicKey getServerSignatureKey(){
        return mServerSignatureKey;
    }
    
}

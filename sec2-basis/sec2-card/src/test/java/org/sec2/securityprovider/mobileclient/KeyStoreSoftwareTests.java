/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.securityprovider.mobileclient;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import org.sec2.securityprovider.MobileClientCommons;
import org.sec2.securityprovider.serviceparameter.PublicKeyType;
import org.sec2.securityprovider.serviceparameter.TokenType;

/**
 *
 * @author Thorsten Schreiber <thorsten.schreiber@rub.de>
 */
public class KeyStoreSoftwareTests extends KeyStoreTestImpovedImpl{
    
    public KeyStoreSoftwareTests() {
        super(MobileClientCommons.getInstance(TokenType.SOFTWARE_TOKEN)
                .getProvider());
    }
    
      public void testMatchingCertificates() {
        
  
        
         Exception ex = null;
        
         X509Certificate cert=null;
         PublicKey pk = null;      
         try {
             
             
             SoftwareTokenSpy red = new SoftwareTokenSpy("crypto-data");
             
             
              pk = (PublicKey) keystore
                      .getKey(PublicKeyType.CLIENT_ENCRYPTION.toString(), null);
              KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
             
             g.initialize(2048);
             KeyPair k = g.generateKeyPair();
             
           //  cert = generateCertificate(pk, null);
            // System.out.println("This is generated Certificate"+cert.toString());
         } catch (Exception notInTest) {

             notInTest.printStackTrace();
             fail("Could Not Generate Test-Certificate");
         }
             
          
        
    }
}

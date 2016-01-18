/*
 * Copyright 2011 Sec2 Consortium
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit written
 * permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://www.sec2.org
 */
package org.sec2.securityprovider.mobileClient;

import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import junit.framework.TestCase;
import org.sec2.securityprovider.exceptions.CertificateBadMatchExeption;
import org.sec2.securityprovider.exceptions.CertificateValidationException;
import org.sec2.securityprovider.serviceparameter.PublicKeyType;
import org.sec2.securityprovider.serviceparameter.TokenType;
import org.sec2.token.TokenConstants;
import org.sec2.token.keys.ClusterKey;
import org.sec2.token.keys.DocumentKey;
import sun.security.x509.*;

/**
 * Abstracted UnitTests for KeyStore Testing
 *
 * @author Jan Temme - Jan.Temme@rub.de
 * @version 0.1 Jun 08, 2012
 */
public abstract class KeyStoreTestSVNImpl extends TestCase {

    /**
     * Provider to test.
     */
    protected Provider testProvider = null;
    
    /**
     * Public Key from Card (aka UserKey)
     */
    protected PublicKey publicKey = null;
    /**
     * Keystore Instance from testprovider
     */
    protected KeyStore keystore = null;
    /**
     * Generator for Test Keys
     */
    protected SecureRandom gen = new SecureRandom();
    
    
    /**
     * Generator or X509 Certicates
     * @param pk PublicKey that should be included here.
     * @return 
     */
    protected X509Certificate generateCertificate(PublicKey pk,PrivateKey ca)
            throws CertificateException, IOException, NoSuchAlgorithmException
    ,InvalidKeyException,NoSuchProviderException,SignatureException{
 
       
        X509CertInfo i = new X509CertInfo(); 
        Date now = new Date();
        i.set(X509CertInfo.VALIDITY, new CertificateValidity(now, 
                                        new Date(now.getYear()+10, 1,1)));
        i.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(
                                        new BigInteger(64, new SecureRandom())));
        i.set(X509CertInfo.SUBJECT, new CertificateSubjectName(
                                        new X500Name("CN=Jeff Smith,OU=Sales,DC=Fabrikam,DC=COM")));
        i.set(X509CertInfo.ISSUER, new CertificateIssuerName(
                                        new X500Name("CN=Jeff Smith,OU=Sales,DC=Fabrikam,DC=COM")));
        i.set(X509CertInfo.KEY, new CertificateX509Key(pk));
        i.set(X509CertInfo.VERSION, new CertificateVersion(
                                        CertificateVersion.V3));
        i.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(
                                        new AlgorithmId(
                                        AlgorithmId.md5WithRSAEncryption_oid)));
               X509CertImpl c = new X509CertImpl(i);
        if (ca != null)
        c.sign(ca, "SHA1with"+ca.getAlgorithm());
        
       
        // Update the algorith, and resign.

        i.set(CertificateAlgorithmId.NAME + "."
                        + CertificateAlgorithmId.ALGORITHM,
                    (AlgorithmId)c.get(X509CertImpl.SIG_ALG));
        c = new X509CertImpl(i);
        if (ca != null)
        c.sign(ca, "SHA1with"+ca.getAlgorithm());

       return c;   
    }
    
     /**
     * Prints all SecProvider for debugging
     * @return a String multilevel List
     */
    private static String printProviderList(){
       StringBuilder s = new StringBuilder();
            try {
      Provider p[] = Security.getProviders();
      for (int i = 0; i < p.length; i++) {
          s.append(p[i]);
         
          for (Enumeration e = p[i].keys(); e.hasMoreElements();){
              s.append("\t");
              s.append(e.nextElement());
              s.append("\n");
          }
      }
    } catch (Exception e) {
      System.out.println(e);
    }
          return s.toString();
    }
    

    /**
     * Public contructor allows pseudo-parameterization.
     *
     * @param testProvider Provider to be tested.
     * @param referenceProvider Provider to be used as reference provider.
     */
    public KeyStoreTestSVNImpl(Provider testProvider) {
        this.testProvider = testProvider;
    
    }

    /**
     * Set up routine to initialize the environment. Called before every test
     * case method.
     */
    protected void setUp() {
        initKeyStore();
        try {
            publicKey = (PublicKey) keystore.getKey(
                    PublicKeyType.CLIENT_ENCRYPTION.name(), null);
        } catch (KeyStoreException ex) {
            fail("*** FAILED*** | " + ex.toString());
        } catch (NoSuchAlgorithmException ex) {
            fail("*** FAILED*** | " + ex.toString());
        } catch (UnrecoverableKeyException ex) {
            fail("*** FAILED*** | " + ex.toString());
        }
        if (this.testProvider == null) {
            fail("No test provider set.");
        }

    }

    public void testLoad() {
        System.out.println("==== Starting KeyStore Tests on "
                +testProvider.getProperty(TokenType.TOKEN_TYPE_IDENTIFIER)
                +
                " ====");
        ClusterKey key = genClusterKey();
        try {
            keystore.setKeyEntry(key.getId().toString(), key, null, null);
            keystore.load(null, null);
            keystore.getKey(key.getId().toString(), null);
        } catch (UnrecoverableKeyException ex) {
            fail("*** FAILED*** | " + ex.toString());
        } catch (IOException ex) {
            fail("*** FAILED*** | " + ex.toString());
        } catch (NoSuchAlgorithmException ex) {
            fail("*** FAILED*** | " + ex.toString());
        } catch (CertificateException ex) {
            fail("*** FAILED*** | " + ex.toString());
        } catch (KeyStoreException ex) {
            fail("*** FAILED*** | " + ex.toString());
        }
        System.out.println("*** Passed *** | TestCase: Initial Loading");
    }

    public void testSetKey() {
        ClusterKey cKey = genClusterKey();
        DocumentKey dKey = genDocumentKey();
        Exception exc = null;
        try {//save first time
            keystore.setKeyEntry(cKey.getId().toString(), cKey, null, null);
            keystore.setKeyEntry(dKey.getKey().toString(), dKey, null, null);
            assertTrue(keystore.containsAlias(cKey.getId().toString()));
            assertTrue(keystore.containsAlias(dKey.getKey().toString()));
        } catch (KeyStoreException ex) {
            fail("*** FAILED*** | " + ex.toString());
        }

        try {//save ClusterKey second time
            keystore.setKeyEntry(cKey.getId().toString(), cKey, null, null);
        } catch (KeyStoreException ex) {
            exc = ex;
        }
        assertNotNull(exc);
        exc = null;
        try {//save DocumentKey second time
            keystore.setKeyEntry(dKey.getKey().toString(), dKey, null, null);
        } catch (KeyStoreException ex) {
            exc = ex;
        }
        assertNotNull(exc);
        System.out.println("*** Passed *** | TestCase: Adding Keys");
    }

    public void testGetKey() {
        try {
            Enumeration aliases = keystore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                Key key = keystore.getKey(alias, null);
            }
        } catch (NoSuchAlgorithmException ex) {
            fail("*** FAILED*** | " + ex.toString());
        } catch (UnrecoverableKeyException ex) {
            fail("*** FAILED*** | " + ex.toString());
        } catch (KeyStoreException ex) {
            fail("*** FAILED*** | " + ex.toString());
        }
        Exception exc = null;
        try {
            keystore.getKey("NonExistingKey", null);
        } catch (NoSuchAlgorithmException ex) {
            fail("*** FAILED*** | " + ex.toString());
        } catch (UnrecoverableKeyException ex) {
            exc = ex;
        } catch (KeyStoreException ex) {
            fail("*** FAILED*** | " + ex.toString());
        }
        assertNotNull(exc);
        System.out.println("*** Passed *** | TestCase: Retreiving Keys");
    }

    public void testSize() {
        try {
            Enumeration aliases = keystore.aliases();
            int size1 = (Collections.list(aliases)).size();
            int size2 = keystore.size();
            assertEquals(size1, size2);
        } catch (KeyStoreException ex) {
            fail("*** FAILED*** | " + ex.toString());
        }
        System.out.println("*** Passed *** | TestCase: Get Size");
    }

    public void testNullArguments() {
        try {
            keystore.getKey(null, null);
        } catch (NoSuchAlgorithmException ex) {
            //Nothing to do here
        } catch (UnrecoverableKeyException ex) {
            //Nothing to do here
        } catch (KeyStoreException ex) {
            //Nothing to do here
        }//Na hoffentlich ist keine NullPointerException geflogen...
        Exception exc = null;
        try {
            keystore.setKeyEntry(null, null, null, null);
        } catch (KeyStoreException ex) {
            exc = ex;
        }
        assertNotNull(exc);
    }

    public void testDelete() {
        Exception exc = null;
        try {
            keystore.deleteEntry("NonExistingKey");
        } catch (KeyStoreException ex) {
            exc = ex;
        }
        assertNotNull(exc);
        
  System.out.println("*** Passed *** | TestCase: [Deleting Keys] Safe NonExistingKey Handling");

        try {
    
            assertTrue(keystore.aliases().hasMoreElements());  
            
            Enumeration aliases = keystore.aliases();
            while (aliases.hasMoreElements()) {
      
                String alias = ((String) aliases.nextElement());
                keystore.deleteEntry(alias); 
                //List has changed, get new Enumeation-Iterator
                aliases = keystore.aliases();
              //  System.out.println("One Key Deleted:" + alias);
            }
        } catch (KeyStoreException ex) {
            fail("*** FAILED*** | " + ex.toString());
        } catch (Exception ex){
             fail("*** FAILED*** | " + ex.toString());
        }
        System.out.println("*** Passed *** | TestCase: [Deleting Keys] Delete All Keys");
        System.out.println("==== Leaving KeyStore Tests ====");
    }
    /**
     * Tests, if the SetCertificate is all right.
     * The Keystore must be initialisized.
     */
    public void testSetCertficateEntry(){
         //Is Exception thrown on null Entry?
         Exception ex=null;
         try {
             keystore.setCertificateEntry("bla", null);       
            } catch (NullPointerException e) {
                    ex=e;
            } catch (Exception r) {
                r.printStackTrace();
                fail("*** FAILED*** | Other Exceptions should not be thrown" 
                        + r.toString() );
            }   
         assertNotNull(ex);
       System.out.println("*** Passed *** | TestCase: [Certificate Store] Safe Null Alias Handling");

         ex = null;

                  //Is Exception thrown on null Entry?
        X509Certificate cert=null;
         try {
              KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
             
             g.initialize(2048);
             KeyPair k = g.generateKeyPair();
             
             cert = generateCertificate(k.getPublic(), k.getPrivate());
            // System.out.println("This is generated Certificate"+cert.toString());
         } catch (Exception notInTest) {

             notInTest.printStackTrace();
             fail("Could Not Generate Test-Certificate");
         }
             
      
         try {
            
             keystore.setCertificateEntry(null,cert);       
            } catch (NullPointerException e) {
                    ex=e;
                   
                    
            } catch (Exception r) {
                r.printStackTrace();
                fail("*** FAILED*** | Other Exceptions should not be thrown" 
                        + r.toString() );
            }
         assertNotNull(ex);
      
          System.out.println("*** Passed *** |"
                  + " TestCase: [Certificate Store] Safe Null Argument Certificate Handling");
    
      }
    
    /*
     * Will all Exception be thrown?
     */
    
    public void testNoMatchingCertifcates(){
        Exception ex = null;
         X509Certificate cert=null;
         try {
              KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
             
             g.initialize(2048);
             KeyPair k = g.generateKeyPair();
             
             cert = generateCertificate(k.getPublic(), k.getPrivate());
            // System.out.println("This is generated Certificate"+cert.toString());
         } catch (Exception notInTest) {

             notInTest.printStackTrace();
             fail("Could Not Generate Test-Certificate");
         }
             
      
         for (PublicKeyType t : PublicKeyType.values()){
         try {
            
             keystore.setCertificateEntry( t.toString(),cert);       
            } catch (CertificateBadMatchExeption e) {
                    ex=e;
                   
                    
            } catch (CertificateValidationException e ) {
                if (ex != null)
                    fail("*** FAILED*** | Additional exception " 
                        +    e.getMessage() + "to " + ex.getMessage()
                                    +" thrwon. Only one exception should"
                                    + "raise.");
                ex=e;
            } 
         
            catch (Exception r) {
                r.printStackTrace();
                fail("*** FAILED*** | Other Exceptions should not be thrown" 
                        + r.toString() );
            }
         assertNotNull(ex);
         ex=null;
           System.out.println("*** Passed *** |"
                  + " TestCase: [Certificate Store] MISMATCHING CERT ON SLOT "
                   + t.toString()
                   + " RECOGNIZED." 
                   );
    
    }
         
         
    }
    
    
  
            
    
    /*
     * Tests on loading and reveiving irlevant Certifcates.
     */
    
    public void testInrelevantCertificate() {
         X509Certificate cert=null;
         final String RANDOM_CERT_ALIAS = "SLOT_NAME";
         
         try {
              KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
             
             g.initialize(2048);
             KeyPair k = g.generateKeyPair();
             
             cert = generateCertificate(k.getPublic(), k.getPrivate());
            // System.out.println("This is generated Certificate"+cert.toString());
         } catch (Exception notInTest) {

             notInTest.printStackTrace();
             fail("Could Not Generate Test-Certificate");
         };
         assertNotNull(cert);
         
        try {
            keystore.setCertificateEntry(RANDOM_CERT_ALIAS, cert);
        } catch (KeyStoreException ex) {
                        fail("Unexpected Exception" + ex);
        }
               
         System.out.println("*** Passed *** |"
                  + " TestCase: [Certificate Store] Added "
                 + "Inrelevant Certificate");
         
         X509Certificate cert2 = null;
        try {
            cert2 = (X509Certificate) keystore.getCertificate(RANDOM_CERT_ALIAS);
        } catch (KeyStoreException ex) {
                           fail("Unexpected Exception" + ex);
        }
    
        assertEquals(cert, cert2);
        
               System.out.println("*** Passed *** |"
                  + " TestCase: [Certificate Store] Loaded "
                 + "Inrelevant Certificate");
        
    }
    
    

    private ClusterKey genClusterKey() {
        Cipher enc;
        ClusterKey clusKey = null;
        byte[] key = new byte[TokenConstants.CKEY_LEN];
        byte[] identifier = new byte[TokenConstants.CKEY_ID_LEN];
        gen.nextBytes(key);
        gen.nextBytes(identifier);
        try {
            enc = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            enc.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encClusKey = enc.doFinal(key);
            clusKey = new ClusterKey(encClusKey, identifier);
        } catch (Exception ex) {
            fail("*** FAILED*** | " + ex.toString());
        }
        return clusKey;
    }

    private DocumentKey genDocumentKey() {
        byte[] key = new byte[TokenConstants.DKEY_LEN];
        return new DocumentKey(key, false);
    }

    private void initKeyStore() {
        try {
            if (keystore == null) {
                Set s = testProvider.getServices();
                keystore = KeyStore.getInstance("Standard" ,testProvider);
            }
            keystore.load(null, null);
        } catch (KeyStoreException ex) {
            fail("*** FAILED *** | " + ex.toString());
        } catch (IOException ex) {
            fail("*** FAILED *** | " + ex.toString());
        } catch (NoSuchAlgorithmException ex) {
            fail("*** FAILED *** | " + ex.toString());
        } catch (CertificateException ex) {
            fail("*** FAILED *** | " + ex.toString());
        }
    }
}
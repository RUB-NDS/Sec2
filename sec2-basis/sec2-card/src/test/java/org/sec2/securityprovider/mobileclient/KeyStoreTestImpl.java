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
package org.sec2.securityprovider.mobileclient;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.crypto.Cipher;
import junit.framework.TestCase;
import org.sec2.securityprovider.serviceparameter.PublicKeyType;
import org.sec2.token.TokenConstants;
import org.sec2.token.keys.DocumentKey;
import org.sec2.token.keys.GroupKey;
import sun.security.x509.*;

/**
 * Abstracted UnitTests for KeyStore Testing
 *
 * @author Jan Temme - Jan.Temme@rub.de
 * @version 0.1 Jun 08, 2012
 */
public abstract class KeyStoreTestImpl extends TestCase {

    /**
     * Provider to test.
     */
    private Provider testProvider = null;
    private String typeProvider = null;
    /**
     * Public Key from Card (aka UserKey)
     */
    private PublicKey publicKey = null;
    /**
     * Keystore Instance from testprovider
     */
    private KeyStore keystore = null;
    /**
     * Generator for Test Keys
     */
    private SecureRandom gen = new SecureRandom();

    /**
     * Generator or X509 Certicates
     *
     * @param pk PublicKey that should be included here.
     * @return
     */
    private X509Certificate generateCertificate(PublicKey pk, PrivateKey ca)
      throws CertificateException, IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {


        X509CertInfo i = new X509CertInfo();
        Date now = new Date();
        i.set(X509CertInfo.VALIDITY, new CertificateValidity(now,
          new Date(now.getYear() + 10, 1, 1)));
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
        c.sign(ca, "SHA1with" + ca.getAlgorithm());


        // Update the algorith, and resign.

        i.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM,
          (AlgorithmId) c.get(X509CertImpl.SIG_ALG));
        c = new X509CertImpl(i);
        c.sign(ca, "SHA1with" + ca.getAlgorithm());

        return c;
    }

    /**
     * Prints all SecProvider for debugging
     *
     * @return a String multilevel List
     */
    private static String printProviderList() {
        StringBuilder s = new StringBuilder();
        try {
            Provider p[] = Security.getProviders();
            for (int i = 0; i < p.length; i++) {
                s.append(p[i]);

                for (Enumeration e = p[i].keys(); e.hasMoreElements();) {
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
    public KeyStoreTestImpl(String typeProvider, Provider testProvider) {
        this.testProvider = testProvider;
        this.typeProvider = typeProvider;
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
          + typeProvider
          + " ====");
        GroupKey key = genGroupKey();
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
        GroupKey gKey = genGroupKey();
        DocumentKey dKey = genDocumentKey();
        Exception exc = null;
        try {//save first time
            keystore.setKeyEntry(gKey.getId().toString(), gKey, null, null);
            keystore.setKeyEntry(dKey.getKey().toString(), dKey, null, null);
            assertTrue(keystore.containsAlias(gKey.getId().toString()));
            assertTrue(keystore.containsAlias(dKey.getKey().toString()));
        } catch (KeyStoreException ex) {
            fail("*** FAILED*** | " + ex.toString());
        }

        try {//save GroupKey second time
            keystore.setKeyEntry(gKey.getId().toString(), gKey, null, null);
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
        try {
            Enumeration aliases = keystore.aliases();
            while (aliases.hasMoreElements()) {

                String alias = (String) aliases.nextElement();
                keystore.deleteEntry(alias);
            }
        } catch (KeyStoreException ex) {
            fail("*** FAILED*** | " + ex.toString());
        }
        System.out.println("*** Passed *** | TestCase: Deleting Keys");
        System.out.println("==== Leaving KeyStore Tests ====");
    }

    /**
     * Tests, if the SetCertificate is all right.
     * The Keystore must be initialisized.
     */
    public void testSetCertficateSet() {
        //Is Exception thrown on null Entry?
        Exception ex = null;
        try {
            keystore.setCertificateEntry("bla", null);
        } catch (NullPointerException e) {
            ex = e;
        } catch (Exception r) {
            r.printStackTrace();
            fail("*** FAILED*** | Other Exceptions should not be thrown"
              + r.toString());
        }
        assertNotNull(ex);
        System.out.println("*** Passed *** | TestCase: [Certificate Store] Safe Null Alias Handling");

        ex = null;

        //Is Exception thrown on null Entry?
        X509Certificate cert = null;
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

            keystore.setCertificateEntry(null, cert);
        } catch (NullPointerException e) {
            ex = e;


        } catch (Exception r) {
            r.printStackTrace();
            fail("*** FAILED*** | Other Exceptions should not be thrown"
              + r.toString());
        }
        assertNotNull(ex);

        System.out.println("*** Passed *** |"
          + " TestCase: [Certificate Store] Safe Null Certificate Handling");


        //Load Mismatching Certificate

    }

    private GroupKey genGroupKey() {
        Cipher enc;
        GroupKey groupKey = null;
        byte[] key = new byte[TokenConstants.GKEY_LEN];
        byte[] identifier = new byte[TokenConstants.GKEY_ID_LEN];
        gen.nextBytes(key);
        gen.nextBytes(identifier);
        try {
            enc = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            enc.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encGroupKey = enc.doFinal(key);
            groupKey = new GroupKey(encGroupKey, identifier);
        } catch (Exception ex) {
            fail("*** FAILED*** | " + ex.toString());
        }
        return groupKey;
    }

    private DocumentKey genDocumentKey() {
        byte[] key = new byte[TokenConstants.DKEY_LEN];
        return new DocumentKey(key, false);
    }

    private void initKeyStore() {
        try {
            if (keystore == null) {

                keystore = KeyStore.getInstance(typeProvider, testProvider);
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

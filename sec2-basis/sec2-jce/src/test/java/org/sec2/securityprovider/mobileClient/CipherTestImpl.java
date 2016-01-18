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

import java.security.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import junit.framework.TestCase;
import org.sec2.securityprovider.serviceparameter.CipherAlgorithm;
import org.sec2.securityprovider.serviceparameter.PaddingAlgorithm;
import org.sec2.securityprovider.serviceparameter.PublicKeyType;
import org.sec2.securityprovider.serviceparameter.TokenType;
import org.sec2.token.TokenConstants;
import org.sec2.token.keys.ClusterKey;
import org.sec2.token.keys.DocumentKey;

/**
 * Abstracted UnitTests for Cipher Testing
 *
 * @author Jan Temme - Jan.Temme@rub.de
 * @version 0.1 May 20, 2012
 */
public abstract class CipherTestImpl extends TestCase {

    private Provider testProvider = null;

    private List<CipherAlgorithm> testCipherAlgorithms = null;
    private List<PaddingAlgorithm> testPaddingAlgorithms = null;
    private KeyStore keystore;

    public CipherTestImpl(Provider testProvider,
            
            List<CipherAlgorithm> cipherAlgorithms,
            List<PaddingAlgorithm> paddingAlgorithms) {
        this.testProvider = testProvider;
        this.testCipherAlgorithms = cipherAlgorithms;
        this.testPaddingAlgorithms = paddingAlgorithms;
         
    }

    protected void setUp() {
        if (this.testProvider == null) {
            fail("No test provider set.");
        }

        if (this.testCipherAlgorithms == null) {
            fail("No Cipher algorithms set.");
        }

        if (this.testPaddingAlgorithms == null) {
            fail("No Padding algorithms set.");
        }
        System.out.println("==== Starting Cipher Test "
                +testProvider.getProperty(TokenType.TOKEN_TYPE_IDENTIFIER)+
                " ====");
        System.out.println("# Test provider:      \t" + testProvider.getName());
        try {
            keystore = KeyStore.getInstance("Standard", testProvider);
            keystore.load(null, null);
        } catch (Exception ex) {
            fail("*** FAILED*** | " + ex.toString());
        }
    }

    protected void tearDown() {
        System.out.println("==== Leaving Cipher Test ====");
    }
    public void testCipher() {
        Cipher testCipher = null;
        ClusterKey clusKey;
        DocumentKey wrapMe = null;

        for (CipherAlgorithm cipherAlgo : this.testCipherAlgorithms) {
            for (PaddingAlgorithm paddingAlgo : this.testPaddingAlgorithms) {

                String transform = cipherAlgo.name() + "/CBC/"
                        + paddingAlgo.name();
                Random gen = new Random(1234567);
                byte[] keyMat = new byte[TokenConstants.DKEY_LEN];
                gen.nextBytes(keyMat);
                wrapMe = new DocumentKey(keyMat, false);
                try {
                    testCipher = Cipher.getInstance(transform, testProvider);
                    clusKey = getValidClusterKey();
                    testCipher.init(Cipher.WRAP_MODE, clusKey);
                    byte[] wrapedKey = testCipher.wrap(wrapMe);
                    testCipher.init(Cipher.UNWRAP_MODE, clusKey);
                    DocumentKey unwrapedKey = (DocumentKey) testCipher.unwrap(
                            wrapedKey, "AES", Cipher.SECRET_KEY);
                    keystore.deleteEntry(clusKey.getId().toString());
                    assertTrue(Arrays.equals(unwrapedKey.getKey().getBytes(),
                            wrapMe.getKey().getBytes()));
                } catch (InvalidKeyException ex) {
                    fail("*** FAILED *** | " + ex.toString());
                } catch (IllegalBlockSizeException ex) {
                    fail("*** FAILED *** | " + ex.toString());
                } catch (NoSuchAlgorithmException ex) {
                    fail("*** FAILED *** | " + ex.toString());
                } catch (NoSuchPaddingException ex) {
                    fail("*** FAILED *** | " + ex.toString());
                } catch (KeyStoreException ex) {
                    fail("*** FAILED *** | " + ex.toString());
                }
                System.out.println("*** Passed *** | algorithm: " + transform);
            }
        }
    }

    private ClusterKey getValidClusterKey() {
        Random gen = new Random(123456789);
        byte[] keyMat = new byte[TokenConstants.CKEY_LEN];
        byte[] keyId = new byte[TokenConstants.CKEY_ID_LEN];
        gen.nextBytes(keyMat);
        gen.nextBytes(keyId);
        Cipher enc;
        ClusterKey clusKey = null;
        try {
            enc = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            enc.init(Cipher.ENCRYPT_MODE, keystore.getKey(
                    PublicKeyType.CLIENT_ENCRYPTION.name(), null));
            byte[] encClusKey = enc.doFinal(keyMat);
            clusKey = new ClusterKey(encClusKey, keyId);
            keystore.setKeyEntry(clusKey.getId().toString(), clusKey, null, null);
        } catch (Exception ex) { //Should not happen, really not here
            fail("*** FAILED *** | " + ex.toString());
        }
        assertNotNull("Could not create ClusterKey", clusKey);
        return clusKey;
    }
}

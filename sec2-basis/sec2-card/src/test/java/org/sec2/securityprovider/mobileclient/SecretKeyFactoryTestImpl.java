/*
 * Copyright 2012 Sec2 Consortium
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

import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import junit.framework.TestCase;
import org.sec2.securityprovider.keys.KeyType;
import org.sec2.securityprovider.keys.Sec2SecretKeySpec;
import org.sec2.securityprovider.serviceparameter.CipherAlgorithm;
import org.sec2.token.TokenConstants;
import org.sec2.token.keys.DocumentKey;
import org.sec2.token.keys.GroupKey;

/**
 *
 * @author Jan 09.08.2012
 */
public class SecretKeyFactoryTestImpl extends TestCase {

    private List<CipherAlgorithm> testCipherAlgorithms = null;
    private Provider testProvider = null;

    public SecretKeyFactoryTestImpl(Provider testProvider,
      List<CipherAlgorithm> cipherAlgorithms) {
        this.testCipherAlgorithms = cipherAlgorithms;
        this.testProvider = testProvider;
    }

    protected void setUp() {
        System.out.println("==== Starting SecretKeyFactory Test ====");
        if (this.testProvider == null) {
            fail("No test provider set.");
        }

        if (this.testCipherAlgorithms == null) {
            fail("No Cipher algorithms set.");
        }

        System.out.println("# Test provider: \t" + testProvider.getName());
    }

    protected void tearDown() {
        System.out.println("==== Leaving SecretKeyFactory Test ====");
    }

    public void testGenerateSecret() {
        Sec2SecretKeySpec spec = null;
        SecretKeyFactory fact = null;
        try {
            spec = new Sec2SecretKeySpec(TokenConstants.DKEY_LEN,
              CipherAlgorithm.AES, KeyType.DOCUMENT);
            fact = SecretKeyFactory.getInstance(
              CipherAlgorithm.AES.name(), testProvider);
            DocumentKey key = (DocumentKey) fact.generateSecret(spec);
            assertEquals(key.getKey().getBytes().length, TokenConstants.DKEY_LEN);
            Sec2SecretKeySpec specNew = (Sec2SecretKeySpec) fact.getKeySpec((SecretKey) key,
              Sec2SecretKeySpec.class);
            assertEquals(specNew.getKeyType().name(),
              spec.getKeyType().name());
            assertEquals(specNew.getCipherAlgorithm().name(),
              spec.getCipherAlgorithm().name());
            assertEquals(specNew.getKeyLength(), spec.getKeyLength());
        } catch (InvalidKeySpecException ex) {
            fail("*** FAILED *** | " + ex.toString());
        } catch (NoSuchAlgorithmException ex) {
            fail("*** FAILED *** | " + ex.toString());
        } catch (IllegalArgumentException ex) {
            fail("*** FAILED *** | " + ex.toString());
        }
        Exception exc = null;
        try {
            spec = new Sec2SecretKeySpec(TokenConstants.GKEY_LEN,
              CipherAlgorithm.AES, KeyType.GROUP);
            GroupKey key = (GroupKey) fact.generateSecret(spec);
        } catch (InvalidKeySpecException ex) {
            exc = ex;
        }
        assertNotNull(exc);
        System.out.println("*** Passed *** | Cipher: AES");
        exc = null;
        try {
            spec = new Sec2SecretKeySpec(TokenConstants.DKEY_LEN,
              CipherAlgorithm.RSA, KeyType.DOCUMENT);
        } catch (IllegalArgumentException ex) {
            exc = ex;
        }
        assertNotNull(exc);
        System.out.println("*** Passed *** | Cipher: RSA");
    }
}

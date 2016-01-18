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

import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import static junit.framework.Assert.assertTrue;
import junit.framework.TestCase;
import org.sec2.securityprovider.serviceparameter.SignatureAlgorithm;
import org.sec2.securityprovider.serviceparameter.TokenType;
import static org.sec2.securityprovider.serviceparameter.TokenType.SOFTWARE_TEST_TOKEN_USER_1;
import static org.sec2.securityprovider.serviceparameter.TokenType.SOFTWARE_TEST_TOKEN_USER_2;
import static org.sec2.securityprovider.serviceparameter.TokenType.SOFTWARE_TOKEN;
import org.sec2.token.IToken;
import org.sec2.token.hwtoken.HardwareToken;
import org.sec2.token.keys.UserKey;
import org.sec2.token.swtoken.SoftwareToken;

/**
 * Abstracted UnitTests for Signature Testing
 *
 * @author Jan Temme - Jan.Temme@rub.de
 * @version 0.1 May 10, 2012
 */
public abstract class SignatureTestsImpl extends TestCase {

    private Provider testProvider = null;
    private Provider referenceProvider = null;
    private List<SignatureAlgorithm> testAlgorithms = null;
    public static final byte[] TEST_DATA = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};

    public SignatureTestsImpl(Provider testProvider,
            Provider referenceProvider,
            List<SignatureAlgorithm> algorithms) {
        this.referenceProvider = referenceProvider;
        this.testProvider = testProvider;
        this.testAlgorithms = algorithms;
    }

    protected void setUp() {
        System.out.println("==== Starting Signature Test "
                + testProvider.getProperty(TokenType.TOKEN_TYPE_IDENTIFIER)
                + "===");
        if (this.referenceProvider == null) {
            fail("No reference provider set.");
        }

        if (this.testProvider == null) {
            fail("No test provider set.");
        }

        if (this.testAlgorithms == null) {
            fail("No algorithms set.");
        }

        System.out.println("# Test provider:      \t" + testProvider.getName()
                + " on " + testProvider.getProperty(TokenType.TOKEN_TYPE_IDENTIFIER));
        System.out.println("# Reference provider: \t" + referenceProvider.getName());
    }

    protected void tearDown() {
        System.out.println("==== Leaving Signature Test ====");
    }

    public void testSignature() throws KeyStoreException, UnrecoverableKeyException {
        Signature testImpl;
        Signature refImpl;
        KeyPair keyPair;
        byte[] testSig;
        byte[] refSig;

        for (SignatureAlgorithm algo : this.testAlgorithms) {
            try {
                testImpl = Signature.getInstance(algo.name(), testProvider);
                refImpl = Signature.getInstance(algo.name(), referenceProvider);

                keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

                testImpl.initSign(null);
                refImpl.initSign(keyPair.getPrivate());

                testImpl.update(TEST_DATA);
                refImpl.update(TEST_DATA);
                testSig = testImpl.sign();
                refSig = refImpl.sign();

                testImpl.initVerify((PublicKey) null);
                testImpl.update(TEST_DATA);
                assertTrue(testImpl.verify(testSig));
                testImpl.update(TEST_DATA);
                testSig[3] = (byte) (testSig[3] + 1);
                assertFalse(testImpl.verify(testSig));

                RSAPublicKey pub = (RSAPublicKey) keyPair.getPublic();
                UserKey sec2Pub = new UserKey(pub.getModulus().toByteArray(),
                        pub.getPublicExponent().toByteArray());
                testImpl.initVerify((PublicKey) sec2Pub);
                testImpl.update(TEST_DATA);
                assertTrue(testImpl.verify(refSig));
                testImpl.update(TEST_DATA);
                refSig[3] = (byte) (refSig[3] + 1);
                assertFalse(testImpl.verify(refSig));

                System.out.println("*** Passed *** | algorithm: " + algo.name());
            } catch (NoSuchAlgorithmException ex) {
                fail("*** FAILED*** | " + ex.toString());
            } catch (InvalidKeyException ex) {
                fail("*** FAILED*** | " + ex.toString());
            } catch (SignatureException ex) {
                ex.printStackTrace();

                fail("*** FAILED*** | " + ex.toString());
            }
        }
    }
}

package org.sec2.backend.provider.test;

import java.io.ByteArrayInputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;

import junit.framework.TestCase;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Arrays;
import org.sec2.backend.impl.ConfigurationFactory;
import org.sec2.backend.provider.Sec2Provider;
import org.sec2.statictestdata.TestKeyProvider;

public class Sec2ProviderTest extends TestCase {

    private TestKeyProvider keyProvider;
    
    public final String TEST_PLAIN_TEXT = "Hallo Welt!";
    private Properties configuration;

    public Sec2ProviderTest(String name) {
        super(name);
        configuration = ConfigurationFactory.createDefault();
        try {
            Security.insertProviderAt(new Sec2Provider(configuration), 1);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.keyProvider = TestKeyProvider.getInstance();
    }

    /**
     * Tests the Sec2 provider's signing capabilities against itself and against
     * the BouncyCastle provider.
     * 
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws NoSuchPaddingException
     * @throws InvalidKeySpecException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public void testSignVerify() throws NoSuchAlgorithmException,
            NoSuchProviderException, NoSuchPaddingException,
            InvalidKeySpecException, InvalidKeyException, SignatureException {

        /**
         * Generate signature with the Sec2 provider.
         */
        Signature signer = Signature.getInstance(ConfigurationFactory
                .createDefault().getProperty("keyserver.saml.signature.type"),
                "Sec2");
        byte[] signature = null;
        try {
            signer.initSign(null);
            signer.update(TEST_PLAIN_TEXT.getBytes());
            signature = signer.sign();
        }
        catch (InvalidKeyException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        catch (SignatureException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        PublicKey publicKey = keyProvider.getKeyserverSignKey().getPublic();

        /**
         * Verify signature with the Sec2 provider.
         */
        signer.initVerify(publicKey);
        signer.update(TEST_PLAIN_TEXT.getBytes());
        boolean verified = signer.verify(signature);
        assertTrue(verified);

        /**
         * Verify signature with the BC provider.
         */
        Provider bouncyCastleProvider = new BouncyCastleProvider();
        Signature thirdPartyVerifier = Signature.getInstance(
                ConfigurationFactory.createDefault().getProperty(
                        "keyserver.saml.signature.type"), bouncyCastleProvider);
        thirdPartyVerifier.initVerify(publicKey);
        thirdPartyVerifier.update(TEST_PLAIN_TEXT.getBytes());
        verified = thirdPartyVerifier.verify(signature);
        assertTrue(verified);

        // Case 2

        /**
         * Create signature with third party provider (BC).
         */
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        thirdPartyVerifier.initSign(kp.getPrivate());
        thirdPartyVerifier.update(TEST_PLAIN_TEXT.getBytes());
        signature = thirdPartyVerifier.sign();

        /**
         * Verify signature with Sec2 provider.
         */
        signer.initVerify(kp.getPublic());
        signer.update(TEST_PLAIN_TEXT.getBytes());
        verified = signer.verify(signature);
        assertTrue(verified);

    }

    /**
     * 
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws CertificateException
     * @throws IllegalBlockSizeException
     * @throws NoSuchProviderException
     * @throws InvalidKeySpecException
     */
    public void testWrap() throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, CertificateException,
            IllegalBlockSizeException, NoSuchProviderException,
            InvalidKeySpecException {

        Provider bc = new BouncyCastleProvider();
        Cipher encapsulator = Cipher.getInstance(configuration.getProperty("keyserver.encapsulation.type"),
        "Sec2");

        byte[] userPKC = keyProvider.getUserEncCert().getEncoded();
        CertificateFactory cf = CertificateFactory.getInstance("x509");
        Certificate userCert = cf.generateCertificate(new ByteArrayInputStream(
                userPKC));

        encapsulator.init(Cipher.WRAP_MODE, userCert);

        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256);
        Key key = kg.generateKey();

        System.out.println("Plain key: \n"
                + DatatypeConverter.printHexBinary(key.getEncoded()));
        byte[] wrappedKey = encapsulator.wrap(key);
        System.out.println("Wrapped key: \n"
                + DatatypeConverter.printHexBinary(wrappedKey));

        PrivateKey userPrivateKey = keyProvider.getUserEncKey().getPrivate();

        Cipher decapsulator = Cipher.getInstance(configuration.getProperty("keyserver.encapsulation.type"),
                bc);
        decapsulator.init(Cipher.UNWRAP_MODE, userPrivateKey);

        Key unwrappedKey = decapsulator.unwrap(wrappedKey,
                configuration.getProperty(
                        "keyserver.encapsulation.type"), Cipher.SECRET_KEY);
        System.out.println("Unwrapped key: \n"
                + DatatypeConverter.printHexBinary(unwrappedKey.getEncoded()));
        assertTrue(Arrays.areEqual(unwrappedKey.getEncoded(), key.getEncoded()));
    }
    
    public void testUnwrap() throws NoSuchAlgorithmException,
    NoSuchPaddingException, InvalidKeyException, CertificateException,
    IllegalBlockSizeException, NoSuchProviderException,
    InvalidKeySpecException {
        Provider bc = new BouncyCastleProvider();
        Cipher encapsulator = Cipher.getInstance(configuration.getProperty("keyserver.encapsulation.type"),
                bc);
        
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256);
        Key key = kg.generateKey();
        
        System.out.println("Plain key: \n"
                + DatatypeConverter.printHexBinary(key.getEncoded()));
        
        Certificate keyserverCert = keyProvider.getKeyserverEncCert();
        
        encapsulator.init(Cipher.WRAP_MODE, keyserverCert);
        byte[] wrappedKey = encapsulator.wrap(key);
        System.out.println("Wrapped key: \n"
                + DatatypeConverter.printHexBinary(wrappedKey));
        
        PrivateKey keyserverPrivate = keyProvider.getKeyserverEncKey().getPrivate();
        
        Cipher decapsulator = Cipher.getInstance(configuration.getProperty("keyserver.encapsulation.type"),
                "Sec2");
        decapsulator.init(Cipher.UNWRAP_MODE, keyserverPrivate);
        
        Key unwrappedKey = decapsulator.unwrap(wrappedKey,
                configuration.getProperty(
                        "keyserver.encapsulation.type"), Cipher.SECRET_KEY);
        
        System.out.println("Unwrapped key: \n"
                + DatatypeConverter.printHexBinary(unwrappedKey.getEncoded()));
        assertTrue(Arrays.areEqual(unwrappedKey.getEncoded(), key.getEncoded()));
    }
    
}

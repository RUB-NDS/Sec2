package org.sec2.backend.keyserver.test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import junit.framework.TestCase;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.sec2.backend.impl.ConfigurationFactory;
import org.sec2.backend.impl.DatabaseServer;
import org.sec2.backend.impl.KeyServer;
import org.sec2.backend.impl.UserManagement;
import org.sec2.statictestdata.TestKeyProvider;

public class EncapsulateKeyTest extends TestCase {

    private KeyServer keyserver;
    private TestKeyProvider keyProvider;
    private BouncyCastleProvider bc;
    private Properties configuration;
    
    public EncapsulateKeyTest(String name) {
        super(name);
    }
    
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.keyProvider = TestKeyProvider.getInstance();
        this.configuration = ConfigurationFactory.createDefault();
        DatabaseServer databaseServer = new DatabaseServer(configuration);
        this.keyserver = new KeyServer(databaseServer, configuration);
        this.bc = new BouncyCastleProvider();
    }
    
    public void testEncapsulateKey() {
        System.out.println("begin testEncapsulateKey:");
        byte[] key = new byte[32];
        Random rnd = new Random();
        rnd.nextBytes(key);
        X509Certificate certificate = keyProvider.getUserEncCert();
        byte[] encapsulatedKey = keyserver.encapsulateKey(key, certificate);
        
        PrivateKey privateKey = keyProvider.getUserEncKey().getPrivate();
        Cipher decapsulator = null;
        try {
            decapsulator = Cipher.getInstance("RSA/NONE/OAEPPadding", bc);
            decapsulator.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decapsulatedKey = decapsulator.doFinal(encapsulatedKey);
            assertTrue("Decapsulated key differs from plain key", Arrays.equals(key, decapsulatedKey));
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        System.out.println("end testEncapsulateKey:");
    }
}
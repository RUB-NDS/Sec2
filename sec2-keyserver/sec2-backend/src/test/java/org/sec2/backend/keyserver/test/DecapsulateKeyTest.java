package org.sec2.backend.keyserver.test;

import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.Random;

import junit.framework.TestCase;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Arrays;
import org.sec2.backend.impl.ConfigurationFactory;
import org.sec2.backend.impl.DatabaseServer;
import org.sec2.backend.impl.KeyServer;
import org.sec2.statictestdata.TestKeyProvider;

public class DecapsulateKeyTest extends TestCase {

    private KeyServer keyserver;
    private TestKeyProvider keyProvider;
    private Properties configuration;
    private BouncyCastleProvider bc;
    
    public DecapsulateKeyTest(String name) {
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

    public void testDecapsulateKey() {
        System.out.println("begin testDecapsulateKey:");
        byte[] key = new byte[32];
        Random rnd = new Random();
        rnd.nextBytes(key);
        X509Certificate certificate = keyProvider.getKeyserverEncCert();
        
        byte[] encapsulatedKey = keyserver.encapsulateKey(key, certificate);
        
        byte[] decapsulatedKey = keyserver.decapsulateKey(encapsulatedKey);
        assertTrue("Decapsulated key differs from original", Arrays.areEqual(decapsulatedKey, key));
        System.out.println("end testDecapsulateKey:");
    }


}

package org.sec2.backend.keyserver.test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Properties;

import junit.framework.TestCase;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.sec2.backend.impl.ConfigurationFactory;
import org.sec2.backend.impl.DatabaseServer;
import org.sec2.backend.impl.KeyServer;
import org.sec2.backend.impl.UserManagement;
import org.sec2.statictestdata.TestKeyProvider;

public class HashTest extends TestCase {

    private KeyServer keyserver;
    private TestKeyProvider keyProvider;
    private Properties configuration; 
    
    public HashTest(String name) {
        super(name);
    }
    
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.keyProvider = TestKeyProvider.getInstance();
        configuration = ConfigurationFactory.createDefault();
        DatabaseServer databaseServer = new DatabaseServer(configuration);
        this.keyserver = new KeyServer(databaseServer, configuration);
    }


    public void testHash() {
        System.out.println("begin testHash:");
        byte[] data = "This will be hashed...".getBytes();
        
        BouncyCastleProvider bc = new BouncyCastleProvider();
        
        MessageDigest referenceDigester;
        byte[] referenceDigest = "a".getBytes();
        byte[] digest = "b".getBytes();
        try {
            referenceDigester = MessageDigest.getInstance(configuration.getProperty(KeyServer.KEYSERVER_MESSAGEDIGEST_TYPE), bc);
            referenceDigester.update(data);
            referenceDigest = referenceDigester.digest();
            digest = keyserver.hash(data);
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        
        assertTrue(Arrays.equals(referenceDigest, digest));
        
        System.out.println("end testHash:");
    }

}

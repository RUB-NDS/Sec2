package org.sec2.backend.keyserver.test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.Properties;

import junit.framework.TestCase;

import org.sec2.backend.exceptions.SignatureVerificationFailedException;
import org.sec2.backend.impl.ConfigurationFactory;
import org.sec2.backend.impl.DatabaseServer;
import org.sec2.backend.impl.KeyServer;
import org.sec2.backend.impl.UserManagement;
import org.sec2.statictestdata.TestKeyProvider;

public class ValidateTest extends TestCase {

    private KeyServer keyserver;
    private TestKeyProvider keyProvider;
    private Properties configuration;
    
    public ValidateTest(String name) {
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

    public void testValidate() {
        System.out.println("begin testValidate:");
        byte[] data = "This is the data to be signed".getBytes();
        byte[] signature = null;
        X509Certificate certificate = keyProvider.getUserSignCert();
        try {
            Signature signer = Signature.getInstance(configuration.getProperty(KeyServer.KEYSERVER_SAML_SIGNATURE_TYPE));
            signer.initSign(keyProvider.getUserSignKey().getPrivate());
            signer.update(data);
            signature = signer.sign();
            boolean valid = keyserver.validate(data, signature, certificate);
            assertTrue(valid);
        }
        catch (SignatureVerificationFailedException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        catch (InvalidKeyException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        catch (SignatureException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        System.out.println("end testValidate:");
    }
}

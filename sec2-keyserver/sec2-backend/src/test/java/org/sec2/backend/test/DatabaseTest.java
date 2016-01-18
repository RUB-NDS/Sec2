package org.sec2.backend.test;

/**
 * @author Utimaco Safeware
 * @XXX: more documentation
 */
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.util.Properties;

import javax.xml.bind.DatatypeConverter;

import junit.framework.TestCase;

import org.sec2.backend.impl.ConfigurationFactory;
import org.sec2.backend.impl.DatabaseServer;
import org.sec2.backend.impl.MailHelper;
import org.sec2.statictestdata.TestKeyProvider;

public class DatabaseTest extends TestCase {

    private TestKeyProvider keyProvider;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.keyProvider = TestKeyProvider.getInstance();
    }
    
    @Override
    protected void runTest() throws Throwable {
        //super.runTest();
    }
    
    public void testUserExists() throws CertificateEncodingException {
        System.out.println("begin testUserExists:");
        DatabaseServer db = new DatabaseServer(ConfigurationFactory
                .createDefault());
        byte[] userPKC = keyProvider.getUserSignCert().getEncoded();
        byte[] userId = TestUtil.calculateUserId(userPKC);

        // First test case: Empty user should not exist.
        boolean userExists = db.userExists(null, "");
        assertFalse(userExists);

        // Second test case: email address is sufficient to find an entry.
        userExists = db.userExists(null, MailHelper.extractEmailAddress(keyProvider.getUserSignCert()));
        assertTrue(userExists);

        // Third test case: user id is sufficient to find an entry.
        userExists = db.userExists(userId, "");
        assertTrue(userExists);

        System.out.println("end testUserExists.");
    }

    public void testGetAllKnownMembers() {
        System.out.println("begin testGetAllKnownMembers:");
        DatabaseServer db = new DatabaseServer(ConfigurationFactory.createDefault());

        // First test case: TODO: description
        String[] emails = db
                .getAllKnownMembers(MailHelper.extractEmailAddress(keyProvider.getUserEncCert()));
        for (String email : emails) {
            System.out.println(email);
        }

        System.out.println("end testGetAllKnownMembers.");
    }

    public void testGetAllKnownUsers() {
        
    }

    public void testAddUserToGroup() throws CertificateEncodingException {
        System.out.println("begin testAddUserToGroup:");
        DatabaseServer db = new DatabaseServer(ConfigurationFactory
                .createDefault());
        byte[] userPKC = keyProvider.getUserSignCert().getEncoded();
        byte[] userId = TestUtil.calculateUserId(userPKC);
        // First test case: Empty user should not exist.
        db.addUserToGroup(userId, "foobar");
        System.out.println("end testAddUserToGroup.");
    }
}

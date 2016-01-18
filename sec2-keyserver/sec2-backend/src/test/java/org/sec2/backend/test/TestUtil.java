package org.sec2.backend.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Properties;

import junit.framework.TestFailure;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.sec2.backend.IChallenge;
import org.sec2.backend.IGroupInfo;
import org.sec2.backend.exceptions.InvalidUserPKCException;
import org.sec2.backend.exceptions.UserAlreadyExistsException;
import org.sec2.backend.impl.ConfigurationFactory;
import org.sec2.backend.impl.AbstractUserManagementFactory;
import org.sec2.backend.impl.DatabaseServer;
import org.sec2.backend.impl.MailHelper;
import org.sec2.backend.impl.UserManagement;
import org.sec2.statictestdata.TestKeyProvider;

import CryptoServerAPI.CryptoServerUtil;

public class TestUtil {
    public static byte[] calculateUserId(byte[] userPKC) {
        X509Certificate certificate = null;
        CertificateFactory factory;
        try {
            factory = CertificateFactory.getInstance("X.509");
            certificate = (X509Certificate) factory
                    .generateCertificate(new ByteArrayInputStream(userPKC));
            MessageDigest digester = MessageDigest.getInstance("SHA-256");
            return digester.digest(certificate.getPublicKey().getEncoded());
        }
        catch (CertificateException e) {
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static void restoreDatabase() {
        System.out.print("Restoring Database...");
        try {
            Properties properties = ConfigurationFactory
                    .createDefault();
            DatabaseServer db = new DatabaseServer(properties);
            Connection connection = db.getConnection();
            PreparedStatement statement = connection
                    .prepareStatement("DELETE FROM users;");
            statement.executeUpdate();
            statement.close();
            statement = connection.prepareStatement("DELETE FROM groups;");
            statement.executeUpdate();
            statement.close();
            statement = connection
                    .prepareStatement("DELETE FROM group_members;");
            statement.executeUpdate();
            statement.close();
            connection.close();
            System.out.println(" success!");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    public static void createUser() {
        TestKeyProvider keyProvider = TestKeyProvider.getInstance();
        X509Certificate userEncCertificate = keyProvider.getUserEncCert();
        X509Certificate userSignCertificate = keyProvider.getUserSignCert();
        UserManagement um = AbstractUserManagementFactory.createDefault();
        try {
            um.register(userEncCertificate, userSignCertificate);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createUser2() {
        TestKeyProvider keyProvider = TestKeyProvider.getInstance();
        X509Certificate userEncCertificate = keyProvider.getUser2EncCert();
        X509Certificate userSignCertificate = keyProvider.getUser2SignCert();
        UserManagement um = AbstractUserManagementFactory.createDefault();
        try {
            um.register(userEncCertificate, userSignCertificate);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static byte[] createChallengeSignature(X509Certificate userSignatureCertificate, PrivateKey userPrivateKey) {
        TestKeyProvider keyProvider = TestKeyProvider.getInstance();
        BouncyCastleProvider bc = new BouncyCastleProvider();
        DatabaseServer databaseServer = new DatabaseServer(
                ConfigurationFactory.createDefault());
        String emailAddress = MailHelper.extractEmailAddress(userSignatureCertificate);
        IChallenge storedChallenge = databaseServer.fetchStoredChallenge(emailAddress);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.write(storedChallenge.getChallenge());
            dos.writeLong(storedChallenge.getIssueTimestamp());
            dos.flush();
    
            byte[] challenge = baos.toByteArray();
    
            CryptoServerUtil.xtrace("Timestamp with stored challenge @user",
                    challenge);
    
            // Sign challenge
            PrivateKey userSignKey = userPrivateKey;
    
            Signature signer = null;
            byte[] signature = null;
            signer = Signature.getInstance("SHA256withRSA", bc);
            signer.initSign(userSignKey);
            signer.update(challenge);
            signature = signer.sign();
            CryptoServerUtil.xtrace("Signed challenge with timestamp from user",
                    signature);
            return signature;
        } 
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static void confirmMail(X509Certificate userSignatureCertificate, PrivateKey userPrivateKey) {
        byte[] signature = createChallengeSignature(userSignatureCertificate, userPrivateKey);
        UserManagement um = AbstractUserManagementFactory.createDefault();
        try {
            um.confirmEmail(signature, userSignatureCertificate);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void createUserAndConfirmEmail() {
        TestKeyProvider keyProvider = TestKeyProvider.getInstance();
        createUser();
        confirmMail(keyProvider.getUserSignCert(), keyProvider.getUserSignKey().getPrivate());
    }
    
    public static void createUser2AndConfirmEmail() {
        TestKeyProvider keyProvider = TestKeyProvider.getInstance();
        createUser2();        
        confirmMail(keyProvider.getUser2SignCert(), keyProvider.getUser2SignKey().getPrivate());
    }
    
    // borrowed from
    // 'https://www.java2s.com/Open-Source/Java/Google-tech/appengineunit/com/sortedunderbelly/appengineunit/harness/junit3/JUnit3TestHarness.java.htm'
    public static String convertTestFailureToString(TestFailure failure) {
        String exceptionMessage = failure.toString();
        String trace = failure.trace();
        if (trace == null) {
            trace = "";
        }
        return exceptionMessage + trace;
    }

}

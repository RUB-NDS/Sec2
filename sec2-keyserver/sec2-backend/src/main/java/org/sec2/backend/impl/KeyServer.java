package org.sec2.backend.impl;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.sec2.backend.IChallenge;
import org.sec2.backend.IGroupInfo;
import org.sec2.backend.IUserInfo;
import org.sec2.backend.exceptions.PermissionException;
import org.sec2.backend.exceptions.SignatureVerificationFailedException;
import org.sec2.backend.exceptions.UserNotFoundException;

import CryptoServerAPI.CryptoServerException;
import CryptoServerAPI.CryptoServerUtil;
import CryptoServerCXI.CryptoServerCXI;
import CryptoServerCXI.CryptoServerCXI.KeyAttAndComp;
import CryptoServerCXI.CryptoServerCXI.KeyAttributes;
import CryptoServerJCE.CryptoServerKeyGenParameterSpec;
import CryptoServerJCE.CryptoServerProvider;

/**
 * 
 * @author Utimaco Safeware
 *
 */
public class KeyServer {

    /**
     * The logger.
     */
    private static final Logger LOGGER = Logger.getLogger(KeyServer.class.getName());

    
    public static final String KEYSERVER_SAML_SIGNATURE_TYPE = "keyserver.saml.signature.type";
    public static final String KEYSERVER_SAML_SIGNATURE_KEYNAME = "keyserver.saml.signature.keyname";
    public static final String KEYSERVER_PROVIDER = "keyserver.provider";
    public static final String KEYSERVER_KEYSTORE_TYPE = "keyserver.keystore.type";
    public static final String KEYSERVER_KEYSTORE_STREAM = "keyserver.keystore.stream";
    public static final String KEYSERVER_KEYSTORE_PASSWORD = "keyserver.keystore.password";
    public static final String KEYSERVER_SECURERANDOM_TYPE = "keyserver.securerandom.type";
    public static final String KEYSERVER_SECURERANDOM_PROVIDER = "keyserver.securerandom.provider";
    public static final String KEYSERVER_MESSAGEDIGEST_TYPE = "keyserver.messagedigest.type";
    public static final String KEYSERVER_MESSAGEDIGEST_PROVIDER = "keyserver.messagedigest.provider";
    public static final String KEYSERVER_ENCAPSULATION_TYPE = "keyserver.encapsulation.type";
    public static final String KEYSERVER_ENCAPSULATION_TYPE_INERNAL = "keyserver.encapsulation.type.internal";
    public static final String KEYSERVER_ENCAPSULATION_KEYNAME = "keyserver.encapsulation.keyname";
    public static final String KEYSERVER_GROUPS_KEYS_LENGTH = "keyserver.groups.keys.length";
    public static final String KEYSERVER_GROUPS_KEYS_TYPE = "keyserver.groups.keys.type";
    public static final String KEYSERVER_CHALLENGE_SIZE = "keyserver.challenge.size";
    
    public static final String KEYSERVER_HSM_HOSTNAME = "hsm.host";
    public static final String KEYSERVER_HSM_TIMEOUT = "hsm.timeout";
    public static final String KEYSERVER_HSM_USERNAME = "hsm.username";
    public static final String KEYSERVER_HSM_PASSWORD = "hsm.password";

    private DatabaseServer databaseServer;
    private BouncyCastleProvider bc;
    private Properties properties;

    /**
     * Contructs a new Keyserver object.
     * 
     * @param databaseServer The DatabaseServer object which is responsible for 
     *                       persistence.
     * @param configuration  The configuration {@link Properties} file, which 
     *                       is used to configure the KeyServer. 
     */
    public KeyServer(DatabaseServer databaseServer, Properties configuration) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                KeyServer.class.getName(),
                "KeyServer", 
                new Object[]{ databaseServer, configuration }
            );
        }
        
        this.properties = configuration;
        this.bc = new BouncyCastleProvider();
        this.databaseServer = databaseServer;
        initializeKeyServerProvider();

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(
                KeyServer.class.getName(),
                "KeyServer"
            );
        }
    }
    
    
    /**
     * This method initializes the JCE provider used for key storage.
     */
    private void initializeKeyServerProvider() {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                KeyServer.class.getName(),
                "initializeKeyServerProvider"
            );
        }
        try {
            if (Security.getProvider(properties.getProperty(KEYSERVER_PROVIDER)) != null ) {
                // KeyServer provider is already registered to JCE.
                return;  
            }
            CryptoServerProvider cs = new CryptoServerProvider(
                properties.getProperty(KEYSERVER_HSM_HOSTNAME), 
                Integer.parseInt(properties.getProperty(KEYSERVER_HSM_TIMEOUT))
            );
            cs.loginPassword(
                properties.getProperty(KEYSERVER_HSM_USERNAME),
                properties.getProperty(KEYSERVER_HSM_PASSWORD)
            );
            // keeps the session alive (polls HSM every once in a while)
            cs.getCryptoServer().setKeepSessionAlive(true);
            Security.addProvider(cs);
        } catch (Exception e) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(e.getMessage());
                e.printStackTrace();
            }
        } finally {
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.exiting(
                    KeyServer.class.getName(),
                    "initializeKeyServerProvider"
                );
            }
        }
    }

    public List<IGroupInfo> getAllKnownGroups(byte[] operatorId) throws UserNotFoundException, PermissionException {
        X509Certificate certificate = databaseServer.fetchEncryptionPKC(operatorId);
        List<IGroupInfo> groups = new ArrayList<IGroupInfo>();
        List<String> groupNames = databaseServer.fetchKnownGroupNames(operatorId);
        for(String groupName : groupNames) {
            IGroupInfo group = getGroupInfo(operatorId, groupName, certificate);
            groups.add(group);
        }
        return groups;
    }
    
    /**
     * This method decapsulates an encapsulated key.
     * 
     * @param encapsulatedKey The encapsulated key.
     * @return The decapsulated key encoded as a byte array.
     */
    public byte[] decapsulateKey(byte[] encapsulatedKey) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                KeyServer.class.getName(),
                "decapsulateKey", 
                encapsulatedKey
            );
        }
        // XXX: this part really sucks due to the missing unwrap
        // functionality...
        byte[] decapsulatedKeyWithPadding = null;
        try {
            KeyStore ks = KeyStore.getInstance(
                properties.getProperty(KEYSERVER_KEYSTORE_TYPE), 
                properties.getProperty(KEYSERVER_PROVIDER)
            );
            InputStream keystoreStream = null;
            if (!properties.getProperty(KEYSERVER_KEYSTORE_STREAM).equals("")) {
                keystoreStream = new FileInputStream(
                    properties.getProperty(KEYSERVER_KEYSTORE_STREAM)
                );
            }
            char[] keystorePassword = null;
            if (!properties.getProperty(KEYSERVER_KEYSTORE_PASSWORD).equals("")) {
                keystorePassword = properties.getProperty(
                        KEYSERVER_KEYSTORE_PASSWORD).toCharArray();
            }
            ks.load(keystoreStream, keystorePassword);

            PrivateKey keyserverKey = (PrivateKey) ks.getKey(
                properties.getProperty(KEYSERVER_ENCAPSULATION_KEYNAME), 
                null
            );
            Cipher decapsulator = Cipher.getInstance(
                properties.getProperty(KEYSERVER_ENCAPSULATION_TYPE_INERNAL), 
                properties.getProperty(KEYSERVER_PROVIDER)
            );
            decapsulator.init(
                Cipher.DECRYPT_MODE, 
                keyserverKey
                ,OAEPParameterSpec.DEFAULT
            );
            decapsulatedKeyWithPadding = decapsulator.doFinal(encapsulatedKey);
            CryptoServerUtil.xtrace(decapsulatedKeyWithPadding);
        }
        catch (KeyStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (NoSuchProviderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (CertificateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (UnrecoverableKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InvalidAlgorithmParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(
                KeyServer.class.getName(),
                "decapsulateKey",
                decapsulatedKeyWithPadding
            );
        }
        return decapsulatedKeyWithPadding;
    }

    /**
     * This method encapsulates a key with the public key of the given certificate.
     * 
     * @param key         The key (as a byte array) which will be encapsulated
     * @param certificate The {@code X509Certificate} which contains the 
     *                    {@code PublicKey} used for the encapsulation
     * @return The encapsulated key
     * @see #encapsulateKey(SecretKey, X509Certificate)
     * @see #encapsulateKey(SecretKey, PublicKey)
     */
    public byte[] encapsulateKey(byte[] key, X509Certificate certificate) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                KeyServer.class.getName(),
                "encapsulateKey",
                new Object[]{key, certificate}
            );
        }
        byte[] encapsulatedKey = encapsulateKey(new SecretKeySpec(key, "AES"), certificate);
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(
                KeyServer.class.getName(),
                "encapsulateKey",
                encapsulatedKey
            );
        }
        return encapsulatedKey; 
    }

    /**
     * This method encapsulates a key with the public key of the given certificate.
     * 
     * @param groupKey
     * @param publicKey
     * @return
     */
    public byte[] encapsulateKey(SecretKey groupKey, PublicKey publicKey) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                KeyServer.class.getName(),
                "encapsulateKey",
                new Object[]{groupKey, publicKey}
            );
        }
        byte[] encapsulatedKey = null;
        try {
            // TODO: if the patch for key encapsulation is there, change this
            // back to use the CryptoServerJCEProvider
            Cipher encapsulator = Cipher.getInstance(
                properties.getProperty(KEYSERVER_ENCAPSULATION_TYPE), 
                bc
            );
            encapsulator.init(Cipher.WRAP_MODE, publicKey);
            encapsulatedKey = encapsulator.wrap(groupKey);
        }
        catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(
                KeyServer.class.getName(),
                "encapsulateKey",
                encapsulatedKey
            );
        }
        return encapsulatedKey;
    }

    /**
     * 
     * @param groupKey
     * @param certificate
     * @return
     */
    public byte[] encapsulateKey(SecretKey groupKey, X509Certificate certificate) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                KeyServer.class.getName(),
                "encapsulateKey",
                new Object[]{groupKey, certificate}
            );
        }
        PublicKey publicKey = null;
        try {
            RSAPublicKey sunRSAPublicKey = (RSAPublicKey) certificate
                    .getPublicKey();
            RSAPublicKeySpec spec = new RSAPublicKeySpec(sunRSAPublicKey
                    .getModulus(), sunRSAPublicKey.getPublicExponent());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", bc);
            publicKey = (PublicKey) keyFactory.generatePublic(spec);
        }
        catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InvalidKeySpecException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] encapsulatedKey = encapsulateKey(groupKey, publicKey);
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(
                KeyServer.class.getName(),
                "encapsulateKey",
                encapsulatedKey
            );
        }
        return encapsulatedKey;
    }

    /**
     * Generates a random challenge. The size of the challenge is referenced 
     * in the configuration file by the name {@link #KEYSERVER_CHALLENGE_SIZE}.
     * 
     * @return The generated challenge
     */
    public final byte[] generateChallenge() {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                KeyServer.class.getName(),
                "generateChallenge"
            );
        }
        SecureRandom srnd;
        byte[] challenge = new byte[Integer.parseInt(properties
                .getProperty(KEYSERVER_CHALLENGE_SIZE))];
        try {
            srnd = SecureRandom.getInstance(
                properties.getProperty(KEYSERVER_SECURERANDOM_TYPE), 
                properties.getProperty(KEYSERVER_SECURERANDOM_PROVIDER)
            );
            srnd.nextBytes(challenge);
        }
        catch (Exception e) {
            // If this fails, something has terribly gone wrong...
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(e.getMessage());
                e.printStackTrace();
            }
        }
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(
                KeyServer.class.getName(),
                "generateChallenge",
                challenge
            );
        }
        return challenge;
    }

    /**
     * Generates a group key with the name {@code keyName} and 
     * stores it in the HSM.
     * 
     * @param keyName Name of the key
     */
    public void generateGroupKey(String keyName) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                KeyServer.class.getName(),
                "generateGroupKey"
            );
        }
        KeyGenerator kg;
        try {
            kg = KeyGenerator.getInstance(properties
                    .getProperty(KEYSERVER_GROUPS_KEYS_TYPE), properties
                    .getProperty(KEYSERVER_PROVIDER));
            // XXX: This is ugly as hell but there is currently no other way to do it :(
            // -----------------------------------------------------------
            CryptoServerKeyGenParameterSpec spec = new CryptoServerKeyGenParameterSpec(
                    Integer.parseInt(properties
                            .getProperty(KEYSERVER_GROUPS_KEYS_LENGTH)));
            spec.setKeySize(Integer.parseInt(properties
                    .getProperty(KEYSERVER_GROUPS_KEYS_LENGTH)));
            spec.setExportable(true);
            spec.setPlainExportable(true);
            // -----------------------------------------------------------
            kg.init(spec);

            // kg.init(Integer.parseInt(properties.getProperty(KEYSERVER_GROUPS_KEYS_LENGTH)));
            SecretKey key = kg.generateKey();
            KeyStore ks = KeyStore.getInstance(properties
                    .getProperty(KEYSERVER_KEYSTORE_TYPE), properties
                    .getProperty(KEYSERVER_PROVIDER));
            ks.load(null, null);
            ks.setKeyEntry(keyName, key, null, null);
        }
        catch (NoSuchAlgorithmException e) {
            // If this fails, something has terribly gone wrong...
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(e.getMessage());
                e.printStackTrace();
            }
        }
        catch (KeyStoreException e) {
            // If this fails, something has terribly gone wrong...
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(e.getMessage());
                e.printStackTrace();
            }
        }
        catch (NoSuchProviderException e) {
            // If this fails, something has terribly gone wrong...
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(e.getMessage());
                e.printStackTrace();
            }
        }
        catch (CertificateException e) {
            // If this fails, something has terribly gone wrong...
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(e.getMessage());
                e.printStackTrace();
            }
        }
        catch (IOException e) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(e.getMessage());
                LOGGER.fine("Connection to HSM might have failed?!");
                e.printStackTrace();
            }
        }
        catch (InvalidAlgorithmParameterException e) {
            // If this fails, something has terribly gone wrong...
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(e.getMessage());
                e.printStackTrace();
            }
        }
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(
                KeyServer.class.getName(),
                "generateGroupKey"
            );
        }
    }

    /**
     * Retrieves information about the group identified by {@code groupName}. 
     * If the user identified by {@code userId} is no member of the group a 
     * {@link PermissionException} will be thrown.
     * 
     * @param userId      ID of the user (SHA-256 hash of the user's signing 
     *                    certificate)
     * @param groupName   Name of the group
     * @param certificate Key encryption certificate of the user identified by 
     *                    {@code userId} 
     * @return {@link IGroupInfo} object containing information about the group
     * @throws PermissionException Thrown if the user identified by 
     *                             {@code userId} is no member of the group
     */
    public final IGroupInfo getGroupInfo(byte[] userId, String groupName,
            X509Certificate certificate) 
    throws PermissionException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                KeyServer.class.getName(),
                "getGroupInfo",
                new Object[]{userId, groupName, certificate}
            );
        }
        
        // Simple input validation.
        if (userId == null || groupName == null || groupName.isEmpty() || certificate == null) {
            throw new PermissionException();
        }
        
        // Check if user is member of group
        if (!databaseServer.isGroupMember(userId, groupName)) {
            throw new PermissionException();
        }
        SecretKey groupKey = (SecretKey) loadKey(groupName);
        if (groupKey == null) {
            // XXX check Exception type 
            throw new PermissionException();
        }
        GroupInfo groupInfo = databaseServer.fetchBasicGroupInfo(groupName);
        if (groupInfo == null) {
            // XXX check Exception type
            throw new PermissionException();
        }
        byte[] encapsulatedKey = encapsulateKey(groupKey, certificate);
        groupInfo.setEncapsulatedKey(encapsulatedKey);
        
        ArrayList<IUserInfo> groupMembers = databaseServer.fetchGroupMembers(groupName);
        groupInfo.setMembers(groupMembers);
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(
                KeyServer.class.getName(),
                "getGroupInfo",
                groupInfo
            );
        }
        return groupInfo;
    }

    /**
     * Generates a hash over the passed in {@code data}. The used hash 
     * algorithm and provider are defined by the configuration properties 
     * {@link #KEYSERVER_MESSAGEDIGEST_TYPE} and 
     * {@link #KEYSERVER_MESSAGEDIGEST_PROVIDER}.
     * 
     * @param data Data to be hashed
     * @return     Hash value of the data or null if data is null. 
     */
    public final byte[] hash(byte[] data) {
        if(data == null) return null;
        byte[] hash = null;
        try {
            MessageDigest digester = MessageDigest.getInstance(properties
                    .getProperty(KEYSERVER_MESSAGEDIGEST_TYPE), properties
                    .getProperty(KEYSERVER_MESSAGEDIGEST_PROVIDER));
            hash = digester.digest(data);
        }
        catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (NoSuchProviderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return hash;
    }

    /**
     * 
     * @param keyName
     * @return
     */
    private final Key loadKey(String keyName) {
        // logger.debug("loadKey <" + keyName + ">");
        KeyStore keystore;
        Key key = null;
        try {
            keystore = KeyStore.getInstance(properties
                    .getProperty(KEYSERVER_KEYSTORE_TYPE), properties
                    .getProperty(KEYSERVER_PROVIDER));
            // InputStream keystoreStream = null;
            // if(!properties.getProperty(KEYSERVER_KEYSTORE_STREAM).equals(""))
            // {
            // keystoreStream = new
            // FileInputStream(properties.getProperty(KEYSERVER_KEYSTORE_STREAM));
            // }
            // char[] keystorePassword = null;
            // if(!properties.getProperty(KEYSERVER_KEYSTORE_PASSWORD).equals(""))
            // {
            // System.out.println(properties.getProperty(KEYSERVER_KEYSTORE_PASSWORD));
            // keystorePassword =
            // properties.getProperty(KEYSERVER_KEYSTORE_PASSWORD).toCharArray();
            // }
            // keystore.load(keystoreStream, keystorePassword);
            keystore.load(null, null);
            byte[] rawGroupKey = null;

            // XXX: there is currently no other way to export the key from the
            // HSM so we need to export the key manually to the server so that
            // we can wrap it later on.
            // ---------------------------------------------------------
            CryptoServerProvider prov = (CryptoServerProvider) Security
                    .getProvider(properties
                            .getProperty(KEYSERVER_KEYSTORE_TYPE));
            CryptoServerCXI cxi = prov.getCryptoServer();
            CryptoServerCXI.KeyAttributes cxiKeyAttr = new KeyAttributes();
            cxiKeyAttr.setName(keyName);
            CryptoServerCXI.Key cxiGroupKey = cxi.findKey(cxiKeyAttr);
            if (cxiGroupKey == null) {
                System.out.println("Key not found?! " + keyName);
                // TODO: error handling
            }
            KeyAttAndComp kaac = cxi.exportClearKey(cxiGroupKey,
                    CryptoServerCXI.KEY_TYPE_SECRET);
            rawGroupKey = Arrays.copyOfRange(kaac.keyComponents.getList(), 0,
                    16);
            key = new SecretKeySpec(rawGroupKey, "AES");
            // ---------------------------------------------------------

            // key = keystore.getKey(keyName, KEYSTORE_KEYSTORE_PASSWORD);
        }
        catch (KeyStoreException e) {
            // TODO Auto-generated catch block
            System.out.println("" + e);
            e.printStackTrace();
        }
        catch (NoSuchProviderException e) {
            // TODO Auto-generated catch block
            System.out.println("" + e);
            e.printStackTrace();
        }
        catch (IOException e) {
            System.out.println("" + e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (CryptoServerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (CertificateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return key;
    }

    /**
     * 
     * @param data
     * @param signature
     * @param certificate
     * @return
     * @throws SignatureVerificationFailedException
     */
    public final boolean validate(byte[] data, byte[] signature,
            X509Certificate certificate)
            throws SignatureVerificationFailedException {
        Signature verifier;
        SignatureVerificationFailedException ex = new SignatureVerificationFailedException();
        try {
            verifier = Signature.getInstance(properties
                    .getProperty(KEYSERVER_SAML_SIGNATURE_TYPE));
            verifier.initVerify(certificate);
            verifier.update(data);
            boolean verified = verifier.verify(signature);
            if (!verified)
                throw ex;
            return verified;
        }
        catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw ex;
        }
        catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw ex;
        }
        catch (SignatureException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw ex;
        }
    }

    /**
     * 
     * @param storedChallenge
     * @param signature
     * @param certificate
     * @return
     * @throws SignatureVerificationFailedException
     */
    public final boolean verifyChallenge(IChallenge storedChallenge, byte[] signature,
            X509Certificate certificate)
            throws SignatureVerificationFailedException {
        Signature verifier;
        SignatureVerificationFailedException ex = new SignatureVerificationFailedException();
        boolean verified = false;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            try {
                dos.write(storedChallenge.getChallenge());
                dos.writeLong(storedChallenge.getIssueTimestamp());
                dos.flush();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            byte[] storedChallengeByteArray = baos.toByteArray();
            
            verifier = Signature.getInstance(properties
                    .getProperty(KEYSERVER_SAML_SIGNATURE_TYPE));
            verifier.initVerify(certificate);
            verifier.update(storedChallengeByteArray);
            verified = verifier.verify(signature);
            if (!verified)
                throw ex;
        }
        catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (SignatureException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw ex;
        }
        return verified;
    }

}

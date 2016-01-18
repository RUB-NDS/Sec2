/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.managers.impl;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import junit.framework.TestCase;
import org.sec2.managers.exceptions.KeyManagerException;
import org.sec2.managers.factories.KeyManagerFactory;
import org.sec2.securityprovider.exceptions.IllegalPostInstantinationModificationException;
import org.sec2.securityprovider.mobileclient.MobileClientProvider;
import org.sec2.securityprovider.serviceparameter.PublicKeyType;
import org.sec2.securityprovider.serviceparameter.TokenType;
import org.sec2.token.TokenConstants;
import org.sec2.token.keys.DocumentKey;
import org.sec2.token.keys.ExtendedDocumentKey;
import org.sec2.token.keys.GroupKey;
import org.sec2.token.keys.GroupKeyId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.1
 */
public class DocumentKeyManagerTests extends TestCase {

    /**
     * Provider to test.
     */
    private Provider testProvider = null;
    /**
     * Keystore Instance from testprovider
     */
    private KeyStore keystore = null;
    /**
     * logger
     */
    private static Logger logger =
            LoggerFactory.getLogger(DocumentKeyManagerTests.class);
    /**
     * secure random
     */
    private final SecureRandom random = new SecureRandom();
    /**
     * public key
     */
    private PublicKey publicKey;
    private DocumentKeyManagerImpl dkm;
    /*
     * Software or hardware Token chooser.
     */
    public static TokenType TOKEN_TYPE = TokenType.UNSPECIFIED; //TokenType.SOFTWARE_TOKEN;

    /**
     * Test constructor with test provider initialization
     */
    public DocumentKeyManagerTests() {
        try {
            MobileClientProvider.setType(TOKEN_TYPE);
        } catch (IllegalPostInstantinationModificationException ex) {
            logger.debug("Couldn't set Tokentype {}", TOKEN_TYPE);
        }

        this.testProvider = MobileClientProvider.getInstance(
                TokenConstants.DEFAULT_PIN);
        Security.insertProviderAt(testProvider, 1);
        logger.debug("Current Token"
                + "type used in Provider is {}",
                testProvider.getProperty(TokenType.TOKEN_TYPE_IDENTIFIER));
    }

    /**
     * Search for a document key, which is already decrypted in the keystore.
     *
     * @throws KeyStoreException
     */
    public void testCase1a() throws KeyManagerException, KeyStoreException,
            NoSuchAlgorithmException, UnrecoverableKeyException {
        logger.debug("Testing DocumentKeyManger: Fetching a document key that "
                + "is already stored in the keystore");

        ExtendedDocumentKey edk = dkm.getDecryptedDocumentKeyForGroups("group111;group222");

        DocumentKey k = (DocumentKey) keystore.getKey("d_group111;group222;nonce124", null);

        assertTrue(Arrays.equals(
                edk.getDecryptedDocumentKey().getKey().getBytes(),
                k.getKey().getBytes()));
        assertEquals(edk.getEncryptedDocumentKeys().size(), 2);
        assertEquals("group111;group222", edk.getGroups());
    }

    /**
     * Search for a document key, which is not in the keystore. Create a new
     * document key and store it together with all the encrypted document keys
     *
     * @throws KeyStoreException
     */
    public void testCase1b() throws KeyManagerException, KeyStoreException,
            NoSuchAlgorithmException, UnrecoverableKeyException {
        logger.debug("Testing DocumentKeyManger: Fetching a document key that "
                + "is not yet stored in the keystore");

        ExtendedDocumentKey edk = dkm.getDecryptedDocumentKeyForGroups("group111");
        assertEquals(edk.getEncryptedDocumentKeys().size(), 1);

        edk = dkm.getDecryptedDocumentKeyForGroups("group111;group333");
        assertEquals(edk.getEncryptedDocumentKeys().size(), 2);
    }

    /**
     * Encrypted Document comes from the cloud, it contains one document key
     * encrypted with two group keys. The document key is already stored in the
     * keystore.
     *
     * @throws KeyStoreException
     */
    public void testCase2a() throws KeyManagerException, KeyStoreException,
            NoSuchAlgorithmException, UnrecoverableKeyException {
        logger.debug("Testing DocumentKeyManger: Fetching a document key that "
                + "is not yet stored in the keystore");

        DocumentKey k1 = (DocumentKey) keystore.getKey("d_group111;group111;group222;nonce124", null);
        DocumentKey k2 = (DocumentKey) keystore.getKey("d_group222;group111;group222;nonce124", null);

        List<DocumentKey> keys = new LinkedList<DocumentKey>();
        keys.add(k1);
        keys.add(k2);

        DocumentKey decDK = dkm.getDecryptedDocumentKey(keys).getDecryptedDocumentKey();

        assertFalse(decDK.isEncrypted());

        DocumentKey k = (DocumentKey) keystore.getKey("d_group111;group222;nonce124", null);
        assertTrue(Arrays.equals(decDK.getKey().getBytes(), k.getKey().getBytes()));
    }

    /**
     * Encrypted Document comes from the cloud, it contains one document key
     * encrypted with two group keys. The document key is not yet stored in the
     * keystore.
     *
     * @throws KeyStoreException
     */
    public void testCase2b() throws KeyManagerException, KeyStoreException,
            NoSuchAlgorithmException, UnrecoverableKeyException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException {
        logger.debug("Testing DocumentKeyManger: Fetching a document key that "
                + "is not yet stored in the keystore");

        DocumentKey dKey = genDocumentKey("group222;group333");

        GroupKey gk2 = (GroupKey) keystore.getKey(new GroupKeyId("group222").toString(), null);
        GroupKey gk3 = (GroupKey) keystore.getKey(new GroupKeyId("group333").toString(), null);

        Cipher c = Cipher.getInstance("AES/CBC/NONE", testProvider);
        c.init(Cipher.WRAP_MODE, gk2);
        byte[] wrapedKey = c.wrap(dKey);
        DocumentKey dk223 = new DocumentKey(wrapedKey, true,
                "group222;group222;group333;nonce124");

        c.init(Cipher.WRAP_MODE, gk3);
        wrapedKey = c.wrap(dKey);
        DocumentKey dk323 = new DocumentKey(wrapedKey, true,
                "group333;group222;group333;nonce124");

        List<DocumentKey> keys = new LinkedList<DocumentKey>();
        keys.add(dk223);
        keys.add(dk323);

        DocumentKey decDK = dkm.getDecryptedDocumentKey(keys).getDecryptedDocumentKey();

        assertFalse(decDK.isEncrypted());
        assertTrue(Arrays.equals(decDK.getKey().getBytes(), dKey.getKey().getBytes()));

        // no rights
        byte[] key = new byte[TokenConstants.DKEY_LEN];
        random.nextBytes(key);
        dKey = new DocumentKey(key, true, "group444;group444;nonce124");
        keys.clear();
        keys.add(dKey);

        KeyManagerException ex = null;
        try {
            decDK = dkm.getDecryptedDocumentKey(keys).getDecryptedDocumentKey();
        } catch (KeyManagerException e) {
            ex = e;
        }
        assertNotNull(ex);
    }

    public void testUtils() {
        String[] x = DocumentKeyManagerImpl.getGroupsFromKeyId("g1;g2;g3");
        assertEquals(3, x.length);
        String id = DocumentKeyManagerImpl.getEncryptionGroupKeyFromKeyId("g1;g1;g2;g3");
        assertEquals("g1", id);
        id = DocumentKeyManagerImpl.getKeyIdFromEncryptionGroupKeyId("g1;g1;g2;g3");
        assertEquals("g1;g2;g3", id);

        id = DocumentKeyManagerImpl.getKeyIdWithoutNonce("g1;g2;g3;23424142");
        assertEquals("g1;g2;g3", id);
    }

    private void initKeyStore() throws KeyStoreException, IOException,
            NoSuchAlgorithmException, CertificateException,
            UnrecoverableKeyException, IllegalBlockSizeException,
            BadPaddingException, NoSuchPaddingException, InvalidKeyException,
            KeyManagerException {


        publicKey = (PublicKey) keystore.getKey(
                PublicKeyType.CLIENT_ENCRYPTION.name(), null);
        this.initDocumentKeyManager();
        this.initKeys();
    }

    private void initDocumentKeyManager() throws KeyManagerException {
        dkm = (DocumentKeyManagerImpl) KeyManagerFactory.
                getDocumentKeyManager();
        dkm.groupManager = new DummyGroupManager();
    }

    private void initKeys() throws NoSuchAlgorithmException,
            IllegalBlockSizeException, BadPaddingException,
            NoSuchPaddingException, InvalidKeyException, KeyStoreException {
        logger.debug("Initializing the keystore with three group keys");
        GroupKey gKey1 = genGroupKey("group111");
        GroupKey gKey2 = genGroupKey("group222");
        GroupKey gKey3 = genGroupKey("group333");
        keystore.setKeyEntry(gKey1.getId().toString(), gKey1, null, null);
        keystore.setKeyEntry(gKey2.getId().toString(), gKey2, null, null);
        keystore.setKeyEntry(gKey3.getId().toString(), gKey3, null, null);

        logger.debug("Generating a new document key");
        DocumentKey dKey = genDocumentKey("d_group111;group222;nonce124");
        keystore.setKeyEntry(dKey.getKeyId(), dKey, null, null);
        List<String> list = new LinkedList<String>();
        list.add("group111;group222;nonce124");
        dkm.documentKeyIds.put("group111;group222", list);

        logger.debug("Encrypting the document key with gk1");
        Cipher c = Cipher.getInstance("AES/CBC/NONE", testProvider);
        c.init(Cipher.WRAP_MODE, gKey1);
        byte[] wrapedKey = c.wrap(dKey);
        DocumentKey wrapMe = new DocumentKey(wrapedKey, true,
                "d_group111;group111;group222;nonce124");
        keystore.setKeyEntry(wrapMe.getKeyId(), wrapMe, null, null);
        list = new LinkedList<String>();
        list.add("group111;group111;group222;nonce124");
        dkm.documentKeyIds.put("group111;group111;group222", list);

        logger.debug("Encrypting the document key with gk2");
        c.init(Cipher.WRAP_MODE, gKey2);
        wrapedKey = c.wrap(dKey);
        wrapMe = new DocumentKey(wrapedKey, true,
                "d_group222;group111;group222;nonce124");
        keystore.setKeyEntry(wrapMe.getKeyId(), wrapMe, null, null);
        list = new LinkedList<String>();
        list.add("group222;group111;group222;nonce124");
        dkm.documentKeyIds.put("group222;group111;group222", list);
    }

    private DocumentKey genDocumentKey(String dkId) {
        byte[] key = new byte[TokenConstants.DKEY_LEN];
        random.nextBytes(key);
        return new DocumentKey(key, false, dkId);
    }

    private GroupKey genGroupKey(String id) throws NoSuchAlgorithmException,
            IllegalBlockSizeException, BadPaddingException,
            NoSuchPaddingException, InvalidKeyException {
        logger.debug("Generating group key " + id);
        Cipher enc;
        byte[] key = new byte[TokenConstants.GKEY_LEN];
        random.nextBytes(key);
        enc = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        enc.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encGroupKey = enc.doFinal(key);
        GroupKey groupKey = new GroupKey(encGroupKey, id.getBytes());
        return groupKey;
    }

    @Override
    public void setUp() {
        try {
            logger.debug("initializing keystore");
            if (keystore == null) {
                keystore = KeyStore.getInstance("Standard", testProvider);
            }
            logger.debug("keystore initialized");
            keystore.load(null, null);
            logger.debug("keystore loaded");
            clearKeyStore();
            initKeyStore();
            dkm.keystore = keystore;
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    public void clearKeyStore() {
        try {
            Enumeration aliases = keystore.aliases();
            LinkedList<String> aliasStrings = new LinkedList<String>();
            while (aliases.hasMoreElements()) {
                aliasStrings.add((String) aliases.nextElement());
            }
            for (String a : aliasStrings) {
                keystore.deleteEntry(a);
            }
        } catch (KeyStoreException ex) {
            logger.debug(ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public void tearDown() {
        clearKeyStore();
    }
}

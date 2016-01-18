/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.pipeline;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
import java.util.Enumeration;
import java.util.LinkedList;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import junit.framework.TestCase;
import org.sec2.managers.exceptions.KeyManagerException;
import org.sec2.managers.factories.KeyManagerFactory;
import org.sec2.managers.impl.DocumentKeyManagerImpl;
import org.sec2.securityprovider.exceptions.IllegalPostInstantinationModificationException;
import org.sec2.securityprovider.mobileclient.MobileClientProvider;
import org.sec2.securityprovider.serviceparameter.PublicKeyType;
import org.sec2.securityprovider.serviceparameter.TokenType;
import org.sec2.token.TokenConstants;
import org.sec2.token.keys.DocumentKey;
import org.sec2.token.keys.GroupKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO move this class to the sec2-card package?
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.1
 */
public abstract class AbstractEncryptionDecryptionTest extends TestCase {

    static final String GROUP111_ID = "group111";
    static final String GROUP222_ID = "group222";
    static final String GROUP333_ID = "group333";
    
    /**
     * document key for the keystore
     */
    static final String STORE_D_KEY_111_222_ID = "d_group111;group222;nonce124";
    /**
     * document key for the keystore
     */
    static final String STORE_D_KEY_111_111_222_ID = "d_group111;group111;group222;nonce124";
    /**
     * document key for the keystore
     */
    static final String STORE_D_KEY_222_111_222_ID = "d_group222;group111;group222;nonce124";
    /**
     * document key 
     */
    static final String D_KEY_111_222_ID = "group111;group222";
    /**
     * document key 
     */
    static final String D_KEY_111_111_222_ID = "group111;group111;group222";
    /**
     * document key 
     */
    static final String D_KEY_222_111_222_ID = "group222;group111;group222";
    static final String NONCE = "nonce124";
    /**
     * Provider to test.
     */
    Provider testProvider = null;
    /**
     * Keystore Instance from testprovider
     */
    KeyStore keystore = null;
    DocumentKeyManagerImpl dkm;
    /**
     * logger
     */
    static Logger logger =
            LoggerFactory.getLogger(AbstractEncryptionDecryptionTest.class);
    /**
     * secure random
     */
    private final SecureRandom random = new SecureRandom();
    /**
     * public key
     */
    private PublicKey publicKey;

    /**
     * Test constructor with test provider initialization
     */
    public AbstractEncryptionDecryptionTest() throws Exception {
        try {
            MobileClientProvider.setType(TokenType.SOFTWARE_TOKEN);
        } catch (IllegalPostInstantinationModificationException ex) {
            logger.debug(ex.getLocalizedMessage());
        }
        this.testProvider = MobileClientProvider.getInstance(
                TokenConstants.DEFAULT_PIN);
        Security.insertProviderAt(testProvider, 1);
    }

    private void initKeyStore() throws KeyStoreException, IOException,
            NoSuchAlgorithmException, CertificateException,
            UnrecoverableKeyException, IllegalBlockSizeException,
            BadPaddingException, NoSuchPaddingException, InvalidKeyException, 
            NoSuchFieldException, IllegalAccessException, KeyManagerException {

        logger.debug("initializing keystore");
        if (keystore == null) {
            keystore = KeyStore.getInstance("Standard", testProvider);
        }
        logger.debug("keystore initialized");
        keystore.load(null, null);
        logger.debug("keystore loaded");
        publicKey = (PublicKey) keystore.getKey(
                PublicKeyType.CLIENT_ENCRYPTION.name(), null);
        this.deleteKeystoreEntries();
        this.initKeys();
    }

    private void initKeys() throws NoSuchAlgorithmException,
            IllegalBlockSizeException, BadPaddingException,
            NoSuchPaddingException, InvalidKeyException, KeyStoreException, 
            NoSuchFieldException, IllegalAccessException, KeyManagerException {
        logger.debug("Initializing the keystore with three group keys");
        GroupKey gKey1 = genGroupKey(GROUP111_ID);
        GroupKey gKey2 = genGroupKey(GROUP222_ID);
        GroupKey gKey3 = genGroupKey(GROUP333_ID);
        keystore.setKeyEntry(gKey1.getId().toString(), gKey1, null, null);
        keystore.setKeyEntry(gKey2.getId().toString(), gKey2, null, null);
        keystore.setKeyEntry(gKey3.getId().toString(), gKey3, null, null);

        logger.debug("Generating a new document key");
        DocumentKey dKey = genDocumentKey(STORE_D_KEY_111_222_ID);
        keystore.setKeyEntry(dKey.getKeyId(), dKey, null, null);
        
        logger.debug("Encrypting the document key with gk1");
        Cipher c = Cipher.getInstance("AES/CBC/NONE", testProvider);
        c.init(Cipher.WRAP_MODE, gKey1);
        byte[] wrapedKey = c.wrap(dKey);
        DocumentKey wrapMe = new DocumentKey(wrapedKey, true, STORE_D_KEY_111_111_222_ID);
        keystore.setKeyEntry(wrapMe.getKeyId(), wrapMe, null, null);

        logger.debug("Encrypting the document key with gk2");
        c.init(Cipher.WRAP_MODE, gKey2);
        wrapedKey = c.wrap(dKey);
        wrapMe = new DocumentKey(wrapedKey, true, STORE_D_KEY_222_111_222_ID);
        keystore.setKeyEntry(wrapMe.getKeyId(), wrapMe, null, null);
        
        initDocumentKeyManager();
        dkm.putKeyIdIntoList(D_KEY_111_222_ID + ";" + NONCE);
        dkm.putKeyIdIntoList(D_KEY_111_111_222_ID + ";" + NONCE);
        dkm.putKeyIdIntoList(D_KEY_222_111_222_ID + ";" + NONCE);
    }
    
     private void initDocumentKeyManager() throws NoSuchFieldException, 
             IllegalAccessException,
             KeyManagerException  {
        dkm = (DocumentKeyManagerImpl) KeyManagerFactory.
                getDocumentKeyManager();
        // reflection...not that nice, but whatever
        this.setPrivateField(dkm, "keystore", keystore);
        this.setPrivateField(dkm, "groupManager", new DummyGroupManager());
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
    
    private void deleteKeystoreEntries() {
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
    public void setUp() {
        try {
            initKeyStore();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Override
    public void tearDown() {
        deleteKeystoreEntries();
    }
    
    private void setPrivateField(Object c, String fieldName, Object fieldValue) throws IllegalArgumentException, 
            IllegalAccessException, NoSuchFieldException {
        for(Field f : c.getClass().getDeclaredFields()) {
                if(f.getName().equalsIgnoreCase(fieldName)) {
                f.setAccessible(true);

                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(f, f.getModifiers() & ~Modifier.PRIVATE);

                f.set(c, fieldValue);
            }
        }
    }
}

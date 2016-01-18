/*
 * Copyright 2011 Ruhr-University Bochum, Chair for Network and Data Security
 * 
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors visit
 * 
 *        http://nds.rub.de/research/projects/sec2/
 */
package org.sec2.managers.impl;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.sec2.extern.org.apache.commons.codec.binary.Base64;
import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.managers.IDocumentKeyManager;
import org.sec2.managers.IGroupManager;
import org.sec2.managers.ManagerProvider;
import org.sec2.managers.exceptions.KeyManagerException;
import org.sec2.securityprovider.mobileclient.MobileClientProvider;
import org.sec2.token.TokenConstants;
import org.sec2.token.keys.DocumentKey;
import org.sec2.token.keys.ExtendedDocumentKey;
import org.sec2.token.keys.GroupKey;
import org.sec2.token.keys.GroupKeyId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for managing Document Keys
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.1
 */
public class DocumentKeyManagerImpl implements IDocumentKeyManager {

    private static final String DOCUMENT_KEY_IDENTIFIER = "d_";
    private static final int NONCE_LENGTH = 10;
    /**
     * singleton instance
     */
    private static IDocumentKeyManager instance = null;
    /**
     * Group Manager instance
     */
    IGroupManager groupManager;
    /**
     * Secure random for generating new document keys
     */
    private static final SecureRandom random = new SecureRandom();
    /**
     * logger
     */
    private static Logger logger = LoggerFactory.getLogger(DocumentKeyManagerImpl.class);
    /**
     * There can exist more document keys for the same combination of groups.
     * These document keys are differentiated using nonces. This hashmap
     * includes a mapping from groups' combinations to document key ids.
     */
    HashMap<String, List<String>> documentKeyIds;
    /**
     * Mobile client's provider keystore
     */
    KeyStore keystore = null;

    /**
     * private constructor
     */
    private DocumentKeyManagerImpl() throws KeyManagerException {
        groupManager = ManagerProvider.getInstance().getGroupManager();
        documentKeyIds = new HashMap<String, List<String>>();
        try {
            // TODO was passiert wenn jemand (z.B. Dennis) das gleiche in seinem groupManager macht?
            keystore = KeyStore.getInstance("Standard", MobileClientProvider.getInstance());
            keystore.load(null, null);
        } catch (CertificateException ex) {
            logger.debug(ex.getLocalizedMessage(), ex);
            throw new KeyManagerException(ex);
        } catch (KeyStoreException ex) {
            logger.debug(ex.getLocalizedMessage(), ex);
            throw new KeyManagerException(ex);
        } catch (IOException ex) {
            logger.debug(ex.getLocalizedMessage(), ex);
            throw new KeyManagerException(ex);
        } catch (NoSuchAlgorithmException ex) {
            logger.debug(ex.getLocalizedMessage(), ex);
            throw new KeyManagerException(ex);
        }
    }
    
    /**
     * Returns the singleton instance
     */
    public static synchronized IDocumentKeyManager getInstance() throws KeyManagerException {
        if (instance == null) {
            instance = new DocumentKeyManagerImpl();
        }
        return instance;
    }

    /**
     * Decrypts the encrypted document key using the specified group key
     *
     * @param groupKey
     * @param algorithm
     * @param wrappedDocumentKey
     * @return decrypted DocumentKey
     * @throws KeyManagerException
     */
    private DocumentKey decryptDocumentKey(final String groupKey,
            final String algorithm,
            final DocumentKey wrappedDocumentKey) throws KeyManagerException {

        // check if the document key is encrypted
        if (!wrappedDocumentKey.isEncrypted()) {
            throw new KeyManagerException("Document key is not encrypted");
        }

        GroupKeyId gkId = new GroupKeyId(groupKey);

        // unwraps the document key
        try {
            Cipher c = Cipher.getInstance(algorithm);
            GroupKey gk = (GroupKey) keystore.getKey(gkId.toString(), null);
            c.init(Cipher.UNWRAP_MODE, gk);
            DocumentKey unwrapedKey = (DocumentKey) c.unwrap(
                    wrappedDocumentKey.getKey().getBytes(), "AES",
                    Cipher.SECRET_KEY);
            unwrapedKey.setKeyId(getKeyIdFromEncryptionGroupKeyId(
                    new String(wrappedDocumentKey.getKeyId().getBytes())));
            this.putKeyIdIntoList(new String(wrappedDocumentKey.getKeyId().getBytes()));
            return unwrapedKey;
        } catch (UnrecoverableKeyException e) {
            logger.debug(e.getLocalizedMessage(), e);
            throw new KeyManagerException("The group key " + new String(gkId.getBytes())
                    + " could not be recovered. Are you in the group?", e);
        } catch (NoSuchAlgorithmException e) {
            logger.debug(e.getLocalizedMessage(), e);
            throw new KeyManagerException(e);
        } catch (NoSuchPaddingException e) {
            logger.debug(e.getLocalizedMessage(), e);
            throw new KeyManagerException(e);
        } catch (InvalidKeyException e) {
            logger.debug(e.getLocalizedMessage(), e);
            throw new KeyManagerException(e);
        } catch (KeyStoreException e) {
            logger.debug(e.getLocalizedMessage(), e);
            throw new KeyManagerException(e);
        }
    }

    /**
     * Encrypts the document key using the specified group key
     *
     * @param groupKey
     * @param algorithm
     * @param documentKey
     * @return
     * @throws KeyManagerException
     */
    private DocumentKey encryptDocumentKey(String groupKey, String algorithm,
            DocumentKey unwrappedDocumentKey) throws KeyManagerException {
        // check if the document key is encrypted
        if (unwrappedDocumentKey.isEncrypted()) {
            throw new KeyManagerException("Document key is encrypted");
        }

        GroupKeyId gkId = new GroupKeyId(groupKey);

        // unwraps the document key
        try {
            Cipher c = Cipher.getInstance(algorithm);
            GroupKey gk = (GroupKey) keystore.getKey(gkId.toString(), null);
            c.init(Cipher.WRAP_MODE, gk);
            byte[] wrapped = c.wrap(unwrappedDocumentKey);
            DocumentKey wrappedKey = new DocumentKey(wrapped, true,
                    groupKey + ";" + unwrappedDocumentKey.getKeyId());
            return wrappedKey;
        } catch (UnrecoverableKeyException e) {
            logger.debug(e.getLocalizedMessage(), e);
            throw new KeyManagerException("The group key " + new String(gkId.getBytes())
                    + " could not be recovered. Are you in the group?", e);
        } catch (NoSuchAlgorithmException e) {
            logger.debug(e.getLocalizedMessage(), e);
            throw new KeyManagerException(e);
        } catch (NoSuchPaddingException e) {
            logger.debug(e.getLocalizedMessage(), e);
            throw new KeyManagerException(e);
        } catch (InvalidKeyException e) {
            logger.debug(e.getLocalizedMessage(), e);
            throw new KeyManagerException(e);
        } catch (IllegalBlockSizeException e) {
            logger.debug(e.getLocalizedMessage(), e);
            throw new KeyManagerException(e);
        } catch (KeyStoreException e) {
            logger.debug(e.getLocalizedMessage(), e);
            throw new KeyManagerException(e);
        }
    }

    /**
     * Use case: from the mobile to the cloud 1) search for a decrypted document
     * key, if not available:
     *
     * <ul> <li>check if you can get group keys for all the groups, if not -
     * throw an Exception! </li>
     *
     * <li>create new DocumentKey, store it in plain</li>
     *
     * <li>encrypt it with the group keys, store the keys </ul>
     *
     * 2) create ExtendedDocumentKey structure
     *
     * 3) return
     *
     * @param groups keyId consisting of groups
     * @return
     * @throws KeyManagerException
     */
    @Override
    public ExtendedDocumentKey getDecryptedDocumentKeyForGroups(String groups)
            throws KeyManagerException {
        try {
            ExtendedDocumentKey edk = new ExtendedDocumentKey();
            if (isDocumentKeyForGroupsInKeyStore(groups)) {
                DocumentKey decDK = this.getDocumentKeyForGroupsFromKeyStore(groups);
                edk.setDecryptedDocumentKey(decDK);

                // fetch the group key
                for (String gk : getGroupsFromKeyId(groups)) {
                    DocumentKey encDK = this.getDocumentKeyFromKeyStore(gk
                            + ExtendedDocumentKey.GROUP_SEPARATOR + decDK.getKeyId());
                    edk.addEncryptedDocumentKey(encDK);
                }
            } else {
                // first, try to fetch all the group keys
                String[] groupKeys = getGroupsFromKeyId(groups);
                for (String gk : groupKeys) {
                    groupManager.ensureGroupKeyIsAvailable(gk);
                }
                // create extended document key
                DocumentKey decDK = generateDocumentKey(groups);
                edk.setDecryptedDocumentKey(decDK);
                storeDocumentKeyIntoKeystore(decDK);
                this.putKeyIdIntoList(edk.getKeyIdWithNonce());

                // encrypt the document key with the group keys
                for (String gk : groupKeys) {
                    DocumentKey encDK = encryptDocumentKey(gk, edk.getAlgorithm(), decDK);
                    edk.addEncryptedDocumentKey(encDK);
                    storeDocumentKeyIntoKeystore(encDK);
                }
            }

            return edk;
        } catch (KeyStoreException ex) {
            logger.debug(ex.getLocalizedMessage(), ex);
            throw new KeyManagerException(ex);
        } catch (ExMiddlewareException ex) {
            logger.debug(ex.getLocalizedMessage(), ex);
            throw new KeyManagerException(ex);
        } catch (IOException ex) {
            logger.debug(ex.getLocalizedMessage(), ex);
            throw new KeyManagerException(ex);
        }
    }

    /**
     * Use case from the cloud to the mobile: The implementation gets a list of
     * encrypted document keys and returns a decrypted key
     *
     * 1) search for a decrypted document key, if not available:
     *
     * <ul>
     *
     * <li>iterate over encrypted document keys, try to decrypt, and store all
     * the encrypted and decrypted document keys </li>
     *
     * <li>if no key decrypted, throw an exception<li>
     *
     * </ul>
     *
     * 2) return plain document key
     *
     * @param encryptedDocumentKeys
     * @return
     * @throws KeyManagerException
     */
    @Override
    public ExtendedDocumentKey getDecryptedDocumentKey(List<DocumentKey> encryptedDocumentKeys) throws KeyManagerException {

        ExtendedDocumentKey edk;
        String keyIdWithNonce = getKeyIdFromEncryptionGroupKeyId(encryptedDocumentKeys.get(0).
                getKeyId());

        try {
            if (isDocumentKeyInKeyStore(keyIdWithNonce)) {
                DocumentKey decDK = this.getDocumentKeyFromKeyStore(keyIdWithNonce);
                edk = new ExtendedDocumentKey(decDK, encryptedDocumentKeys);
                return edk;
            } else {
                edk = new ExtendedDocumentKey(encryptedDocumentKeys);
                for (DocumentKey encDK : encryptedDocumentKeys) {
                    String gk = getEncryptionGroupKeyFromKeyId(encDK.getKeyId());
                    try {
                        // decrypt
                        groupManager.ensureGroupKeyIsAvailable(gk);
                        DocumentKey decDK = decryptDocumentKey(gk, edk.getAlgorithm(), encDK);
                        edk.setDecryptedDocumentKey(decDK);
                    } catch (KeyManagerException ex) {
                        logger.debug(ex.getLocalizedMessage());
                    }
                }

                // in case everything went ok store all the keys and return
                if (edk.getDecryptedDocumentKey() != null) {
                    storeDocumentKeyIntoKeystore(edk.getDecryptedDocumentKey());
                    for (DocumentKey encDK : encryptedDocumentKeys) {
                        storeDocumentKeyIntoKeystore(encDK);
                    }
                    return edk;
                } else {
                    throw new KeyManagerException("Insufficient rights to decrypt "
                            + "the document key.");
                }
            }
        } catch (KeyStoreException ex) {
            logger.debug(ex.getLocalizedMessage(), ex);
            throw new KeyManagerException(ex);
        } catch (ExMiddlewareException ex) {
            logger.debug(ex.getLocalizedMessage(), ex);
            throw new KeyManagerException(ex);
        } catch (IOException ex) {
            logger.debug(ex.getLocalizedMessage(), ex);
            throw new KeyManagerException(ex);
        }
    }

    /**
     * Generates new DocumentKey<br/>
     *
     * TODO: if there is a way to generate the key with the provider, please
     * feel free to change this function.
     *
     * @param groups
     * @return
     */
    private DocumentKey generateDocumentKey(String groups) {
        byte[] key = new byte[TokenConstants.DKEY_LEN];
        random.nextBytes(key);
        String keyIdWithNonce = groups + ExtendedDocumentKey.GROUP_SEPARATOR
                + generateNonce();
        return new DocumentKey(key, false, keyIdWithNonce);
    }

    /**
     * Splits the keyId name and returns an array of groups.
     *
     * @param keyId
     * @return
     */
    public static String[] getGroupsFromKeyId(String keyId) {
        return keyId.split(ExtendedDocumentKey.GROUP_SEPARATOR);
    }

    /**
     * Splits the keyId name and returns the first string -- the name of the
     * group key that encrypts the document key
     *
     * @param keyId
     * @return
     */
    public static String getEncryptionGroupKeyFromKeyId(String keyId) {
        String[] groups = getGroupsFromKeyId(keyId);
        return groups[0];
    }

    /**
     * removes the name of the first group key name
     *
     * @param keyId
     * @return
     * @throws KeyManagerException
     */
    public static String getKeyIdFromEncryptionGroupKeyId(String groupKeyId) {
        return groupKeyId.substring(groupKeyId.indexOf(';') + 1);
    }

    public static String getKeyIdWithoutNonce(String keyIdWithNonce) {
        int noncePosition = keyIdWithNonce.lastIndexOf(ExtendedDocumentKey.GROUP_SEPARATOR);
        return keyIdWithNonce.substring(0, noncePosition);
    }

    private void storeDocumentKeyIntoKeystore(DocumentKey key) throws KeyManagerException {
        try {
            keystore.setKeyEntry(DOCUMENT_KEY_IDENTIFIER
                    + new String(key.getKeyId().getBytes()), key, null, null);
        } catch (KeyStoreException ex) {
            logger.debug(ex.getLocalizedMessage(), ex);
            throw new KeyManagerException(ex + "By storing the key " + new String(key.getKeyId().getBytes()));
        }
    }

    private DocumentKey getDocumentKeyFromKeyStore(String keyIdWithNonce)
            throws KeyManagerException {
        try {
            DocumentKey dk = (DocumentKey) keystore.getKey(
                    DOCUMENT_KEY_IDENTIFIER + keyIdWithNonce, null);
            return new DocumentKey(dk.getKey().getBytes(), dk.isEncrypted(),
                    dk.getKeyId().substring(DOCUMENT_KEY_IDENTIFIER.length()));
        } catch (NoSuchAlgorithmException ex) {
            logger.debug(ex.getLocalizedMessage(), ex);
            throw new KeyManagerException(ex.getLocalizedMessage() + " Problem by "
                    + "processing the key: " + keyIdWithNonce, ex);
        } catch (UnrecoverableKeyException ex) {
            logger.debug(ex.getLocalizedMessage(), ex);
            throw new KeyManagerException(ex.getLocalizedMessage() + " Problem by "
                    + "processing the key: " + keyIdWithNonce, ex);
        } catch (KeyStoreException ex) {
            logger.debug(ex.getLocalizedMessage(), ex);
            throw new KeyManagerException(ex.getLocalizedMessage() + " Problem by "
                    + "processing the key: " + keyIdWithNonce, ex);
        }
    }

    private DocumentKey getDocumentKeyForGroupsFromKeyStore(String groups)
            throws KeyManagerException {
        String keyIdWithNonce = documentKeyIds.get(groups).get(0);
        return this.getDocumentKeyFromKeyStore(keyIdWithNonce);
    }

    private boolean isDocumentKeyInKeyStore(String keyIdWithNonce)
            throws KeyStoreException {
        return keystore.isKeyEntry(DOCUMENT_KEY_IDENTIFIER + keyIdWithNonce);
    }

    private boolean isDocumentKeyForGroupsInKeyStore(String groups)
            throws KeyStoreException {
        if (documentKeyIds.containsKey(groups)) {
            String keyIdWithNonce = documentKeyIds.get(groups).get(0);
            return isDocumentKeyInKeyStore(keyIdWithNonce);
        }
        return false;
    }

    /**
     * removes the nonce and puts the key into the map: groups -> keyIdWithNonce
     *
     * Later, we then know that the document key for the groups is already
     * created
     *
     * @param keyIdWithNonce
     */
    public void putKeyIdIntoList(String keyIdWithNonce) {
        String groups = getKeyIdWithoutNonce(keyIdWithNonce);
        if (!documentKeyIds.containsKey(groups)) {
            documentKeyIds.put(groups, new LinkedList<String>());
        }
        List<String> list = documentKeyIds.get(groups);
        list.add(keyIdWithNonce);
    }

    private String generateNonce() {
        byte[] nonce = new byte[NONCE_LENGTH];
        random.nextBytes(nonce);
        return Base64.encodeBase64String(nonce);
    }
}

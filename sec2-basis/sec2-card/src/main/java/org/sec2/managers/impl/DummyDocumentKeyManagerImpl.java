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

import java.util.List;
import org.sec2.managers.IDocumentKeyManager;
import org.sec2.managers.IGroupManager;
import org.sec2.managers.ManagerProvider;
import org.sec2.managers.exceptions.KeyManagerException;
import org.sec2.token.TokenConstants;
import org.sec2.token.keys.DocumentKey;
import org.sec2.token.keys.ExtendedDocumentKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for managing Document Keys
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.1
 */
public class DummyDocumentKeyManagerImpl implements IDocumentKeyManager {

    private static final int NONCE_LENGTH = 10;
    
    private static final String GROUP = "test";
    /**
     * singleton instance
     */
    private static IDocumentKeyManager instance = null;
    /**
     * Group Manager instance
     */
    IGroupManager groupManager;
    /**
     * logger
     */
    private static Logger logger = LoggerFactory.getLogger(DummyDocumentKeyManagerImpl.class);

    /**
     * private constructor
     */
    private DummyDocumentKeyManagerImpl() throws KeyManagerException {
        groupManager = ManagerProvider.getInstance().getGroupManager();
    }

    /**
     * Returns the singleton instance
     */
    public static synchronized IDocumentKeyManager getInstance() throws KeyManagerException {
        if (instance == null) {
            instance = new DummyDocumentKeyManagerImpl();
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

        return generateDecryptedDocumentKey(GROUP);
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

        return generateEncryptedDocumentKey(GROUP);
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
        ExtendedDocumentKey edk = new ExtendedDocumentKey();
        DocumentKey decDK = generateDecryptedDocumentKey(GROUP);
        edk.setDecryptedDocumentKey(decDK);
        
        edk.addEncryptedDocumentKey(generateEncryptedDocumentKey(GROUP));

        // fetch the group key
//                for (String gk : getGroupsFromKeyId(groups)) {
//                    DocumentKey encDK = this.getDocumentKeyFromKeyStore(gk
//                            + ExtendedDocumentKey.GROUP_SEPARATOR + decDK.getKeyId());
//                    edk.addEncryptedDocumentKey(encDK);
//                }
        return edk;
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

        ExtendedDocumentKey edk = new ExtendedDocumentKey();
        DocumentKey decDK = generateDecryptedDocumentKey(GROUP);
        edk.setDecryptedDocumentKey(decDK);
        
        edk.addEncryptedDocumentKey(generateEncryptedDocumentKey(GROUP));
        
        return edk;
    }

    /**
      * @param groups
     * @return
     */
    private DocumentKey generateDecryptedDocumentKey(String groups) {
        byte[] key = new byte[TokenConstants.DKEY_LEN];
        for (int i = 0; i < groups.length(); i++) {
            if (i < key.length) {
                key[i] = groups.getBytes()[i];
            }
        }
        String keyIdWithNonce = groups;
        return new DocumentKey(key, false, keyIdWithNonce);
    }
    
    /**
      * @param groups
     * @return
     */
    private DocumentKey generateEncryptedDocumentKey(String groups) {
        byte[] key = new byte[TokenConstants.DKEY_LEN];
        for (int i = 0; i < groups.length(); i++) {
            if (i < key.length) {
                key[i] = groups.getBytes()[i];
            }
        }
        String keyIdWithNonce = groups;
        return new DocumentKey(key, true, keyIdWithNonce);
    }    
}

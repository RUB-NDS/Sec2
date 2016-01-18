/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.token.keys;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.1
 */
public class ExtendedDocumentKey {
        private static final long serialVersionUID = 1L;

    public static final String GROUP_SEPARATOR = ";";
    /**
     * Algorithm used to encrypt / decrypt this document key.
     * 
     * This variable is not supported now, the default algorithm is always used.
     */
    private static final String ALGORITHM = "AES/CBC/NONE";
    /**
     * decrypted document key
     */
    private DocumentKey decryptedDocumentKey;
    /**
     * List of encrypted document keys, which are decrypted to the same
     * DocumentKey. Each document key in this list is encrypted with a different
     * group key.
     *
     */
    private List<DocumentKey> encryptedDocumentKeys;

    
    public ExtendedDocumentKey() {
        encryptedDocumentKeys = new LinkedList<DocumentKey>();
    }
    
    public ExtendedDocumentKey(List<DocumentKey> encryptedDocumentKeys) {
        this.encryptedDocumentKeys = encryptedDocumentKeys;
    }

    public ExtendedDocumentKey(DocumentKey decryptedDocumentKey,
            List<DocumentKey> encryptedDocumentKeys) {
        this.encryptedDocumentKeys = encryptedDocumentKeys;
        this.decryptedDocumentKey = decryptedDocumentKey;
    }

    public String getKeyIdWithNonce() {
        return decryptedDocumentKey.getKeyId();
    }
    
    public String getGroups() {
        String keyId = decryptedDocumentKey.getKeyId();
        int noncePosition = keyId.lastIndexOf(GROUP_SEPARATOR);
        return keyId.substring(0, noncePosition);
    }
    
    public String getAlgorithm() {
        return ALGORITHM;
    }

    public DocumentKey getDecryptedDocumentKey() {
        return decryptedDocumentKey;
    }

    public void setDecryptedDocumentKey(DocumentKey decryptedDocumentKey) {
        this.decryptedDocumentKey = decryptedDocumentKey;
    }

    public List<DocumentKey> getEncryptedDocumentKeys() {
        return encryptedDocumentKeys;
    }

    public void setEncryptedDocumentKeys(List<DocumentKey> encryptedDocumentKeys) {
        this.encryptedDocumentKeys = encryptedDocumentKeys;
    }
    
    public void addEncryptedDocumentKey(DocumentKey edk) {
        this.encryptedDocumentKeys.add(edk);
    }
}

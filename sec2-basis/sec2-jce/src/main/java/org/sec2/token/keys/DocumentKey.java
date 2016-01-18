package org.sec2.token.keys;

/**
 *
 * @author benedikt
 */
public class DocumentKey extends ASecretKey {

    public DocumentKey(byte[] keyData, boolean isEncrypted) {
        super(keyData, isEncrypted);
    }
}

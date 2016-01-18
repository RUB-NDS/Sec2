package org.sec2.token.keys;

/**
 *
 * @author benedikt
 */
public class DocumentKey extends ASecretKey {
            private static final long serialVersionUID = 1L;

    /** key id consisting of strings of group names and (if encrypted) the 
     * group key used for decryption. Examples:
     * <ul>
     * <li>Decrypted key for groups g1 and g2: g1;g2</li>
     * <li>Encrypted key for groups g1 and g2, encrypted with the group key g1:
     * g1;g1;g2</li>
     * </ul>
     */
    private String keyId;
    
//    /** group key id this document key should be encrypted */
//    private GroupKeyId groupKeyId;
    
    public DocumentKey(byte[] keyData, boolean isEncrypted) {
        super(keyData, isEncrypted);
    }

    public DocumentKey(byte[] keyData, boolean isEncrypted, String kId) {
        super(keyData, isEncrypted);
        this.keyId = kId;
    }
    
    public String getKeyId() {
        return keyId;
    }
    
    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }
}

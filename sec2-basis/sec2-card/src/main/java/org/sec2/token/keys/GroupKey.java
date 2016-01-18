package org.sec2.token.keys;

/**
 *
 * @author benedikt
 */
public class GroupKey extends ASecretKey {

    private final GroupKeyId keyId;
        private static final long serialVersionUID = 1L;

    public GroupKey(byte[] keyData, GroupKeyId keyId) {
        super(keyData, true);
        this.keyId = new GroupKeyId(keyId);
    }

    public GroupKey(byte[] keyData, byte[] keyId) {
        super(keyData, true);
        this.keyId = new GroupKeyId(keyId);
    }

    public GroupKeyId getId() {
        return keyId;
    }
    
}

package org.sec2.token.keys;

import java.math.BigInteger;

/**
 *
 * @author benedikt
 */
public class GroupKeyId extends ByteArray {
        private static final long serialVersionUID = 1L;

    public GroupKeyId(byte[] keyId) {
        super(keyId);
    }

    public GroupKeyId(String keyId) {
        super(keyId);
    }

    public GroupKeyId(BigInteger keyId) {
        super(keyId);
    }
    
    public GroupKeyId(ByteArray keyId) {
        super(keyId);
    }
}

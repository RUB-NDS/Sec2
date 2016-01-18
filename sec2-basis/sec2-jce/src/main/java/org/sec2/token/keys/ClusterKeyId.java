package org.sec2.token.keys;

import java.math.BigInteger;

/**
 *
 * @author benedikt
 */
public class ClusterKeyId extends ByteArray {

    public ClusterKeyId(byte[] keyId) {
        super(keyId);
    }

    public ClusterKeyId(String keyId) {
        super(keyId);
    }

    public ClusterKeyId(BigInteger keyId) {
        super(keyId);
    }
    
    public ClusterKeyId(ByteArray keyId) {
        super(keyId);
    }
}

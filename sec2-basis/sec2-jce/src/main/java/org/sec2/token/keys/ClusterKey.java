package org.sec2.token.keys;

import java.math.BigInteger;

/**
 *
 * @author benedikt
 */
public class ClusterKey extends ASecretKey {

    private final ClusterKeyId keyId;

    public ClusterKey(byte[] keyData, ClusterKeyId keyId) {
        super(keyData, true);
        this.keyId = new ClusterKeyId(keyId);
    }

    public ClusterKey(byte[] keyData, byte[] keyId) {
        super(keyData, true);
        this.keyId = new ClusterKeyId(keyId);
    }

    public ClusterKeyId getId() {
        return keyId;
    }
}

package org.sec2.token.keys;

import java.io.Serializable;

import javax.crypto.SecretKey;
import org.sec2.securityprovider.serviceparameter.CipherAlgorithm;

/**
 *
 * @author benedikt
 */
abstract class ASecretKey implements SecretKey, Serializable {

    private static final long serialVersionUID = 1L;

    public class KeyData extends ByteArray {

        private static final long serialVersionUID = 1L;

        public KeyData(byte[] data) {
            super(data);
        }
    }
    private final boolean isEncrypted;
    private final KeyData key;
    private static final String ALGORITHM_NAME = CipherAlgorithm.AES.name();
    private static final String KEY_FORMAT = "RAW";

    public ASecretKey(byte[] keyData, boolean isEncrypted) {
        this.key = new KeyData(keyData);
        this.isEncrypted = isEncrypted;
    }

    public KeyData getKey() {
        return key;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    @Override
    public String getAlgorithm() {
        return ALGORITHM_NAME;
    }

    @Override
    public String getFormat() {
        return KEY_FORMAT;
    }

    @Override
    public byte[] getEncoded() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

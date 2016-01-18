package org.sec2.token.swtoken;

import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.sec2.token.*;
import org.sec2.token.exceptions.TokenException;
import org.sec2.token.keys.ClusterKey;
import org.sec2.token.keys.ClusterKeyId;

public class ClusterKeyStore implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String CKEY_ALG = "AES/CBC/NoPadding";
    private ClusterKey[] clusterKeys;
    private int numKeys;

    public ClusterKeyStore(int numKeys) {
        this.clusterKeys = new ClusterKey[numKeys];
        this.numKeys = numKeys;
    }

    public void removeClusterKey(int pos) throws TokenException {
        ensureSlotFilled(pos);
        clusterKeys[pos] = null;
    }

    private void ensureSlotFilled(int pos) throws TokenException {
        if (pos < 0 || pos > this.numKeys) {
            throw new TokenException(ReturnCodes.SW_CKEY_SLOT_INVALID);
        }
        if (clusterKeys[pos] == null) {
            throw new TokenException(ReturnCodes.SW_CKEY_SLOT_EMPTY);
        }
    }

    public int getClusterKeyPosition(ClusterKeyId clusterKeyId) throws TokenException {
        if (clusterKeyId.getBytes().length != TokenConstants.CKEY_ID_LEN) {
            throw new TokenException(ReturnCodes.SW_CKEY_WRONG_ID_SIZE);
        }

        for (int i = 0; i < numKeys; i++) {
            if (this.clusterKeys[i] != null && Arrays.equals(this.clusterKeys[i].getId().getBytes(), clusterKeyId.getBytes())) {
                return i;
            }
        }
        return (TokenConstants.POS_NOT_FOUND);
    }

    public int getClusterKeyFreePosition() {
        for (int i = 0; i < this.numKeys; i++) {
            if (this.clusterKeys[i] == null) {
                return i;
            }
        }
        return TokenConstants.POS_NOT_FOUND;
    }

    public void addClusterKey(int pos, ClusterKey clusterKey) throws TokenException {
        ensureSlotEmpty(pos);
        this.clusterKeys[pos] = clusterKey;
    }

    private void ensureSlotEmpty(int pos) throws TokenException {
        if (pos < 0 || pos > this.numKeys) {
            throw new TokenException(ReturnCodes.SW_CKEY_SLOT_INVALID);
        }
        if (this.clusterKeys[pos] != null) {
            throw new TokenException(ReturnCodes.SW_CKEY_SLOT_IN_USE);
        }
    }

    public void clearAllClusterKeys() {
        for (int i = 0; i < this.numKeys; i++) {
            this.clusterKeys[i] = null;
        }
    }

    public ClusterKeyId[] getAllClusterKeys() {
        ClusterKeyId[] clusterKeyIds = new ClusterKeyId[getCountClusterKeys()];
        short currentPos = 0;
        for (int i = 0; i < this.numKeys; i++) {
            if (this.clusterKeys[i] != null) {
                clusterKeyIds[currentPos] = this.clusterKeys[i].getId();
                currentPos++;
            }
        }
        return clusterKeyIds;
    }

    private int getCountClusterKeys() {
        int count = 0;
        for (int i = 0; i < this.numKeys; i++) {
            if (this.clusterKeys[i] != null) {
                count++;
            }
        }
        return count;
    }

    public byte[] encrypt(int pos, byte[] IV, byte[] data) throws TokenException {
        return doCipherOperation(pos, IV, data, Cipher.ENCRYPT_MODE);
    }

    public byte[] decrypt(int pos, byte[] IV, byte[] data) throws TokenException {
        return doCipherOperation(pos, IV, data, Cipher.DECRYPT_MODE);
    }

    private byte[] doCipherOperation(int pos, byte[] IV, byte[] data, int mode) throws TokenException {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(this.clusterKeys[pos].getKey().getBytes(), "AES");
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(IV);
            Cipher cipher = Cipher.getInstance(CKEY_ALG);
            cipher.init(mode, skeySpec, paramSpec);
            byte[] encryptedData = cipher.doFinal(data);
            return encryptedData;
        } catch (GeneralSecurityException e) {
            throw new TokenException(ReturnCodes.SW_GENERAL_CRYPTO_ERROR + e.getMessage());
        } catch (Exception e) {
            throw new TokenException(e.getMessage());
        }
    }
}

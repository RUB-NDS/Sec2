package org.sec2.token.swtoken;

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.sec2.token.*;
import org.sec2.token.exceptions.TokenException;
import org.sec2.token.keys.GroupKey;
import org.sec2.token.keys.GroupKeyId;
import org.spongycastle.jce.provider.BouncyCastleProvider;

public class GroupKeyStore implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String GKEY_ALG = "AES/CBC/NoPadding";
    private GroupKey[] groupKeys;
    private int numKeys;

    public GroupKeyStore(int numKeys) {
        this.groupKeys = new GroupKey[numKeys];
        this.numKeys = numKeys;
    }

    public void removeGroupKey(int pos) throws TokenException {
        ensureSlotFilled(pos);
        groupKeys[pos] = null;
    }

    private void ensureSlotFilled(int pos) throws TokenException {
        if (pos < 0 || pos > this.numKeys) {
            throw new TokenException(ReturnCodes.SW_GKEY_SLOT_INVALID);
        }
        if (groupKeys[pos] == null) {
            throw new TokenException(ReturnCodes.SW_GKEY_SLOT_EMPTY);
        }
    }

    public int getGroupKeyPosition(GroupKeyId groupKeyId) throws TokenException {
        if (groupKeyId.getBytes().length != TokenConstants.GKEY_ID_LEN) {
            throw new TokenException(ReturnCodes.SW_GKEY_WRONG_ID_SIZE);
        }

        for (int i = 0; i < numKeys; i++) {
            if (this.groupKeys[i] != null && Arrays.equals(this.groupKeys[i].getId().getBytes(), groupKeyId.getBytes())) {
                return i;
            }
        }
        return (TokenConstants.POS_NOT_FOUND);
    }

    public int getGroupKeyFreePosition() {
        for (int i = 0; i < this.numKeys; i++) {
            if (this.groupKeys[i] == null) {
                return i;
            }
        }
        return TokenConstants.POS_NOT_FOUND;
    }

    public void addGroupKey(int pos, GroupKey groupKey) throws TokenException {
        ensureSlotEmpty(pos);
        this.groupKeys[pos] = groupKey;
    }

    private void ensureSlotEmpty(int pos) throws TokenException {
        if (pos < 0 || pos > this.numKeys) {
            throw new TokenException(ReturnCodes.SW_GKEY_SLOT_INVALID);
        }
        if (this.groupKeys[pos] != null) {
            throw new TokenException(ReturnCodes.SW_GKEY_SLOT_IN_USE);
        }
    }

    public void clearAllGroupKeys() {
        for (int i = 0; i < this.numKeys; i++) {
            this.groupKeys[i] = null;
        }
    }

    public GroupKeyId[] getAllGroupKeys() {
        GroupKeyId[] groupKeyIds = new GroupKeyId[getCountGroupKeys()];
        short currentPos = 0;
        for (int i = 0; i < this.numKeys; i++) {
            if (this.groupKeys[i] != null) {
                groupKeyIds[currentPos] = this.groupKeys[i].getId();
                currentPos++;
            }
        }
        return groupKeyIds;
    }

    private int getCountGroupKeys() {
        int count = 0;
        for (int i = 0; i < this.numKeys; i++) {
            if (this.groupKeys[i] != null) {
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
            SecretKeySpec skeySpec = new SecretKeySpec(this.groupKeys[pos].getKey().getBytes(), "AES");
            
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(IV);
            Cipher cipher = Cipher.getInstance(GKEY_ALG, new BouncyCastleProvider());
            cipher.init(mode, skeySpec, paramSpec);
            
            byte[] encryptedData = cipher.doFinal(data);
            return encryptedData;
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            throw new TokenException(ReturnCodes.SW_GENERAL_CRYPTO_ERROR + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new TokenException(e.getMessage());
        }
    }
}

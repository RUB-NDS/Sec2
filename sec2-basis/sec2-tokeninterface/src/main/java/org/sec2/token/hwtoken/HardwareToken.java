package org.sec2.token.hwtoken;

import org.sec2.token.keys.ServerKey;
import org.sec2.token.keys.UserKey;
import org.sec2.token.keys.ClusterKey;
import org.sec2.token.keys.DocumentKey;
import org.sec2.token.keys.ClusterKeyId;
import org.sec2.token.ReturnCodes;
import org.sec2.token.IToken;
import org.sec2.token.exceptions.TokenException;
import javax.smartcardio.*;
import java.util.Arrays;
import java.util.List;
import org.sec2.token.TokenConstants;

/**
 *
 * @author benedikt
 */
public class HardwareToken implements IToken {

    private ConnectionHandler connHandler;
    static final byte[] DEFAULT_IV = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    public HardwareToken() {
        connHandler = new ConnectionHandler();
    }

    @Override
    public void loginPIN(byte[] pin) throws TokenException {
        connHandler.sendData(InstructionCodes.INS_PIN_VALIDATE, pin);
    }

    @Override
    public void setPIN(byte[] pin) throws TokenException {
        connHandler.sendData(InstructionCodes.INS_PIN_SET, pin);
    }

    @Override
    public void setPUK(byte[] puk) throws TokenException {
        connHandler.sendData(InstructionCodes.INS_PUK_SET, puk);
    }

    @Override
    public void loginPUK(byte[] puk) throws TokenException {
        connHandler.sendData(InstructionCodes.INS_PUK_VALIDATE, puk);
    }

    @Override
    public void importClusterKey(ClusterKey key) throws TokenException {
        byte pos;

        /*
         * Test whether clusterKey with given identifier is already on myCard.
         */
        pos = getClusterKeyPos(key.getId().getBytes());
        if (pos != TokenConstants.POS_NOT_FOUND) {
            throw new TokenException(
                    "Could not add clusterKey, identifier is in use.");
        }

        pos = getFreeClusterKeyPos();
        if (pos == TokenConstants.POS_NOT_FOUND) {
            throw new TokenException(
                    "Could not adcd clusterKey, all slots are occupied.");
        }

        /*
         * Add clusterKey to slot and set identifier
         */
        connHandler.sendData(InstructionCodes.INS_CKEY_SET_KEY, pos, key.getKey().getBytes());
        connHandler.sendData(InstructionCodes.INS_CKEY_SET_ID, pos, key.getId().getBytes());
    }

    @Override
    public void removeClusterKey(ClusterKeyId keyId) throws TokenException {
        if (keyId.getBytes().length != TokenConstants.CKEY_ID_LEN) {
            throw new TokenException("KeyId of cluster-key is invalid");
        }

        connHandler.sendData(InstructionCodes.INS_CKEY_REMOVE, getClusterKeyPos(keyId.getBytes()));
    }

    @Override
    public boolean isClusterKeyAvailable(ClusterKeyId keyId)
            throws TokenException {
        if (keyId.getBytes().length != TokenConstants.CKEY_ID_LEN) {
            throw new TokenException("KeyId of cluster-key is invalid");
        }

        return (getClusterKeyPos(keyId.getBytes()) != TokenConstants.POS_NOT_FOUND);
    }

    private byte getFreeClusterKeyPos() throws TokenException {
        byte[] retArr = connHandler.requestData(InstructionCodes.INS_CKEY_GET_FREE_SLOT);
        return retArr[0];
    }

    private byte getClusterKeyPos(byte[] keyIdentifier) throws TokenException {
        if (keyIdentifier.length != TokenConstants.CKEY_ID_LEN) {
            throw new TokenException("KeyId of cluster-key is invalid");
        }

        byte[] retArr = connHandler.requestData(InstructionCodes.INS_CKEY_FIND, keyIdentifier);
        return retArr[0];
    }

    @Override
    public void setServerKey(ServerKey serverKey) throws TokenException {
        if (serverKey.getExponent().getBytes().length == 0
                || serverKey.getExponent().getBytes().length == 0) {
            throw new TokenException("Exponent or modulus of serverKey empty");
        }

        connHandler.sendData(InstructionCodes.INS_SKEY_SET_MOD, serverKey.getModulus().getBytes());
        connHandler.sendData(InstructionCodes.INS_SKEY_SET_EXP, serverKey.getExponent().getBytes());
    }

    @Override
    public ServerKey getServerKey() throws TokenException {
        byte[] modulus = connHandler.requestData(InstructionCodes.INS_SKEY_GET_MOD);
        byte[] exponent = connHandler.requestData(InstructionCodes.INS_SKEY_GET_EXP);

        return new ServerKey(modulus, exponent);
    }

    @Override
    public DocumentKey createDocumentKey() throws TokenException {
        return new DocumentKey(connHandler.requestData(
                InstructionCodes.INS_MISC_RND_GET, TokenConstants.DKEY_LEN), false);
    }

    @Override
    public byte[] sign(byte[] in) throws TokenException {
        if (!((in.length == TokenConstants.SHA1_HASH_LEN)
                || (in.length == TokenConstants.SHA256_HASH_LEN)
                || (in.length == TokenConstants.MD5_HASH_LEN))) {
            throw new TokenException("Hash data has invalid length");
        }

        return connHandler.requestData(InstructionCodes.INS_UKEYS_SIG_SIGN, in);
    }

    @Override
    public DocumentKey encryptDocumentKey(ClusterKeyId keyId,
            DocumentKey documentKey) throws TokenException {
        return encryptDocumentKey(keyId, documentKey, DEFAULT_IV);
    }

    @Override
    public DocumentKey encryptDocumentKey(ClusterKeyId keyId,
            DocumentKey documentKey, byte[] IV) throws TokenException {
        if (IV.length != TokenConstants.CBC_BLOCK_LEN) {
            throw new TokenException("IV has invalid length");
        }
        if (documentKey.isEncrypted()) {
            throw new TokenException("DocumentKey is already encrypted");
        }

        byte pos = getClusterKeyPos(keyId.getBytes());
        if (pos == TokenConstants.POS_NOT_FOUND) {
            throw new TokenException(
                    "Cannot encrypt document key, clusterKey not found");
        }

        /*
         * Construct new buffer containing IV|Payload.
         */
        byte[] buffer = new byte[documentKey.getKey().getBytes().length
                + TokenConstants.CBC_BLOCK_LEN];
        System.arraycopy(IV, 0, buffer, 0, TokenConstants.CBC_BLOCK_LEN);
        System.arraycopy(documentKey.getKey().getBytes(), 0, buffer,
                TokenConstants.CBC_BLOCK_LEN, documentKey.getKey().getBytes().length);

        return new DocumentKey(connHandler.requestData(
                InstructionCodes.INS_CKEY_ENCRYPT, pos, buffer), true);
    }

    @Override
    public DocumentKey decryptDocumentKey(ClusterKeyId keyId,
            DocumentKey documentKey) throws TokenException {
        return decryptDocumentKey(keyId, documentKey, DEFAULT_IV);
    }

    @Override
    public DocumentKey decryptDocumentKey(ClusterKeyId keyId,
            DocumentKey documentKey, byte[] IV) throws TokenException {
        if (IV.length != TokenConstants.CBC_BLOCK_LEN) {
            throw new TokenException("IV has invalid length");
        }
        if (!documentKey.isEncrypted()) {
            throw new TokenException("DocumentKey is not encrypted");
        }
        byte pos = getClusterKeyPos(keyId.getBytes());
        if (pos == TokenConstants.POS_NOT_FOUND) {
            throw new TokenException(
                    "Cannot decrypt document key, clusterKey not found");
        }

        /*
         * Construct new buffer containing IV|Payload.
         */
        byte[] buffer = new byte[documentKey.getKey().getBytes().length
                + TokenConstants.CBC_BLOCK_LEN];
        System.arraycopy(IV, 0, buffer, 0, TokenConstants.CBC_BLOCK_LEN);
        System.arraycopy(documentKey.getKey().getBytes(), 0, buffer,
                TokenConstants.CBC_BLOCK_LEN, documentKey.getKey().getBytes().length);

        return new DocumentKey(connHandler.requestData(
                InstructionCodes.INS_CKEY_DECRYPT, pos, buffer), false);
    }

    @Override
    public ClusterKeyId[] getAvailableClusterKeys() throws TokenException {
        short i, idCount;

        byte[] ids = connHandler.requestData(InstructionCodes.INS_CKEY_GET_IDS);
        idCount = (short) (ids.length / TokenConstants.CKEY_ID_LEN);

        ClusterKeyId[] idList = new ClusterKeyId[idCount];
        for (i = 0; i < idCount; i++) {
            byte[] id = Arrays.copyOfRange(ids, i * TokenConstants.CKEY_ID_LEN,
                    (i + 1) * TokenConstants.CKEY_ID_LEN);
            idList[i] = new ClusterKeyId(id);
        }

        return idList;
    }

    @Override
    public void clearClusterKeys() throws TokenException {
        connHandler.sendData(InstructionCodes.INS_CKEY_CLEAR_ALL);
    }

    @Override
    public String getVersion() throws TokenException {
        return new String(connHandler.requestData(InstructionCodes.INS_MISC_VERSION_GET));
    }

    @Override
    public TokenStatus getStatus() throws TokenException {
        byte[] statusFields = connHandler.requestData(InstructionCodes.INS_MISC_STATUS_GET);

        return new TokenStatus(statusFields[0] != 0, statusFields[1] != 0,
                statusFields[2] != 0, statusFields[3] != 0);
    }

    @Override
    public void logoutPIN() throws TokenException {
        connHandler.sendData(InstructionCodes.INS_PIN_LOGOUT);
    }

    @Override
    public void logoutPUK() throws TokenException {
        connHandler.sendData(InstructionCodes.INS_PUK_LOGOUT);
    }

    @Override
    public void connect() throws TokenException {
        connHandler.connect();
    }

    @Override
    public void disconnect() throws TokenException {
        connHandler.disconnect();
    }

    @Override
    public void generateUserKeys() throws TokenException {
        connHandler.sendData(InstructionCodes.INS_UKEYS_GENERATE);
    }

    @Override
    public UserKey getUserKeySig() throws TokenException {
        byte[] modulus = connHandler.requestData(InstructionCodes.INS_UKEYS_SIG_GET_MOD);
        byte[] exponent = connHandler.requestData(InstructionCodes.INS_UKEYS_SIG_GET_EXP);

        return new UserKey(modulus, exponent);
    }

    @Override
    public UserKey getUserKeyEnc() throws TokenException {
        byte[] modulus = connHandler.requestData(InstructionCodes.INS_UKEYS_ENC_GET_MOD);
        byte[] exponent = connHandler.requestData(InstructionCodes.INS_UKEYS_ENC_GET_EXP);

        return new UserKey(modulus, exponent);
    }
}

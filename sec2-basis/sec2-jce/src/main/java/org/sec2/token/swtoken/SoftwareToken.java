package org.sec2.token.swtoken;

import java.util.Arrays;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.SecureRandom;
import org.sec2.token.IToken;
import org.sec2.token.ReturnCodes;
import org.sec2.token.TokenConstants;
import org.sec2.token.exceptions.TokenException;
import org.sec2.token.keys.ClusterKey;
import org.sec2.token.keys.ClusterKeyId;
import org.sec2.token.keys.DocumentKey;
import org.sec2.token.keys.ServerKey;
import org.sec2.token.keys.UserKey;

public class SoftwareToken implements IToken {

    private static final byte[] DEFAULT_IV = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private static final byte[] VERSION_STRING = {'1', '.', '2'};
    private static final String FILENAME = "crypto-data";
    private static final int PIN_TRY_LIMIT = 3;
    private static final int PUK_TRY_LIMIT = 3;
    private static final int KEY_SIZE = 2048;
    private byte[] pin, puk;
    private short pinTrysLeft, pukTrysLeft;
    private boolean pinLoggedIn = false;
    private boolean pukLoggedIn = false;
    private boolean connected = false;
    private boolean pukIsSet = false;
    private boolean pinIsSet = false;
    private UserKeySigPair userKeySig;
    private UserKeyEncPair userKeyEnc;
    private ServerKey serverKey;
    private SecureRandom random;
    private ClusterKeyStore clusterKeys;

    public SoftwareToken() {
        this.random = new SecureRandom();
        this.pin = TokenConstants.DEFAULT_PIN;
        this.puk = TokenConstants.DEFAULT_PUK;
        this.pinTrysLeft = PIN_TRY_LIMIT;
        this.pukTrysLeft = PUK_TRY_LIMIT;
        this.userKeySig = new UserKeySigPair();
        this.userKeyEnc = new UserKeyEncPair();
        this.clusterKeys = new ClusterKeyStore(16);
    }

    private void checkConnected() throws TokenException {
        if (!this.connected) {
            throw new TokenException("Not Connected!");
        }
    }

    private void checkPinLoggedIn() throws TokenException {
        checkConnected();
        if (!this.pinLoggedIn) {
            throw new TokenException(ReturnCodes.SW_PIN_PUK_AUTHENTICATION_REQUIRED);
        }
    }

    private void checkPukLoggedIn() throws TokenException {
        checkConnected();
        if (!this.pukLoggedIn) {
            throw new TokenException(ReturnCodes.SW_PIN_PUK_AUTHENTICATION_REQUIRED);
        }
    }

    private void save() throws TokenException {
        checkConnected();
        try {
            FileOutputStream file = new FileOutputStream(FILENAME);
            ObjectOutputStream o = new ObjectOutputStream(file);
            o.writeObject(this.pin);
            o.writeObject(this.puk);
            o.writeObject(this.pinTrysLeft);
            o.writeObject(this.pukTrysLeft);
            o.writeObject(this.pinIsSet);
            o.writeObject(this.pukIsSet);
            o.writeObject(this.userKeySig);
            o.writeObject(this.userKeyEnc);
            o.writeObject(this.serverKey);
            o.writeObject(this.clusterKeys);
            o.close();
        } catch (Exception e) {
            throw new TokenException(e.getMessage());
        }

    }

    @Override
    public void connect() throws TokenException {
        this.connected = true;
        File saveFile = new File(FILENAME);
        if (saveFile.exists()) {
            try {
                FileInputStream file = new FileInputStream(FILENAME);
                ObjectInputStream o = new ObjectInputStream(file);
                this.pin = (byte[]) o.readObject();
                this.puk = (byte[]) o.readObject();
                this.pinTrysLeft = (Short) o.readObject();
                this.pukTrysLeft = (Short) o.readObject();
                this.pinIsSet = (Boolean) o.readObject();
                this.pukIsSet = (Boolean) o.readObject();
                this.userKeySig = (UserKeySigPair) o.readObject();
                this.userKeyEnc = (UserKeyEncPair) o.readObject();
                this.serverKey = (ServerKey) o.readObject();
                this.clusterKeys = (ClusterKeyStore) o.readObject();
                o.close();
            } catch (Exception e) {
                throw new TokenException(e.getMessage());
            }
        }
    }

    @Override
    public void disconnect() throws TokenException {
        try {
            save();
        } catch (Exception e) {
            throw new TokenException(e.getMessage());
        }
        this.connected = false;
        this.pinLoggedIn = false;
        this.pukLoggedIn = false;
    }

    @Override
    public void loginPIN(byte[] pin) throws TokenException {
        checkConnected();
        if (this.pinTrysLeft > 0) {
            if (Arrays.equals(pin, this.pin)) {
                this.pinLoggedIn = true;
                this.pinTrysLeft = PIN_TRY_LIMIT;
                return;
            } else {
                this.pinTrysLeft--;
            }
        }
        throw new TokenException(ReturnCodes.SW_PIN_VALIDATION_FAILED);
    }

    @Override
    public void logoutPIN() throws TokenException {
        checkConnected();
        this.pinLoggedIn = false;
    }

    @Override
    public void setPIN(byte[] pin) throws TokenException {
        checkConnected();
        checkPukLoggedIn();
        if (pin.length >= TokenConstants.PIN_MIN_SIZE && pin.length <= TokenConstants.PIN_MAX_SIZE) {
            this.pin = pin;
            this.pinIsSet = true;
            save();
        } else {
            throw new TokenException(ReturnCodes.SW_PIN_WRONG_SIZE);
        }
    }

    @Override
    public void loginPUK(byte[] puk) throws TokenException {
        checkConnected();
        if (this.pukTrysLeft > 0) {
            if (Arrays.equals(puk, this.puk)) {
                this.pukLoggedIn = true;
                this.pukTrysLeft = PUK_TRY_LIMIT;
                return;
            } else {
                this.pukTrysLeft--;
            }
        }
        throw new TokenException(ReturnCodes.SW_PUK_VALIDATION_FAILED);
    }

    @Override
    public void logoutPUK() throws TokenException {
        checkConnected();
        this.pukLoggedIn = false;
    }

    @Override
    public void setPUK(byte[] puk) throws TokenException {
        checkConnected();
        checkPukLoggedIn();
        if (this.pukIsSet) {
            throw new TokenException(ReturnCodes.SW_PUK_SET_ONLY_ONCE);
        }

        if (puk.length >= TokenConstants.PUK_MIN_SIZE && puk.length <= TokenConstants.PUK_MAX_SIZE) {
            this.puk = puk;
            this.pukIsSet = true;
            save();
        } else {
            throw new TokenException(ReturnCodes.SW_PUK_WRONG_SIZE);
        }
    }

    @Override
    public void generateUserKeys() throws TokenException {
        checkConnected();
        checkPukLoggedIn();
        this.userKeySig.generate(KEY_SIZE);
        this.userKeyEnc.generate(KEY_SIZE);
        save();

    }
    

    @Override
    public UserKey getUserKeySig() throws TokenException {
        checkConnected();
        checkPinLoggedIn();
        return this.userKeySig.getPublicKey();
    }

    @Override
    public UserKey getUserKeyEnc() throws TokenException {
        checkConnected();
        checkPinLoggedIn();
        return this.userKeyEnc.getPublicKey();
    }

    @Override
    public byte[] sign(byte[] in) throws TokenException {
        checkConnected();
        checkPinLoggedIn();
        return this.userKeySig.sign(in);
    }

    @Override
    public DocumentKey createDocumentKey() throws TokenException {
        checkConnected();
        byte[] byteKey = new byte[TokenConstants.DKEY_LEN];
        random.nextBytes(byteKey);
        return new DocumentKey(byteKey, false);
    }

    @Override
    public DocumentKey encryptDocumentKey(ClusterKeyId keyId,
            DocumentKey documentKey) throws TokenException {
        checkConnected();
        return encryptDocumentKey(keyId, documentKey, DEFAULT_IV);
    }

    @Override
    public DocumentKey encryptDocumentKey(ClusterKeyId keyId,
            DocumentKey documentKey, byte[] IV) throws TokenException {
        checkConnected();
        checkPinLoggedIn();
        if (IV.length != TokenConstants.CBC_BLOCK_LEN) {
            throw new TokenException("IV has invalid length");
        }
        if (documentKey.isEncrypted()) {
            throw new TokenException("DocumentKey is already encrypted");
        }
        int pos = this.clusterKeys.getClusterKeyPosition(keyId);
        if (pos == TokenConstants.POS_NOT_FOUND) {
            throw new TokenException("Cannot encrypt document key, clusterKey not found");
        }
        return new DocumentKey(this.clusterKeys.encrypt(pos, IV, documentKey.getKey().getBytes()), true);
    }

    @Override
    public DocumentKey decryptDocumentKey(ClusterKeyId keyId,
            DocumentKey documentKey) throws TokenException {
        checkConnected();
        return decryptDocumentKey(keyId, documentKey, DEFAULT_IV);
    }

    @Override
    public DocumentKey decryptDocumentKey(ClusterKeyId keyId,
            DocumentKey documentKey, byte[] IV) throws TokenException {
        checkConnected();
        checkPinLoggedIn();
        if (IV.length != TokenConstants.CBC_BLOCK_LEN) {
            throw new TokenException("IV has invalid length");
        }
        if (!documentKey.isEncrypted()) {
            throw new TokenException("DocumentKey is not encrypted");
        }
        int pos = this.clusterKeys.getClusterKeyPosition(keyId);
        if (pos == TokenConstants.POS_NOT_FOUND) {
            throw new TokenException("Cannot decrypt document key, clusterKey not found");
        }
        return new DocumentKey(this.clusterKeys.decrypt(pos, IV, documentKey.getKey().getBytes()), false);
    }

    @Override
    public void removeClusterKey(ClusterKeyId keyId) throws TokenException {
        checkConnected();
        checkPinLoggedIn();
        clusterKeys.removeClusterKey(this.clusterKeys.getClusterKeyPosition(keyId));
    }

    @Override
    public void importClusterKey(ClusterKey clusterKey) throws TokenException {
        checkConnected();
        checkPinLoggedIn();

        int pos = this.clusterKeys.getClusterKeyPosition(clusterKey.getId());
        if (pos != TokenConstants.POS_NOT_FOUND) {
            throw new TokenException(
                    "Could not add clusterKey, identifier is in use.");
        }
        pos = this.clusterKeys.getClusterKeyFreePosition();
        if (pos == TokenConstants.POS_NOT_FOUND) {
            throw new TokenException(
                    "Could not add clusterKey, all slots are occupied.");
        }
        byte[] aesKey = this.userKeyEnc.decrypt(clusterKey.getKey().getBytes());
        this.clusterKeys.addClusterKey(pos, new ClusterKey(aesKey, clusterKey.getId()));
    }

    @Override
    public boolean isClusterKeyAvailable(ClusterKeyId keyId)
            throws TokenException {
        checkConnected();
        checkPinLoggedIn();
        return (this.clusterKeys.getClusterKeyPosition(keyId) != TokenConstants.POS_NOT_FOUND);
    }

    @Override
    public ClusterKeyId[] getAvailableClusterKeys() throws TokenException {
        checkConnected();
        checkPinLoggedIn();
        return this.clusterKeys.getAllClusterKeys();
    }

    @Override
    public void clearClusterKeys() throws TokenException {
        checkConnected();
        checkPinLoggedIn();
        this.clusterKeys.clearAllClusterKeys();
    }

    @Override
    public void setServerKey(ServerKey serverKey) throws TokenException {
        checkConnected();
        checkPukLoggedIn();
        this.serverKey = serverKey;
    }

    @Override
    public ServerKey getServerKey() throws TokenException {
        checkConnected();
        checkPinLoggedIn();
        if (this.serverKey == null) {
            throw new TokenException(ReturnCodes.SW_SKEY_NOT_SET);
        }
        return this.serverKey;
    }

    @Override
    public TokenStatus getStatus() throws TokenException {
        checkConnected();
        return new TokenStatus(this.pukIsSet, this.pinIsSet,
                this.userKeySig.isInitialized() && this.userKeyEnc.isInitialized(),
                (this.serverKey == null ? false : true));
    }

    @Override
    public String getVersion() throws TokenException {
        checkConnected();
        return new String(VERSION_STRING);
    }
}

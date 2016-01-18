package org.sec2.token.swtoken;

import java.io.*;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import org.sec2.configuration.ConfigurationManager;
import org.sec2.configuration.exceptions.ExConfigurationInitializationFailure;
import org.sec2.configuration.exceptions.ExNoSuchProperty;
import org.sec2.configuration.exceptions.ExRestrictedPropertyAccess;
import org.sec2.token.IToken;
import org.sec2.token.ReturnCodes;
import org.sec2.token.TokenConstants;
import org.sec2.token.exceptions.TokenException;
import org.sec2.token.keys.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * holds the implementation of a the SoftwareToken and stores its information in
 * a file called crypto-date if generated with StandardConstructor.
 *
 * @author Dominik Preiktschat
 */
public class SoftwareToken implements IToken {
    
    private static final long serialVersionUID = 2L;
    
    private static final byte[] DEFAULT_IV = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private static final byte[] VERSION_STRING = {'1', '.', '2'};
    /**
     * The Standard file for config.
     */
    protected static String filename = "crypto-data";
    private static final String CONFIG_FILENAME = 
            "org.sec2.token.swtoken.filename";
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
    protected UserKeySigPair userKeySig;
    protected UserKeyEncPair userKeyEnc;
    private ServerKey serverKey;
    private SecureRandom random;
    private GroupKeyStore groupKeys;
    /**
     * The slf4j Logging Interace.
     */
    private static Logger log = LoggerFactory.getLogger(SoftwareToken.class);

    protected SoftwareToken() {

        this.random = new SecureRandom();
        this.pin = TokenConstants.DEFAULT_PIN;
        this.puk = TokenConstants.DEFAULT_PUK;
        this.pinTrysLeft = PIN_TRY_LIMIT;
        this.pukTrysLeft = PUK_TRY_LIMIT;
        this.userKeySig = new UserKeySigPair();
        this.userKeyEnc = new UserKeyEncPair();
        this.groupKeys = new GroupKeyStore(16);

        try {

            filename = ConfigurationManager.
                    getInstance().getConfigurationProperty(CONFIG_FILENAME);
        } catch (ExNoSuchProperty ex) {
            // silently ignore. use standard file implicit.
        } catch (ExRestrictedPropertyAccess ex) {
            // silently ignore. use standard file impicit.
        } catch (ExConfigurationInitializationFailure ex) {
            // silently ignore. use standard file implicit.
        }

        log.trace("Software token file name: {}", filename);
    }
    static private SoftwareToken instance = null;

    static public SoftwareToken getInstance() {
        if (instance == null) {
            instance = new SoftwareToken();
        }
        return instance;
    }

    protected void checkConnected() throws TokenException {
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

    protected void save() throws TokenException {
        checkConnected();
        try {
            FileOutputStream file = new FileOutputStream(filename);
            ObjectOutputStream o = new ObjectOutputStream(file);
            o.writeObject(this.pin);
            o.writeObject(this.puk);
            o.writeObject(this.pinTrysLeft);
            o.writeObject(this.pukTrysLeft);
            o.writeObject(this.pinIsSet);
            o.writeObject(this.pukIsSet);
            o.writeObject(this.userKeySig.getEncoded());
            o.writeObject(this.userKeyEnc.getEncoded());
            o.writeObject(this.serverKey.getModulus().getBytes());
            o.writeObject(this.serverKey.getExponent().getBytes());
            o.writeObject(this.groupKeys);
            o.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new TokenException(e.getMessage());
        }

    }

    @Override
    public void connect() throws TokenException {

        this.connected = true;
        File saveFile = new File(filename);
        if (saveFile.exists()) {
            ObjectInputStream o = null;
            try {
                FileInputStream file = new FileInputStream(filename);

                log.trace("Software-Token "
                        + "Connection to File: {} opened.", saveFile.getAbsolutePath());
                o = new ObjectInputStream(file);
                this.pin = (byte[]) o.readObject();
                this.puk = (byte[]) o.readObject();
                this.pinTrysLeft = (Short) o.readObject();
                this.pukTrysLeft = (Short) o.readObject();
                this.pinIsSet = (Boolean) o.readObject();
                this.pukIsSet = (Boolean) o.readObject();
                this.userKeySig = new UserKeySigPair();
                this.userKeySig.generate(getKeyPair((byte[]) o.readObject()));
                this.userKeyEnc = new UserKeyEncPair();
                this.userKeyEnc.generate(getKeyPair((byte[]) o.readObject()));
                this.serverKey = new ServerKey((byte[]) o.readObject(), (byte[]) o.readObject());System.out.println("Server key modulus: " + this.serverKey.getModulus().toString());
                this.groupKeys = (GroupKeyStore) o.readObject();
            } catch (IOException ex) {
                ex.printStackTrace();
                throw new TokenException(ex.getMessage());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new TokenException(e.getMessage());
            } finally {
                try {
                    o.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } else {System.out.println(saveFile.getAbsolutePath());
       
            log.warn("Statefile {} not present.", filename);
            throw new TokenException("State file not present.");
        }
    }

    private KeyPair getKeyPair(final byte[] privKeyBytes) throws TokenException {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            KeySpec ks = new PKCS8EncodedKeySpec(privKeyBytes);
            RSAPrivateCrtKey privk =
                    (RSAPrivateCrtKey) keyFactory.generatePrivate(ks);
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(
                    privk.getModulus(), privk.getPublicExponent());
            RSAPublicKey pubk =
                    (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
            return new KeyPair(pubk, privk);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            throw new TokenException(e.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new TokenException(e.toString());
        }
    }
    
    @Override
    public void disconnect() throws TokenException {
        try {
            save();
        } catch (Exception e) {
//            e.printStackTrace();
            throw new TokenException("Could not save due to:"
                    + e.getMessage());
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
        log.trace("Generating Keys...");
        this.userKeySig.generate(KEY_SIZE);
        log.trace("Key Generated:", userKeyEnc);
        this.userKeyEnc.generate(KEY_SIZE);
        log.trace("Key Generated:", userKeyEnc);
        save();

    }

    @Override
    public UserKey getUserKeySig() throws TokenException {
        log.trace("Requesteted: Signature Key {}",
                this.userKeySig.getPublicKey());
        checkConnected();
        checkPinLoggedIn();
        return this.userKeySig.getPublicKey();
    }

    @Override
    public UserKey getUserKeyEnc() throws TokenException {
        log.trace("Requesteted: Encryption Key {}",
                this.userKeyEnc.getPublicKey());
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
    public DocumentKey encryptDocumentKey(GroupKeyId keyId,
            DocumentKey documentKey) throws TokenException {
        checkConnected();
        return encryptDocumentKey(keyId, documentKey, DEFAULT_IV);
    }

    @Override
    public DocumentKey encryptDocumentKey(GroupKeyId keyId,
            DocumentKey documentKey, byte[] IV) throws TokenException {
        checkConnected();
        checkPinLoggedIn();
        if (IV.length != TokenConstants.CBC_BLOCK_LEN) {
            throw new TokenException("IV has invalid length");
        }
        if (documentKey.isEncrypted()) {
            throw new TokenException("DocumentKey is already encrypted");
        }
        int pos = this.groupKeys.getGroupKeyPosition(keyId);
        if (pos == TokenConstants.POS_NOT_FOUND) {
            throw new TokenException("Cannot encrypt document key, groupKey not found");
        }
        return new DocumentKey(this.groupKeys.encrypt(pos, IV, documentKey.getKey().getBytes()), true);
    }

    @Override
    public DocumentKey decryptDocumentKey(GroupKeyId keyId,
            DocumentKey documentKey) throws TokenException {
        checkConnected();
        return decryptDocumentKey(keyId, documentKey, DEFAULT_IV);
    }

    @Override
    public DocumentKey decryptDocumentKey(GroupKeyId keyId,
            DocumentKey documentKey, byte[] IV) throws TokenException {
        checkConnected();
        checkPinLoggedIn();
        if (IV.length != TokenConstants.CBC_BLOCK_LEN) {
            throw new TokenException("IV has invalid length");
        }
        if (!documentKey.isEncrypted()) {
            throw new TokenException("DocumentKey is not encrypted");
        }
        int pos = this.groupKeys.getGroupKeyPosition(keyId);
        if (pos == TokenConstants.POS_NOT_FOUND) {
            throw new TokenException("Cannot decrypt document key, groupKey not found");
        }
        return new DocumentKey(this.groupKeys.decrypt(pos, IV, documentKey.getKey().getBytes()), false);
    }

    @Override
    public void removeGroupKey(GroupKeyId keyId) throws TokenException {
        checkConnected();
        checkPinLoggedIn();
        groupKeys.removeGroupKey(this.groupKeys.getGroupKeyPosition(keyId));
    }

    @Override
    public void importGroupKey(GroupKey groupKey) throws TokenException {
        checkConnected();
        checkPinLoggedIn();

        int pos = this.groupKeys.getGroupKeyPosition(groupKey.getId());
        if (pos != TokenConstants.POS_NOT_FOUND) {
            throw new TokenException(
                    "Could not add groupKey, identifier is in use.");
        }
        pos = this.groupKeys.getGroupKeyFreePosition();
        if (pos == TokenConstants.POS_NOT_FOUND) {
            throw new TokenException(
                    "Could not add groupKey, all slots are occupied.");
        }
        byte[] aesKey = this.userKeyEnc.decrypt(groupKey.getKey().getBytes());
        this.groupKeys.addGroupKey(pos, new GroupKey(aesKey, groupKey.getId()));
    }

    @Override
    public boolean isGroupKeyAvailable(GroupKeyId keyId)
            throws TokenException {
        checkConnected();
        checkPinLoggedIn();
        return (this.groupKeys.getGroupKeyPosition(keyId) != TokenConstants.POS_NOT_FOUND);
    }

    @Override
    public GroupKeyId[] getAvailableGroupKeys() throws TokenException {
        checkConnected();
        checkPinLoggedIn();
        return this.groupKeys.getAllGroupKeys();
    }

    @Override
    public void clearGroupKeys() throws TokenException {
        checkConnected();
        checkPinLoggedIn();
        this.groupKeys.clearAllGroupKeys();
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.token;

import java.io.*;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import javax.security.cert.X509Certificate;
import org.sec2.token.IToken.TokenStatus;
import org.sec2.token.exceptions.TokenException;
import org.sec2.token.keys.ByteArray;
import org.sec2.token.keys.GroupKeyId;
import org.sec2.token.keys.ServerKey;
import org.sec2.token.keys.UserKey;

/**
 *
 * @author benedikt
 */
public class TokenHelper implements ITokenProgress {

    private final IToken sec2Token;
    private final ITokenProgress progressCallback;
    /*
     * These strings will be sent via ITokenProgess to indicate the status of
     * the token's operation.
     */
    private static final String DESC_CONNECT = "Connecting";
    private static final String DESC_DISCONNECT = "Disconnecting";
    private static final String DESC_LOGIN_PIN = "Authenticating with PIN";
    private static final String DESC_LOGOUT_PIN = "Invalidating PIN authentication";
    private static final String DESC_LOGIN_PUK = "Authenticating with PUK";
    private static final String DESC_LOGOUT_PUK = "Invalidating PUK authentication";
    private static final String DESC_TOKEN_GET_VERSION = "Requesting token software version";
    private static final String DESC_TOKEN_GET_STATUS = "Requesting token status";
    private static final String DESC_GET_AVAILABLE_GROUP_KEYS = "Requesting list of available group-key ids";
    private static final String DESC_GET_USER_KEY_SIG = "Requesting user's public-key for signatures";
    private static final String DESC_GET_USER_KEY_ENC = "Requesting user's public-key for encryption";
    private static final String DESC_GENERATE_USER_KEYS = "Generating user key-pairs";
    private static final String DESC_GET_SERVER_KEY = "Requesting server's public-key";
    private static final String DESC_SET_SERVER_KEY = "Sending server's public-key";
    private static final String DESC_SET_PIN = "Setting PIN";
    private static final String DESC_SET_PUK = "Setting PUK";

    public TokenHelper(IToken sec2Token) {
        this.sec2Token = sec2Token;
        /*
         * If user of TokenHelper does not want to use callback, we simply
         * redirect it to ourselves. This is also the reason why we must
         * implement init(), update() and done()
         */
        this.progressCallback = this;
    }

    public TokenHelper(IToken sec2Token, ITokenProgress callback) {
        this.sec2Token = sec2Token;
        this.progressCallback = callback;
    }

    @Override
    public void init(int maxSteps) {
        /*
         * Don't do anything.
         */
    }

    @Override
    public void update(String desc) {
        /*
         * Don't do anything.
         */
    }

    @Override
    public void done() {
        /*
         * Don't do anything.
         */
    }

    public TokenStatus getStatus() throws TokenException {
        progressCallback.init(3);

        progressCallback.update(DESC_CONNECT);
        sec2Token.connect();

        progressCallback.update(DESC_TOKEN_GET_STATUS);
        TokenStatus stat = sec2Token.getStatus();

        progressCallback.update(DESC_DISCONNECT);
        sec2Token.disconnect();

        progressCallback.done();

        return stat;
    }

    public String[] listGroupKeyIds(ByteArray PIN) throws TokenException {
        progressCallback.init(5);

        progressCallback.update(DESC_CONNECT);
        sec2Token.connect();

        progressCallback.update(DESC_LOGIN_PIN);
        sec2Token.loginPIN(PIN.getBytes());

        progressCallback.update(DESC_GET_AVAILABLE_GROUP_KEYS);
        GroupKeyId[] keyIds = sec2Token.getAvailableGroupKeys();

        progressCallback.update(DESC_LOGOUT_PIN);
        sec2Token.logoutPIN();

        progressCallback.update(DESC_DISCONNECT);
        sec2Token.disconnect();

        short i;
        String[] keyIdList = new String[keyIds.length];
        for (i = 0; i < keyIds.length; i++) {
            keyIdList[i] = keyIds[i].toString();
        }

        progressCallback.done();

        return keyIdList;
    }

    public UserKey getUserKeyEnc(ByteArray PIN) throws TokenException {
        progressCallback.init(5);

        progressCallback.update(DESC_CONNECT);
        sec2Token.connect();

        progressCallback.update(DESC_LOGIN_PIN);
        sec2Token.loginPIN(PIN.getBytes());

        progressCallback.update(DESC_GET_USER_KEY_ENC);
        UserKey userKey = sec2Token.getUserKeyEnc();

        progressCallback.update(DESC_LOGOUT_PIN);
        sec2Token.logoutPIN();

        progressCallback.update(DESC_DISCONNECT);
        sec2Token.disconnect();

        progressCallback.done();

        return userKey;
    }

    public UserKey getUserKeySig(ByteArray PIN) throws TokenException {
        progressCallback.init(5);

        progressCallback.update(DESC_CONNECT);
        sec2Token.connect();

        progressCallback.update(DESC_LOGIN_PIN);
        sec2Token.loginPIN(PIN.getBytes());

        progressCallback.update(DESC_GET_USER_KEY_SIG);
        UserKey userKey = sec2Token.getUserKeySig();

        progressCallback.update(DESC_LOGOUT_PIN);
        sec2Token.logoutPIN();

        progressCallback.update(DESC_DISCONNECT);
        sec2Token.disconnect();

        progressCallback.done();

        return userKey;
    }

    public ServerKey getServerKey(ByteArray PIN) throws TokenException {
        progressCallback.init(5);

        progressCallback.update(DESC_CONNECT);
        sec2Token.connect();

        progressCallback.update(DESC_LOGIN_PIN);
        sec2Token.loginPIN(PIN.getBytes());

        progressCallback.update(DESC_GET_SERVER_KEY);
        ServerKey serverKey = sec2Token.getServerKey();

        progressCallback.update(DESC_LOGOUT_PIN);
        sec2Token.logoutPIN();

        progressCallback.update(DESC_DISCONNECT);
        sec2Token.disconnect();

        progressCallback.done();

        return serverKey;
    }

    public String getVersion() throws TokenException {
        progressCallback.init(3);

        progressCallback.update(DESC_CONNECT);
        sec2Token.connect();

        progressCallback.update(DESC_TOKEN_GET_VERSION);
        String ver = sec2Token.getVersion();

        progressCallback.update(DESC_DISCONNECT);
        sec2Token.disconnect();

        progressCallback.done();

        return ver;
    }

    public void initialize(ByteArray newPIN, ByteArray newPUK, String x509CertFile)
      throws TokenException, IOException {
        TokenStatus tokenStatus = getStatus();
        if (tokenStatus.pukIsChanged) {
            throw new TokenException(
              "PUK is already set, initialization impossible");
        }

        setServerKey(new ByteArray(TokenConstants.DEFAULT_PUK), x509CertFile);
        setPIN(new ByteArray(TokenConstants.DEFAULT_PUK), newPIN);
        generateUserKeys(TokenConstants.DEFAULT_PUK);
        setPUK(newPUK);
    }

    public void generateUserKeys(String PUK) throws TokenException {
        generateUserKeys(PUK.getBytes());
    }

    public void generateUserKeys(byte[] PUK) throws TokenException {
        progressCallback.init(5);

        progressCallback.update(DESC_CONNECT);
        sec2Token.connect();

        progressCallback.update(DESC_LOGIN_PUK);
        sec2Token.loginPUK(PUK);

        progressCallback.update(DESC_GENERATE_USER_KEYS);
        sec2Token.generateUserKeys();

        progressCallback.update(DESC_LOGOUT_PUK);
        sec2Token.logoutPUK();

        progressCallback.update(DESC_DISCONNECT);
        sec2Token.disconnect();

        progressCallback.done();
    }

    public void setPUK(ByteArray PUK) throws TokenException {
        progressCallback.init(4);

        progressCallback.update(DESC_CONNECT);
        sec2Token.connect();

        progressCallback.update(DESC_LOGIN_PUK);
        sec2Token.loginPUK(TokenConstants.DEFAULT_PUK);
        /*
         * Setting of PUK performs a logout!
         */
        progressCallback.update(DESC_SET_PUK);
        sec2Token.setPUK(PUK.getBytes());

        progressCallback.update(DESC_DISCONNECT);
        sec2Token.disconnect();

        progressCallback.done();
    }

    public void setPIN(ByteArray PUK, ByteArray newPIN) throws TokenException {
        progressCallback.init(5);

        progressCallback.update(DESC_CONNECT);
        sec2Token.connect();

        progressCallback.update(DESC_LOGIN_PUK);
        sec2Token.loginPUK(PUK.getBytes());

        progressCallback.update(DESC_SET_PIN);
        sec2Token.setPIN(newPIN.getBytes());

        progressCallback.update(DESC_LOGOUT_PUK);
        sec2Token.logoutPUK();

        progressCallback.update(DESC_DISCONNECT);
        sec2Token.disconnect();

        progressCallback.done();
    }

    public void setServerKey(ByteArray PUK, String x509CertFile)
      throws TokenException, IOException {
        X509Certificate x509Cert = null;

        /*
         * Read the CERT file
         */
        try {
            InputStream inStream = new FileInputStream(x509CertFile);
            x509Cert = X509Certificate.getInstance(inStream);
            inStream.close();
        } catch (Exception ex) {
            throw new IOException("File is no x509 cert");
        }

        /*
         * Extract exponent and modulos.
         */
        if (x509Cert != null) {
            PublicKey pubKey = x509Cert.getPublicKey();
            if (pubKey instanceof RSAPublicKey) {
                RSAPublicKey rsaKey = (RSAPublicKey) pubKey;

                byte[] sKeyMod2er = rsaKey.getModulus().toByteArray();
                byte[] sKeyExp2er = rsaKey.getPublicExponent().toByteArray();

                /*
                 * Get rid of leading zeros of BigInteger conversion (it's
                 * 2-complement representation..)
                 */
                byte[] sKeyMod = Arrays.copyOfRange(sKeyMod2er, 1,
                  sKeyMod2er.length);
                byte[] sKeyExp = Arrays.copyOfRange(sKeyExp2er, 1,
                  sKeyExp2er.length);

                setServerKey(PUK.getBytes(), sKeyMod, sKeyExp);
            } else {
                throw new IOException("File does not contain a x509 PublicKey");
            }

        }
    }

    private void setServerKey(byte[] PUK, byte[] sKeyMod, byte[] sKeyExp)
      throws TokenException {
        progressCallback.init(5);

        ServerKey serverKey = new ServerKey(sKeyMod, sKeyExp);

        progressCallback.update(DESC_CONNECT);
        sec2Token.connect();

        progressCallback.update(DESC_LOGIN_PUK);
        sec2Token.loginPUK(PUK);

        progressCallback.update(DESC_SET_SERVER_KEY);
        sec2Token.setServerKey(serverKey);

        progressCallback.update(DESC_LOGOUT_PUK);
        sec2Token.logoutPUK();

        progressCallback.update(DESC_DISCONNECT);
        sec2Token.disconnect();

        progressCallback.done();
    }
}

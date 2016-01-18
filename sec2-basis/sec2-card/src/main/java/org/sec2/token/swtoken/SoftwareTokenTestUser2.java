/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.token.swtoken;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import org.sec2.statictestdata.TestKeyProvider;
import org.sec2.token.TokenConstants;
import org.sec2.token.exceptions.TokenException;
import org.sec2.token.keys.ServerKey;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thorsten Schreiber <thorsten.schreiber@rub.de>
 */
public class SoftwareTokenTestUser2 extends SoftwareToken {

    //FIXME: Only needed for tests, remove for production use
    
    /**
     * The slf4j Logging Interace.
     */
    private static org.slf4j.Logger log = LoggerFactory.getLogger(
            SoftwareTokenTestUser2.class);
    
    private static KeyPair TEST_USER_KEY_SIG =
            TestKeyProvider.getInstance().getUser2SignKey();
    private static KeyPair TEST_USER_KEY_ENC =
            TestKeyProvider.getInstance().getUser2EncKey();

    private SoftwareTokenTestUser2() {
        super();
        super.filename += "testUser2";
    }

    @Override
    public void connect() throws TokenException {
        try {
            super.connect();
        } catch (TokenException e) {
            this.generateUserKeys();
            this.loginPUK(TokenConstants.DEFAULT_PUK);
            this.setServerKey(new ServerKey(((RSAPublicKey)TestKeyProvider.getInstance().getKeyserverSignKey().getPublic()).getModulus().toByteArray(), ((RSAPublicKey)TestKeyProvider.getInstance().getKeyserverSignKey().getPublic()).getPublicExponent().toByteArray()));
            super.save();
            super.connect();
        }
        super.save();
    }
    
    @Override
    public void generateUserKeys() throws TokenException {

        super.userKeySig.generate(TEST_USER_KEY_SIG);
        super.userKeyEnc.generate(TEST_USER_KEY_ENC);

    }
    private static SoftwareToken instance = null;

    public static SoftwareToken getInstance() {
        if (instance == null) {
            instance = new SoftwareTokenTestUser2();
        }
        return instance;
    }
}

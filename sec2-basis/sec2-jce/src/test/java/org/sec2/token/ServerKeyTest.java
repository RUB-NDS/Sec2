package org.sec2.token;

import org.sec2.token.keys.ServerKey;
import org.sec2.token.keys.UserKey;
import org.sec2.token.TokenConstants;
import java.security.KeyFactory;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import javax.crypto.Cipher;
import org.sec2.token.IToken.TokenStatus;

public class ServerKeyTest extends ATokenTest {

    /**
     * Set the server key to the user key, and retreive the key again. Make sure
     * that both keys match.
     */
    public void testGetAndSetServerKey() {
        UserKey userKey = null;
        ServerKey serverKey = null;

        /*
         * Simply get the user-key's public part and set it as server-key.
         */
        try {
            userKey = tokenHandler.getUserKeyEnc();
            tokenHandler.loginPUK(pukCode);
            tokenHandler.setServerKey(new ServerKey(
                    userKey.getModulus().getBytes(),
                    userKey.getExponent().getBytes()));
            tokenHandler.logoutPUK();
        } catch (Exception e) {
            fail(e.toString());
        }

        /*
         * Read the server-key again and make sure its still the same as the
         * user-key.
         */
        try {
            serverKey = tokenHandler.getServerKey();
        } catch (Exception e) {
            fail(e.toString());
        }

        assertNotNull(serverKey);
        assertNotNull(userKey);
        assertArrayEquals(serverKey.getExponent().getBytes(), userKey.getExponent().getBytes());
        assertArrayEquals(serverKey.getModulus().getBytes(), userKey.getModulus().getBytes());
    }
}

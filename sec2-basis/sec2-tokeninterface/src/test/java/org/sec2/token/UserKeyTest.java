package org.sec2.token;

import org.sec2.token.keys.UserKey;
import org.sec2.token.TokenConstants;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import javax.crypto.Cipher;

public class UserKeyTest extends ATokenTest {

    /**
     * Use the user key to sign hash value. Verify the signature by retrieving
     * the users public key and unsigning the hash.
     */
    public void testSign() {
        byte[] sha1Hash = new byte[TokenConstants.SHA1_HASH_LEN];
        byte[] sig = null;
        byte[] outHash = null;

        /*
         * Sign some random value
         */
        PRNG.nextBytes(sha1Hash);
        try {
            sig = tokenHandler.sign(sha1Hash);
        } catch (Exception e) {
            fail(e.toString());
        }

        assertNotNull(sig);

        /*
         * Verify signature
         */
        try {
            /*
             * Read & convert key into RSAPublicKey
             */
            UserKey userKey = tokenHandler.getUserKeySig();
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(
                    new RSAPublicKeySpec(userKey.getModulus().toBigInteger(),
                    userKey.getExponent().toBigInteger()));

            /*
             * Encrypt aesKey with retrieved public key
             */
            String rsaAlgorithm = "RSA/ECB/PKCS1PADDING";
            Cipher encCipher = Cipher.getInstance(rsaAlgorithm);
            encCipher.init(Cipher.DECRYPT_MODE, pubKey);
            outHash = encCipher.doFinal(sig);
        } catch (Exception e) {
            fail(e.toString());
        }

        /*
         * Check if sha1Hash und output match.
         */
        assertNotNull(outHash);
        assertNotNull(sha1Hash);
        assertArrayEquals(outHash, sha1Hash);
    }
}

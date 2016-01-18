package org.sec2.token.softwaretoken;

import java.security.KeyFactory;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import javax.crypto.Cipher;
import junit.framework.TestCase;
import org.sec2.token.IToken;
import org.spongycastle.util.Arrays;
import org.sec2.token.IToken.TokenStatus;
import org.sec2.token.TokenConstants;
import org.sec2.token.hwtoken.HardwareToken;
import org.sec2.token.keys.GroupKey;
import org.sec2.token.keys.UserKey;
import org.sec2.token.swtoken.SoftwareToken;

/**
 * Standard-Class for Properties and standard methods.
 */
abstract class ATokenTest extends TestCase {

    protected final IToken tokenHandler = SoftwareToken.getInstance();
    protected final SecureRandom PRNG = new SecureRandom();
    protected final byte[] pinCode = TokenConstants.DEFAULT_PIN;
    protected final byte[] pukCode = TokenConstants.DEFAULT_PUK;

    @Override
    public void setUp() {
        try {
            tokenHandler.connect();
            /*
             * Make sure we have a user key set.
             */
            TokenStatus stat = tokenHandler.getStatus();
            if (!stat.ukeysAreGenerated) {
                System.out.println("Generating keypairs on token, please wait.."
                  + " (this will happen only once)");

                tokenHandler.loginPUK(pukCode);
                tokenHandler.generateUserKeys();
                tokenHandler.loginPUK(pukCode);

                System.out.println("Done.");
            }
            tokenHandler.loginPIN(pinCode);
            tokenHandler.clearGroupKeys();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Override
    public void tearDown() {
        try {
            tokenHandler.logoutPIN();
            tokenHandler.disconnect();
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /**
     * This assert is part of the new JUnit framework. We simply re-implement it
     * here so that we can adapt to the new version easily.
     *
     * @param expected array 1
     * @param actual array 2
     */
    public void assertArrayEquals(byte[] expected, byte[] actual) {
        assertTrue(Arrays.areEqual(expected, actual));
    }

    /**
     * Generate a new GroupKey, which is subsequently encrypted with the
     * user's public key.
     *
     * @return a new, encrypted GroupKey
     * @throws Exception
     */
    public GroupKey generateEncryptedGroupKey() throws Exception {
        byte[] encAesKey = null;
        byte[] rndId = new byte[TokenConstants.GKEY_ID_LEN];
        byte[] aesKey = new byte[TokenConstants.GKEY_LEN];
        PRNG.nextBytes(rndId);
        PRNG.nextBytes(aesKey);

        /*
         * Read public key & convert into RSA Key
         */
        UserKey userKey = tokenHandler.getUserKeyEnc();
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(
          new RSAPublicKeySpec(userKey.getModulus().toBigInteger(),
          userKey.getExponent().toBigInteger()));

        /*
         * Encrypt aesKey with retrieved public key
         */
        String rsaAlgorithm = "RSA/ECB/OAEPWithSHA1AndMGF1Padding";
        Cipher encCipher = Cipher.getInstance(rsaAlgorithm);
        encCipher.init(Cipher.ENCRYPT_MODE, pubKey);
        encAesKey = encCipher.doFinal(aesKey);

        return new GroupKey(encAesKey, rndId);
    }
}

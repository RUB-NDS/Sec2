package org.sec2.token;

import org.sec2.token.keys.ClusterKeyId;
import org.sec2.token.keys.ClusterKey;
import org.sec2.token.TokenConstants;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import javax.crypto.Cipher;
import junit.framework.Test;
import org.bouncycastle.util.Arrays;
import org.sec2.token.exceptions.TokenException;

public class ClusterKeyTest extends ATokenTest {

    /**
     * Add a clusterKey and remove it again. Verify that it has been removed.
     */
    public void testAddAndRemoveClusterKey() {
        try {
            ClusterKey clusterKey = generateEncryptedClusterKey();
            tokenHandler.importClusterKey(clusterKey);
            assertTrue(tokenHandler.isClusterKeyAvailable(clusterKey.getId()));
            tokenHandler.removeClusterKey(clusterKey.getId());
            assertTrue(!tokenHandler.isClusterKeyAvailable(clusterKey.getId()));
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /**
     * Import a clusterKey two times. Verify that it can be imported only once.
     */
    public void testAddClusterKeyTwoTimes() {
        try {
            ClusterKey clusterKey = generateEncryptedClusterKey();
            tokenHandler.importClusterKey(clusterKey);
            try {
                tokenHandler.importClusterKey(clusterKey);
            } catch (TokenException e) {
                assertEquals(0x00, e.getSW());
            }
            tokenHandler.removeClusterKey(clusterKey.getId());
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /**
     * Add some clusterKeys to the card and verify that they have been added.
     */
    public void testAddSomeClusterKeys() {
        short KEY_ADD_COUNT = 4;

        /*
         * Add some clusterKeys.
         */
        ClusterKey[] clusterKeys = new ClusterKey[KEY_ADD_COUNT];
        try {
            for (int i = 0; i < KEY_ADD_COUNT; i++) {
                clusterKeys[i] = generateEncryptedClusterKey();
                tokenHandler.importClusterKey(clusterKeys[i]);
            }
        } catch (Exception e) {
            fail(e.toString());
        }

        /*
         * Verify that keys are all stored on card.
         */
        try {
            ClusterKeyId[] idList = tokenHandler.getAvailableClusterKeys();
            for (int i = 0; i < KEY_ADD_COUNT; i++) {
                assertArrayEquals(clusterKeys[i].getId().getBytes(),
                        idList[i].getBytes());
            }

        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /**
     * Write the token's keystore full of keys. After that, try to add another
     * key which is expected to fail.
     */
    public void testAddAllClusterKeys() {
        short KEY_ADD_COUNT = 16;

        /*
         * Add 16 clusterKeys and verify we have them all on the card.
         */
        ClusterKey[] clusterKeys = new ClusterKey[KEY_ADD_COUNT];
        try {
            for (int i = 0; i < KEY_ADD_COUNT; i++) {
                clusterKeys[i] = generateEncryptedClusterKey();
                tokenHandler.importClusterKey(clusterKeys[i]);
            }
            ClusterKeyId[] idList = tokenHandler.getAvailableClusterKeys();
            assertEquals(KEY_ADD_COUNT, idList.length);
        } catch (Exception e) {
            fail(e.toString());
        }

        /*
         * Add one more key, this is supposed to fail.
         */
        try {
            ClusterKey clusterKey = generateEncryptedClusterKey();
            tokenHandler.importClusterKey(clusterKey);
            fail("Importing too many clusterKeys did not throw an exception");
        } catch (TokenException e) {
            assertEquals(0x00, e.getSW());
        } catch (Exception e) {
            fail(e.toString());
        }
    }
}

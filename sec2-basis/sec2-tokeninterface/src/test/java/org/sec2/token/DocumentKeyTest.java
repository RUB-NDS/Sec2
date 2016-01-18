package org.sec2.token;

import org.sec2.token.keys.DocumentKey;
import org.sec2.token.keys.ClusterKey;
import org.sec2.token.TokenConstants;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import javax.crypto.Cipher;
import org.bouncycastle.util.Arrays;
import org.sec2.token.exceptions.TokenException;

public class DocumentKeyTest extends ATokenTest {

    /**
     * Generate a new documentKey. Verify the length of the generated key, we
     * could also include some kind of entropy test..
     */
    public void testCreateDocumentKey() {
        DocumentKey docKey = null;

        try {
            docKey = tokenHandler.createDocumentKey();
        } catch (Exception e) {
            fail(e.toString());
        }

        assertEquals(TokenConstants.DKEY_LEN, docKey.getKey().getBytes().length);
    }

    /**
     * Generate a documentKey and encrypt it and decrypt it after that. Verify
     * that the original key and the decryption-result match.
     */
    public void testEncryptDecryptDocumentKey() {
        DocumentKey docKey = null;
        DocumentKey decDocKey = null;

        try {
            ClusterKey clusterKey = generateEncryptedClusterKey();
            tokenHandler.importClusterKey(clusterKey);
            byte[] rndKey = new byte[TokenConstants.DKEY_LEN];
            PRNG.nextBytes(rndKey);
            docKey = new DocumentKey(rndKey, false);
            DocumentKey encDocKey = tokenHandler.encryptDocumentKey(clusterKey.getId(),
                    docKey);
            /* Make sure the encDocKey is actually not just the rndKey */
            assertTrue(!Arrays.areEqual(rndKey, encDocKey.getKey().getBytes()));
            decDocKey = tokenHandler.decryptDocumentKey(clusterKey.getId(), encDocKey);
        } catch (Exception e) {
            fail(e.toString());
        }

        assertNotNull(docKey);
        assertNotNull(decDocKey);
        assertArrayEquals(docKey.getKey().getBytes(), decDocKey.getKey().getBytes());
    }
}

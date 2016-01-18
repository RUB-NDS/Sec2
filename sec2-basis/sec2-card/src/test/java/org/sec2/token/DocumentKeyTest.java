package org.sec2.token;

import org.spongycastle.util.Arrays;
import org.sec2.token.keys.DocumentKey;
import org.sec2.token.keys.GroupKey;

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
            GroupKey groupKey = generateEncryptedGroupKey();
            tokenHandler.importGroupKey(groupKey);
            byte[] rndKey = new byte[TokenConstants.DKEY_LEN];
            PRNG.nextBytes(rndKey);
            docKey = new DocumentKey(rndKey, false);
            DocumentKey encDocKey = tokenHandler.encryptDocumentKey(groupKey.getId(),
              docKey);
            /*
             * Make sure the encDocKey is actually not just the rndKey
             */
            assertTrue(!Arrays.areEqual(rndKey, encDocKey.getKey().getBytes()));
            decDocKey = tokenHandler.decryptDocumentKey(groupKey.getId(), encDocKey);
        } catch (Exception e) {
            fail(e.toString());
        }

        assertNotNull(docKey);
        assertNotNull(decDocKey);
        assertArrayEquals(docKey.getKey().getBytes(), decDocKey.getKey().getBytes());
    }
}

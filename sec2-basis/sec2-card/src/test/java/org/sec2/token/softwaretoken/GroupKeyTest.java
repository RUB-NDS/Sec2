package org.sec2.token.softwaretoken;

import org.sec2.token.exceptions.TokenException;
import org.sec2.token.keys.GroupKey;
import org.sec2.token.keys.GroupKeyId;

public class GroupKeyTest extends ATokenTest {

    /**
     * Add a groupKey and remove it again. Verify that it has been removed.
     */
    public void testAddAndRemoveGroupKey() {
        try {
            GroupKey groupKey = generateEncryptedGroupKey();
            tokenHandler.importGroupKey(groupKey);
            assertTrue(tokenHandler.isGroupKeyAvailable(groupKey.getId()));
            tokenHandler.removeGroupKey(groupKey.getId());
            assertTrue(!tokenHandler.isGroupKeyAvailable(groupKey.getId()));
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /**
     * Import a groupKey two times. Verify that it can be imported only once.
     */
    public void testAddGroupKeyTwoTimes() {
        try {
            GroupKey groupKey = generateEncryptedGroupKey();
            tokenHandler.importGroupKey(groupKey);
            try {
                tokenHandler.importGroupKey(groupKey);
            } catch (TokenException e) {
                assertEquals(0x00, e.getSW());
            }
            tokenHandler.removeGroupKey(groupKey.getId());
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /**
     * Add some groupKeys to the card and verify that they have been added.
     */
    public void testAddSomeGroupKeys() {
        short KEY_ADD_COUNT = 4;

        /*
         * Add some groupKeys.
         */
        GroupKey[] groupKeys = new GroupKey[KEY_ADD_COUNT];
        try {
            for (int i = 0; i < KEY_ADD_COUNT; i++) {
                groupKeys[i] = generateEncryptedGroupKey();
                tokenHandler.importGroupKey(groupKeys[i]);
            }
        } catch (Exception e) {
            fail(e.toString());
        }

        /*
         * Verify that keys are all stored on card.
         */
        try {
            GroupKeyId[] idList = tokenHandler.getAvailableGroupKeys();
            for (int i = 0; i < KEY_ADD_COUNT; i++) {
                assertArrayEquals(groupKeys[i].getId().getBytes(),
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
    public void testAddAllGroupKeys() {
        short KEY_ADD_COUNT = 16;

        /*
         * Add 16 groupKeys and verify we have them all on the card.
         */
        GroupKey[] groupKeys = new GroupKey[KEY_ADD_COUNT];
        try {
            for (int i = 0; i < KEY_ADD_COUNT; i++) {
                groupKeys[i] = generateEncryptedGroupKey();
                tokenHandler.importGroupKey(groupKeys[i]);
            }
            GroupKeyId[] idList = tokenHandler.getAvailableGroupKeys();
            assertEquals(KEY_ADD_COUNT, idList.length);
        } catch (Exception e) {
            fail(e.toString());
        }

        /*
         * Add one more key, this is supposed to fail.
         */
        try {
            GroupKey groupKey = generateEncryptedGroupKey();
            tokenHandler.importGroupKey(groupKey);
            fail("Importing too many groupKeys did not throw an exception");
        } catch (TokenException e) {
            assertEquals(0x00, e.getSW());
        } catch (Exception e) {
            fail(e.toString());
        }
    }
}

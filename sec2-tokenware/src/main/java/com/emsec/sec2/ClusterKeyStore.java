package com.emsec.sec2;

import javacard.framework.ISOException;
import javacardx.framework.util.ArrayLogic;

public class ClusterKeyStore {

    private static final byte MIN_CKEY_COUNT = 8;
    private static final byte MAX_CKEY_COUNT = 32;
    private ClusterKey[] clusterKeys;
    private short idLen;
    private short numKeys;

    /**
     * Constructor.
     *
     * @param numKeys maximum number of keys to be stored
     * @param keyLen length of keys as given by KeyBuilder (i.e. in bits)
     * @param idLen length of id string
     */
    ClusterKeyStore(short numKeys, short keyLen, short idLen) {
        /*
         * Make sure we don't try to store too many keys and also do not exceed
         * the length of an APDU when we send all keyIds
         */
        short idListSize = (short) (numKeys * idLen);
        if ((numKeys < MIN_CKEY_COUNT) || (numKeys > MAX_CKEY_COUNT)
                || (idListSize > TokenIO.MAX_APDU_SIZE)) {
            ISOException.throwIt(TokenIO.SW_CKEY_SLOT_COUNT_INVALID);
        }

        short i;
        clusterKeys = new ClusterKey[numKeys];
        for (i = 0; i < numKeys; i++) {
            clusterKeys[i] = new ClusterKey(keyLen, idLen);
        }

        this.idLen = idLen;
        this.numKeys = numKeys;
    }

    /**
     * Private function to find a key with a given identifier and return its
     * position in the array.
     *
     * @param buffer buffer where identifier is found.
     * @param readOff offset in buffer where identifier is to be read
     * @param readLen length of identifier
     * @return position in array or TokenIO.POS_NOT_FOUND if id does not exist
     * @throws ISOException
     */
    private byte find(byte[] buffer, short readOff, short readLen)
            throws ISOException {
        if (readLen != idLen) {
            ISOException.throwIt(TokenIO.SW_CKEY_WRONG_ID_SIZE);
        }

        byte i;
        byte[] keyId = new byte[idLen];
        for (i = 0; i < clusterKeys.length; i++) {
            if (clusterKeys[i].isInitialized()) {
                clusterKeys[i].getId(keyId, (short) 0);
                try {
                    if (ArrayLogic.arrayCompareGeneric(keyId, (short) 0,
                            buffer, readOff, idLen) == 0) {
                        return i;
                    }
                } catch (Exception e) {
                    ISOException.throwIt(TokenIO.SW_GENERAL_ERROR);
                }
            }
        }

        return TokenIO.POS_NOT_FOUND;
    }

    /**
     * Simple wrapper for private find().
     *
     * @param buffer buffer where identifier is found and position byte is
     * written to.
     * @param readOff offset in buffer where identifier is found
     * @param readLen length of identifier
     * @param writeOff offset in buffer where position byte is to be written
     * @return number of written bytes, i.e. 1
     * @throws ISOException
     */
    short find(byte[] buffer, short readOff, short readLen, short writeOff)
            throws ISOException {
        buffer[writeOff] = find(buffer, readOff, readLen);
        return 1;
    }

    /**
     * Find an unused slot in the array where a key can be inserted.
     *
     * @return position where key can be written or TokenIO.POS_NOT_FOUND if
     * array is full
     */
    private byte getFreeSlot() {
        byte i;

        for (i = 0; i < numKeys; i++) {
            if (!clusterKeys[i].isInitialized()) {
                return i;
            }
        }

        return TokenIO.POS_NOT_FOUND;
    }

    /**
     * Wrapper for private getFreeSlot().
     *
     * @param buffer buffer where position is written to
     * @param writeOff offset into buffer
     * @return number of bytes written, i.e. 1
     * @throws ISOException
     */
    short getFreeSlot(byte[] buffer, short writeOff) throws ISOException {
        buffer[writeOff] = getFreeSlot();
        return 1;
    }

    /**
     * Make sure at a specific array position is a key.
     *
     * @param pos position in array to check
     * @throws ISOException
     */
    private void ensureSlotFilled(byte pos) throws ISOException {
        if (pos < 0 || pos > numKeys) {
            ISOException.throwIt(TokenIO.SW_CKEY_SLOT_INVALID);
        }
        if (!clusterKeys[pos].isInitialized()) {
            ISOException.throwIt(TokenIO.SW_CKEY_SLOT_EMPTY);
        }
    }

    /**
     * Make sure at a specific position in the array is no key.
     *
     * @param pos position to check
     * @throws ISOException
     */
    private void ensureSlotEmpty(byte pos) throws ISOException {
        if (pos < 0 || pos > numKeys) {
            ISOException.throwIt(TokenIO.SW_CKEY_SLOT_INVALID);
        }
        if (clusterKeys[pos].isInitialized()) {
            ISOException.throwIt(TokenIO.SW_CKEY_SLOT_IN_USE);
        }
    }

    /**
     * Set id field of a clusterKey for a given position in the key array.
     *
     * @param pos position in array
     * @param buffer buffer where keyId is read from
     * @param readOff offset into buffer
     * @param readLen length of keyId
     * @throws ISOException
     */
    void setId(byte pos, byte[] buffer, short readOff, short readLen)
            throws ISOException {
        ensureSlotEmpty(pos);
        clusterKeys[pos].setId(buffer, readOff, readLen);
    }

    /**
     * Set key field of a clusterKey in array.
     *
     * @param pos position in array
     * @param buffer buffer where keyData is found
     * @param readOff offset into buffer
     * @param readLen length of key
     * @throws ISOException
     */
    void setKey(byte pos, byte[] buffer, short readOff, short readLen)
            throws ISOException {
        ensureSlotEmpty(pos);
        clusterKeys[pos].setKey(buffer, readOff, readLen);
    }

    /**
     * Remve a key from the store.
     *
     * @param pos position of key to remove
     * @throws ISOException
     */
    void remove(byte pos) throws ISOException {
        ensureSlotFilled(pos);
        clusterKeys[pos].clear();
    }

    /**
     * Clear the entire keystore.
     *
     * @throws ISOException
     */
    void clearAll() throws ISOException {
        short i;

        for (i = 0; i < numKeys; i++) {
            if (clusterKeys[i].isInitialized()) {
                clusterKeys[i].clear();
            }
        }
    }

    /**
     * Encrypt data with one of the available keys in the keystore. The position
     * can be matched to a keyId by a previous call to find().
     *
     * @param pos slot number where clusterKey resides
     * @param buffer buffer containing data to be encrypted, output will be
     * written here as well
     * @param readOff offset into buffer where data is to be read
     * @param readLen number of bytes to read
     * @param writeOff offset into buffer where data is to be written
     * @return number of bytes written
     * @throws ISOException
     */
    short encrypt(byte pos, byte[] buffer, short readOff, short readLen,
            short writeOff) throws ISOException {
        ensureSlotFilled(pos);
        return clusterKeys[pos].encrypt(buffer, readOff, readLen, writeOff);
    }

    /**
     * Decrypt data with a clusterKey from the store.
     *
     * @param pos slot number where clusterKey resides
     * @param buffer buffer containing data to be decrypted, output will be
     * written here as well
     * @param readOff offset into buffer where data is to be read
     * @param readLen number of bytes to read
     * @param writeOff offset into buffer where data is to be written
     * @return number of bytes written
     * @throws ISOException
     */
    short decrypt(byte pos, byte[] buffer, short readOff, short readLen,
            short writeOff) throws ISOException {
        ensureSlotFilled(pos);
        return clusterKeys[pos].decrypt(buffer, readOff, readLen, writeOff);
    }

    /**
     * Get a list of all available clusterKey ids.
     *
     * @param buffer buffer where ids are written to
     * @param writeOff write offset into buffer
     * @return number of bytes written
     * @throws ISOException
     */
    short getIds(byte[] buffer, short writeOff) throws ISOException {
        short i, writeLen;

        writeLen = 0;
        for (i = 0; i < numKeys; i++) {
            if (clusterKeys[i].isInitialized()) {
                writeLen = (short) (writeLen + clusterKeys[i].getId(buffer,
                        writeOff));
                writeOff = (short) (writeOff + idLen);
            }
        }

        return writeLen;
    }
}

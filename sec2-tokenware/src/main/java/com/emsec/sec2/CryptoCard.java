package com.emsec.sec2;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.OwnerPIN;
import javacard.security.CryptoException;
import javacard.security.KeyBuilder;
import javacard.security.RandomData;
import javacardx.apdu.ExtendedLength;
import javacardx.framework.util.ArrayLogic;

/*
 * Applet ID: 6D 79 70 61 63 30 30 30 31
 */
public class CryptoCard extends Applet implements ExtendedLength {

    private final static byte[] versionString = {'1', '.', '2'};
    private final static byte PIN_TRY_LIMIT = 3;
    private final static byte PUK_TRY_LIMIT = 3;
    private final static byte CKEY_MAX_NUM = 16;
    private final static short SKEY_SIZE = KeyBuilder.LENGTH_RSA_2048;
    private final static short UKEY_SIZE = KeyBuilder.LENGTH_RSA_2048;
    private final static short CKEY_SIZE = KeyBuilder.LENGTH_AES_256;
    private OwnerPIN cardPIN;
    private OwnerPIN cardPUK;
    private boolean pukIsSet;
    private boolean pinIsSet;
    private ServerKey serverKey;
    private UserKeySigPair userKeySig;
    private UserKeyEncPair userKeyEnc;
    private ClusterKeyStore clusterKeyStore;
    private RandomData rndGen;
    private CRC8 crcGen;

    private CryptoCard() {
        this.rndGen = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);

        this.cardPIN = new OwnerPIN(PIN_TRY_LIMIT, TokenIO.PIN_MAX_SIZE);
        this.cardPUK = new OwnerPIN(PUK_TRY_LIMIT, TokenIO.PUK_MAX_SIZE);
        this.pukIsSet = false;
        this.pinIsSet = false;

        this.cardPIN.update(TokenIO.DEFAULT_PIN, (short) 0,
                (byte) TokenIO.DEFAULT_PIN.length);
        this.cardPUK.update(TokenIO.DEFAULT_PUK, (short) 0,
                (byte) TokenIO.DEFAULT_PUK.length);

        this.serverKey = new ServerKey(SKEY_SIZE);
        this.userKeySig = new UserKeySigPair(UKEY_SIZE);
        this.userKeyEnc = new UserKeyEncPair(UKEY_SIZE);
        this.clusterKeyStore = new ClusterKeyStore(CKEY_MAX_NUM, CKEY_SIZE,
                TokenIO.CKEY_ID_LEN);

        this.crcGen = new CRC8();

        register();
    }

    /**
     * Install routine for JavaCardApplet
     *
     * @param bArray
     * @param bOffset
     * @param bLength
     * @throws ISOException
     */
    public static void install(byte bArray[], short bOffset, byte bLength)
            throws ISOException {
        new CryptoCard();
    }

    /**
     * Standard procedure of applet which needs to be overriden, describes
     * behavior when applet is selected. If no tries are remaining, the applet
     * refuses selection. The card can no longer be used.
     *
     */
    public boolean select() {
        if ((cardPIN.getTriesRemaining() == 0)
                && (cardPUK.getTriesRemaining() == 0)) {
            return false;
        }

        return true;
    }

    /**
     * The command overrides the one from the Applet class. It is used to
     * process the APDU coming from the middleware.
     *
     * @param apdu APDU which will be processed
     */
    public void process(APDU apdu) throws ISOException {
        /*
         * If the applet gets selected, we do nothing..
         */
        if (this.selectingApplet()) {
            return;
        }

        /*
         * Check for correct CLA byte and avoid processing SELECTs
         */
        byte[] buffer = apdu.getBuffer();
        if (buffer[ISO7816.OFFSET_CLA] != TokenIO.CARD_CLA) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        /*
         * The buffer is re-used by us for writing the answer. We want to make
         * sure it is always big enough.
         */
        if (buffer.length < 256) {
            ISOException.throwIt(TokenIO.SW_GENERAL_ERROR);
        }

        short readLen = receiveData(apdu);
        short readOff = apdu.getOffsetCdata();
        short writeLen = -1;
        short writeOff = 0;

        byte P1 = buffer[ISO7816.OFFSET_P1];
        byte P2 = buffer[ISO7816.OFFSET_P2];
        byte INS = buffer[ISO7816.OFFSET_INS];

        /*
         * Check CRC on incoming data. If this does not work out, it throws an
         * exception indicating that the phone should resend the stuff.
         */
        if (computeCRC8(P1, buffer, readOff, readLen) != P2) {
            ISOException.throwIt(TokenIO.SW_GENERAL_TRANSMISSION_ERROR);
        }

        boolean requiresPIN = true;
        boolean requiresPUK = true;

        /*
         * Authenticate PIN/PUK
         */
        switch (INS) {
            case TokenIO.INS_PIN_VALIDATE:
                if (!cardPIN.check(buffer, readOff, (byte) readLen)) {
                    ISOException.throwIt(TokenIO.SW_PIN_VALIDATION_FAILED);
                }
                return;

            case TokenIO.INS_PUK_VALIDATE:
                if (!cardPUK.check(buffer, readOff, (byte) readLen)) {
                    ISOException.throwIt(TokenIO.SW_PUK_VALIDATION_FAILED);
                }
                return;

            case TokenIO.INS_MISC_RND_GET:
                writeLen = P1;
                rndGen.generateData(buffer, writeOff, writeLen);
                break;

            case TokenIO.INS_MISC_VERSION_GET:
                writeLen = (short) versionString.length;
                ArrayLogic.arrayCopyRepack(versionString, (short) 0, writeLen,
                        buffer, writeOff);
                break;

            case TokenIO.INS_MISC_STATUS_GET:
                writeLen = getCardStatus(buffer, writeOff);
                break;
        }

        /*
         * These commands are only accessible if PIN was entered correctly
         */
        if (cardPIN.isValidated()) {
            switch (buffer[ISO7816.OFFSET_INS]) {
                case TokenIO.INS_PIN_LOGOUT:
                    cardPIN.reset();
                    return;

                case TokenIO.INS_UKEYS_SIG_GET_EXP:
                    writeLen = userKeySig.getExponent(buffer, (short) writeOff);
                    break;

                case TokenIO.INS_UKEYS_SIG_GET_MOD:
                    writeLen = userKeySig.getModulus(buffer, (short) writeOff);
                    break;

                case TokenIO.INS_UKEYS_ENC_GET_EXP:
                    writeLen = userKeyEnc.getExponent(buffer, (short) writeOff);
                    break;

                case TokenIO.INS_UKEYS_ENC_GET_MOD:
                    writeLen = userKeyEnc.getModulus(buffer, (short) writeOff);
                    break;

                case TokenIO.INS_UKEYS_SIG_SIGN:
                    writeLen = userKeySig.sign(buffer, readOff, readLen, writeOff);
                    break;

                case TokenIO.INS_CKEY_FIND:
                    writeLen = clusterKeyStore.find(buffer, readOff, readLen,
                            writeOff);
                    break;

                case TokenIO.INS_CKEY_GET_FREE_SLOT:
                    writeLen = clusterKeyStore.getFreeSlot(buffer, writeOff);
                    break;

                case TokenIO.INS_CKEY_SET_ID:
                    clusterKeyStore.setId(P1, buffer, readOff, readLen);
                    return;

                case TokenIO.INS_CKEY_SET_KEY:
                    try {
                        writeLen = userKeyEnc.decrypt(buffer, readOff, readLen, writeOff);
                    } catch (ISOException ex) {
                        /*
                         * Here we make sure that the card does not report
                         * errors in case it receives a badly padded message. We
                         * do this in order to deny padding-oracle attacks.
                         *
                         * Juraj S. says its not necessary to do this when we
                         * use OAEP but .. better safe than sorry? There
                         * probably still is a timing side channel..
                         */
                        if (ex.getReason() == TokenIO.SW_GENERAL_CRYPTO_ERROR
                                + CryptoException.ILLEGAL_USE) {
                            return;
                        } else {
                            /*
                             * Any other error my be thrown.
                             */
                            throw ex;
                        }
                    }
                    clusterKeyStore.setKey(P1, buffer, writeOff, writeLen);
                    return;

                case TokenIO.INS_CKEY_REMOVE:
                    clusterKeyStore.remove(P1);
                    return;

                case TokenIO.INS_CKEY_CLEAR_ALL:
                    clusterKeyStore.clearAll();
                    return;

                case TokenIO.INS_CKEY_ENCRYPT:
                    writeLen = clusterKeyStore.encrypt(P1, buffer, readOff,
                            readLen, writeOff);
                    break;

                case TokenIO.INS_CKEY_DECRYPT:
                    writeLen = clusterKeyStore.decrypt(P1, buffer, readOff,
                            readLen, writeOff);
                    break;

                case TokenIO.INS_CKEY_GET_IDS:
                    writeLen = clusterKeyStore.getIds(buffer, writeOff);
                    break;

                case TokenIO.INS_SKEY_GET_EXP:
                    writeLen = serverKey.getExponent(buffer, writeOff);
                    break;

                case TokenIO.INS_SKEY_GET_MOD:
                    writeLen = serverKey.getModulus(buffer, writeOff);
                    break;

                default:
                    requiresPIN = false;
            }
        }

        /*
         * These commands are only accessible if PUK was entered correctly
         */
        if (cardPUK.isValidated()) {
            switch (buffer[ISO7816.OFFSET_INS]) {
                case TokenIO.INS_PUK_LOGOUT:
                    cardPUK.reset();
                    return;

                case TokenIO.INS_PIN_SET:
                    changePIN(buffer, readOff, readLen);
                    return;

                case TokenIO.INS_PUK_SET:
                    changePUK(buffer, readOff, readLen);
                    return;

                case TokenIO.INS_UKEYS_GENERATE:
                    userKeySig.generate();
                    userKeyEnc.generate();
                    return;

                case TokenIO.INS_SKEY_SET_EXP:
                    serverKey.setExponent(buffer, readOff, readLen);
                    return;

                case TokenIO.INS_SKEY_SET_MOD:
                    serverKey.setModulus(buffer, readOff, readLen);
                    return;

                default:
                    requiresPUK = false;
            }
        }

        /*
         * This area is only reached if:
         *
         * 1. The INS was processed by a command which wants to send something
         * and therefore sets writeLen >= 0.
         *
         * 2. The INS was not processed because PIN or PUK were not validated
         * before sending the INS.
         *
         * 3. The INS was not processed because it is not supported
         */

        if (writeLen >= 0) {
            /*
             * It is also allowed to send empty responses.
             */
            sendAPDU(apdu, writeLen);
        } else if (requiresPUK || requiresPIN) {
            /*
             * We will throw this exception until PIN and PUK both are
             * authenticated which allows us to check all three switch
             * statements.
             */
            ISOException.throwIt(TokenIO.SW_PIN_PUK_AUTHENTICATION_REQUIRED);
        } else if (!requiresPIN && !requiresPUK) {
            /*
             * We are only sure the INS is not supported if we could go through
             * all three switch statements.
             *
             * In case we get an INS which is indeed not supported, but are
             * lacking PIN/PUK we will complain about a lack of proper
             * authentication, although - given the correct PIN/PUK - we could
             * realize that the INS is not supported.
             */
            ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    private short getCardStatus(byte[] buffer, short writeOff) {
        short oldOff = writeOff;

        buffer[writeOff++] = (byte) (pukIsSet ? 0x01 : 0x00);
        buffer[writeOff++] = (byte) (pinIsSet ? 0x01 : 0x00);
        buffer[writeOff++] = (byte) ((userKeySig.isInitialized()
                && userKeyEnc.isInitialized()) ? 0x01 : 0x00);
        buffer[writeOff++] = (byte) (serverKey.isInitialized() ? 0x01 : 0x00);

        return (short) (writeOff - oldOff);
    }

    /**
     * Send an APDU.
     *
     * @param apdu apdu to be sent
     * @param len length of data written to apdu buffer
     * @throws ISOException
     */
    void sendAPDU(APDU apdu, short len) throws ISOException {
        if (len > 256) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        apdu.setOutgoingAndSend((short) 0, len);
    }

    /**
     * This function receives data. It expects normal APDUs and throws an
     * ISOException when it encounters extended-length APDUs. These are
     * currently not supported.
     *
     * @param apdu the APDU to be received and processed
     * @throws ISOException
     */
    short receiveData(APDU apdu) throws ISOException {
        short recvLen = apdu.setIncomingAndReceive();
        short LC = apdu.getIncomingLength();

        if (LC > recvLen) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        return recvLen;
    }

    /**
     * Change PIN. Will only be called when PUK was entered previously.
     *
     * @param buffer buffer containing new PIN
     * @param readOff start of PIN in buffer
     * @param readLen length of PIN in buffer
     */
    void changePIN(byte[] buffer, short readOff, short readLen)
            throws ISOException {
        if (readLen < TokenIO.PIN_MIN_SIZE || readLen > TokenIO.PIN_MAX_SIZE) {
            ISOException.throwIt(TokenIO.SW_PIN_WRONG_SIZE);
        }

        cardPIN.update(buffer, readOff, (byte) readLen);

        /*
         * We don't really care about this, but keep track of it for the sake of
         * returning the PIN status upon request.
         */
        pinIsSet = true;
    }

    /**
     * Change PUK. Will only be called after PUK was entered. Will throw an
     * exception when current PUK is not the TokenIO.DEFAULT_PUK, i.e., the PUK
     * can be set only ONCE in the lifetime of a card! PUK may also not be set
     * to default!
     *
     * @param buffer buffer containing PUK
     * @param readOff start of PUK in buffer
     * @param readLen length of PUK in buffer
     * @throws ISOException
     */
    void changePUK(byte[] buffer, short readOff, short readLen)
            throws ISOException {
        if (pukIsSet) {
            ISOException.throwIt(TokenIO.SW_PUK_SET_ONLY_ONCE);
        }
        if (readLen < TokenIO.PUK_MIN_SIZE || readLen > TokenIO.PUK_MAX_SIZE) {
            ISOException.throwIt(TokenIO.SW_PUK_WRONG_SIZE);
        }

        cardPUK.update(buffer, readOff, (byte) readLen);

        /*
         * PUK is fixed.
         */
        pukIsSet = true;
    }

    private byte computeCRC8(byte P1, byte[] buffer, short readOff, short readLen) {
        byte crc = (byte) 0x00;

        /*
         * Compute CRC over P1|buffer
         */
        crc = crcGen.update(P1, crc);
        crc = crcGen.updateBlock(buffer, readOff, readLen, crc);

        return crc;
    }
}

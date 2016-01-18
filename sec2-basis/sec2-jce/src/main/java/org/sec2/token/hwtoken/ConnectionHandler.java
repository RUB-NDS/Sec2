/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.token.hwtoken;

import java.util.List;
import javax.smartcardio.*;
import org.sec2.token.ReturnCodes;
import org.sec2.token.exceptions.TokenException;

/**
 *
 * @author benedikt
 */
public class ConnectionHandler {
    public final static byte CARD_CLA = (byte) 0x80;

    static final byte[][] APPLET_IDS = {
        {0x6D, 0x79, 0x70, 0x61, 0x63, 0x30, 0x30, 0x30, 0x31},
        {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09}};
    static final String TERMINAL_NAMES[] = {"G & D Secure Flash Card", // LINUX
        "Secure Mobile Card" // WINDOWS
    };
    private CardChannel myChannel;
    private Card myCard;

    public ConnectionHandler() {
        myCard = null;
        myChannel = null;
    }

    /**
     * Function to connect to the MSC
     *
     * @throws TokenException
     */
    public void connect() throws TokenException {
        int i, j;

        try {
            /*
             * Get all available terminals..
             */
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            if (terminals.isEmpty()) {
                throw new TokenException("No terminals found");
            }

            /*
             * Select appropriate terminal
             */
            CardTerminal terminal = null;
            for (i = 0; i < terminals.size(); i++) {
                /*
                 * Try the known terminal names the msc-driver uses..
                 */
                for (j = 0; j < TERMINAL_NAMES.length; j++) {
                    if (terminals.get(i).getName().startsWith(TERMINAL_NAMES[j])) {
                        terminal = terminals.get(i);
                        break;
                    }
                }
            }
            if (terminal == null || !terminal.isCardPresent()) {
                throw new TokenException("No G&D MSC found in terminal(s)");
            }

            /*
             * Establish a connection with the myCard
             */
            myCard = terminal.connect("T=1");
            myChannel = myCard.getBasicChannel();

            /*
             * Of the several AIDs in distribution, try to select one that
             * actually works.. ;)
             */
            boolean appSelected = false;
            for (i = 0; i < APPLET_IDS.length; i++) {
                if (appSelected = selectApplet(APPLET_IDS[i])) {
                    break;
                }
            }
            if (!appSelected) {
                throw new TokenException("Could not select Applet");
            }
        } catch (TokenException e) {
            throw e;
        } catch (Exception e) {
            throw new TokenException("Some exception occured: " + e.toString());
        }
    }

    public void disconnect() throws TokenException {
        try {
            myChannel.close();
            myCard.disconnect(true);

            myCard = null;
            myChannel = null;
        } catch (Exception ex) {
            /*
             * We don't care.
             */
        }
    }

    private boolean selectApplet(byte[] aid) {
        try {
            ResponseAPDU respAPDU = transmitAPDU(
                    (byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, aid);
            if (respAPDU.getSW() != ReturnCodes.SW_SUCCESS) {
                throw new TokenException(respAPDU.getSW());
            }
        } catch (TokenException e) {
            return false;
        }

        return true;
    }

    private ResponseAPDU transmitAPDU(byte CLA, byte INS, byte P1, byte[] data)
            throws TokenException {

        /*
         * In case P2 is not explicitly given, which is the case for all of our
         * functionality, we use P2 as a CRC8Generator checksum over P1||data.
         */
        byte P2 = (byte) 0x00;
        P2 = CRC8Generator.update(P1, P2);
        if (data != null) {
            P2 = CRC8Generator.updateBlock(data, data.length, P2);
        }

        return transmitAPDU(CLA, INS, P1, P2, data);
    }

    private ResponseAPDU transmitAPDU(byte CLA, byte INS, byte P1, byte P2, byte[] data)
            throws TokenException {
        CommandAPDU cmdAPDU;

        if (myCard == null || myChannel == null) {
            throw new TokenException("Not connected");
        }

        if (data != null) {
            cmdAPDU = new CommandAPDU(CLA, INS, P1, P2, data);
        } else {
            cmdAPDU = new CommandAPDU(CLA, INS, P1, P2);
        }

        try {
            return myChannel.transmit(cmdAPDU);
        } catch (javax.smartcardio.CardException e) {
            throw new TokenException(
                    "Error occured while transmitting data to MSC (" + e + ")");
        }
    }

    public byte[] requestData(byte INS) throws TokenException {
        return requestData(INS, (byte) 0x00, null);
    }

    public byte[] requestData(byte INS, byte[] data) throws TokenException {
        return requestData(INS, (byte) 0x00, data);
    }

    public byte[] requestData(byte INS, byte P1) throws TokenException {
        return requestData(INS, P1, null);
    }

    public byte[] requestData(byte INS, byte P1, byte[] data)
            throws TokenException {
        ResponseAPDU respAPDU = transmitAPDU(CARD_CLA, INS, P1, data);

        if (respAPDU.getSW() == ReturnCodes.SW_SUCCESS) {
            return respAPDU.getData();
        }

        throw new TokenException(respAPDU.getSW());
    }

    public void sendData(byte INS, byte[] data) throws TokenException {
        sendData(CARD_CLA, INS, (byte) 0x00, data);
    }

    public void sendData(byte INS) throws TokenException {
        sendData(CARD_CLA, INS, (byte) 0x00, null);
    }

    public void sendData(byte INS, byte P1, byte[] data)
            throws TokenException {
        sendData(CARD_CLA, INS, P1, data);
    }

    public void sendData(byte INS, byte P1)
            throws TokenException {
        sendData(CARD_CLA, INS, P1, null);
    }

    private void sendData(byte CLA, byte INS, byte P1, byte[] data)
            throws TokenException {
        ResponseAPDU respAPDU = transmitAPDU(CLA, INS, P1, data);

        if (respAPDU.getSW() != ReturnCodes.SW_SUCCESS) {
            throw new TokenException(respAPDU.getSW());
        }
    }
}

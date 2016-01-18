package org.sec2.token.softwaretoken;

import org.sec2.token.ReturnCodes;
import org.sec2.token.exceptions.TokenException;
import org.sec2.token.TokenConstants;

public class PinPukTest extends ATokenTest {

    @Override
    public void setUp() {
        try {
            tokenHandler.connect();
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    @Override
    public void tearDown() {
        try {
            tokenHandler.disconnect();
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /**
     * Login with a valid PIN.
     */
    public void testLoginPIN() {
        try {
            tokenHandler.loginPIN(pinCode);
            tokenHandler.logoutPIN();
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /**
     * Login with a valid PUK.
     */
    public void testLoginPUK() {
        try {
            tokenHandler.loginPUK(pukCode);
            tokenHandler.logoutPUK();
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /**
     * Try setting the PIN without authenticating via PUK. This should not work.
     */
    public void testSetNewPINWithoutPUK() {
        try {
            tokenHandler.loginPIN(pinCode);
            tokenHandler.setPIN(pinCode);
            fail("Changing PIN without PUK authentication did not cause an exception");
        } catch (TokenException e) {
            assertEquals(ReturnCodes.SW_PIN_PUK_AUTHENTICATION_REQUIRED, e.getSW());
        }
    }

    /**
     * Change PIN and authenticate via PUK first. Verify that PIN was changed
     * and then change it back to the default.
     */
    public void testSetNewPINWithPUK() {
        byte old = pinCode[0];
        byte[] modifiedPin = pinCode;

        /*
         * Change Pin
         */
        modifiedPin[0] ^= 0xFF;
        try {
            tokenHandler.loginPUK(pukCode);
            tokenHandler.setPIN(modifiedPin);
            tokenHandler.logoutPUK();
        } catch (Exception e) {
            fail(e.toString());
        }

        /*
         * Test new Pin
         */
        try {
            tokenHandler.loginPIN(modifiedPin);
            tokenHandler.logoutPIN();
        } catch (Exception e) {
            fail(e.toString());
        }

        /*
         * Restore old PIN and restore login status
         */
        pinCode[0] = old;
        try {

            tokenHandler.loginPUK(pukCode);
            tokenHandler.setPIN(modifiedPin);
            tokenHandler.logoutPUK();
        } catch (Exception e) {
            fail(e.toString());
        }

        /*
         * Test Old Pin
         */
        try {
            tokenHandler.loginPIN(modifiedPin);
            tokenHandler.logoutPIN();
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /**
     * Try to login with wrong PIN.
     */
    public void testLoginWithWrongPIN() {
        short i;
        byte[] modifiedPin = new byte[pinCode.length];
        for (i = 0; i < pinCode.length; i++) {
            modifiedPin[i] = (byte) (pinCode[i] ^ 0xFF);
        }

        try {
            tokenHandler.loginPIN(modifiedPin);
            fail("Logging in with wrong PIN did not cause an exception");
        } catch (TokenException e) {
            assertEquals(ReturnCodes.SW_PIN_VALIDATION_FAILED, e.getSW());
        }
    }

    /**
     * Try to login with wrong PUK.
     */
    public void testLoginWithWrongPUK() {
        short i;
        byte[] modifiedPuk = new byte[pukCode.length];
        for (i = 0; i < pukCode.length; i++) {
            modifiedPuk[i] = (byte) (pukCode[i] ^ 0xFF);
        }

        try {
            tokenHandler.loginPUK(modifiedPuk);
            fail("Logging in with wrong PUK did not cause an exception");
        } catch (TokenException e) {
            assertEquals(ReturnCodes.SW_PUK_VALIDATION_FAILED, e.getSW());
        }
    }
}

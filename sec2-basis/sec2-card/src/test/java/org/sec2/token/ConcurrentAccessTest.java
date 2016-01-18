package org.sec2.token;

import junit.framework.TestCase;
import org.sec2.token.exceptions.TokenException;
import org.sec2.token.hwtoken.HardwareToken;

/**
 *
 * @author jtemme
 */
public class ConcurrentAccessTest extends TestCase {

    protected final IToken tokenHandler = new HardwareToken();
    protected final byte[] pinCode = TokenConstants.DEFAULT_PIN;
    protected final byte[] pukCode = TokenConstants.DEFAULT_PUK;


    public void testConcurrentLogin() {
        try {
            tokenHandler.connect();
            tokenHandler.loginPIN(pinCode);
            tokenHandler.loginPIN(pinCode);
            tokenHandler.logoutPIN();
            tokenHandler.disconnect();
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testConcurrentConnect() {
        try {
            tokenHandler.connect();
            tokenHandler.connect();
            tokenHandler.disconnect();
        } catch (TokenException ex) {
            fail(ex.toString());
        }
    }

}

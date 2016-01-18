package org.sec2.token.softwaretoken;

import junit.framework.TestCase;
import org.sec2.token.IToken;
import org.sec2.token.TokenConstants;
import org.sec2.token.exceptions.TokenException;
import org.sec2.token.hwtoken.HardwareToken;
import org.sec2.token.swtoken.SoftwareToken;

/**
 *
 * @author jtemme
 */
public class ConcurrentAccessTest extends TestCase {

    protected final IToken tokenHandler = SoftwareToken.getInstance();
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

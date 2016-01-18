package org.sec2.token.keys;

import java.math.BigInteger;

/**
 *
 * @author benedikt
 */
public class UserKey extends APublicKey {
        private static final long serialVersionUID = 1L;

    public UserKey(byte[] mod, byte[] exp) {
        super(mod, exp);
    }
}

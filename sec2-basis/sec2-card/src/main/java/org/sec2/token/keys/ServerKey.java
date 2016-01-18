package org.sec2.token.keys;

/**
 *
 * @author benedikt
 */
public class ServerKey extends APublicKey {
        private static final long serialVersionUID = 1L;

    public ServerKey(byte[] mod, byte[] exp) {
        super(mod, exp);
    }
}

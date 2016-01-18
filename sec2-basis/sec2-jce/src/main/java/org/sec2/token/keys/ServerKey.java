package org.sec2.token.keys;

/**
 *
 * @author benedikt
 */
public class ServerKey extends APublicKey {

    public ServerKey(byte[] mod, byte[] exp) {
        super(mod, exp);
    }
}

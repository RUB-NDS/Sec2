package org.sec2.token.keys;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.PublicKey;
import org.sec2.token.CipherAlgorithm;

/**
 *
 * @author benedikt
 */
abstract class APublicKey implements PublicKey, Serializable{

    public class Modulus extends ByteArray {

        public Modulus(byte[] mod) {
            super(mod);
        }

        public Modulus(BigInteger mod) {
            super(mod);
        }
    }

    public class Exponent extends ByteArray {

        public Exponent(byte[] exp) {
            super(exp);
        }

        public Exponent(BigInteger exp) {
            super(exp);
        }
    }
    private static final String ALGORITHM_NAME = CipherAlgorithm.RSA.name();
    private static final String KEY_FORMAT = "RAW";
    private final Exponent exp;
    private final Modulus mod;

    public APublicKey(byte[] mod, byte[] exp) {
        this.exp = new Exponent(exp);
        this.mod = new Modulus(mod);
    }

    public Modulus getModulus() {
        return mod;
    }

    public Exponent getExponent() {
        return exp;
    }

    @Override
    public String getAlgorithm() {
        return ALGORITHM_NAME;
    }

    @Override
    public String getFormat() {
        return KEY_FORMAT;
    }

    @Override
    public byte[] getEncoded() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

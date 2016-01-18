package org.sec2.token.keys;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;

/**
 *
 * @author benedikt
 */
public class ByteArray implements Serializable{

    private final byte[] byteData;

    public ByteArray(byte[] byteData) {
        this.byteData = Arrays.copyOf(byteData, byteData.length);
    }

    public ByteArray(ByteArray byteData) {
        this.byteData = Arrays.copyOf(byteData.getBytes(), byteData.getBytes().length);
    }

    public ByteArray(String stringData) {
        this.byteData = Arrays.copyOf(stringData.getBytes(), stringData.getBytes().length);
    }

    public ByteArray(BigInteger intData) {
        byte[] complementArray = intData.toByteArray();
        /*
         * Get rid of leading zeros of BigInteger conversion (it's 2-complement
         * representation..)
         */
        this.byteData = Arrays.copyOfRange(complementArray, 1,
                complementArray.length);
    }

    public String toString() {
        return String.format("%0" + (byteData.length << 1) + "X", toBigInteger());
    }

    public BigInteger toBigInteger() {
        return new BigInteger(1, byteData);
    }

    public byte[] getBytes() {
        return byteData;
    }
}

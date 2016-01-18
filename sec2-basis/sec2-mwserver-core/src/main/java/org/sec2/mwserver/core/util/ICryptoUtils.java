package org.sec2.mwserver.core.util;

import java.io.UnsupportedEncodingException;

import javax.crypto.SecretKey;

/**
 * This interface provides utility methods for commonly used cryptographic
 * functions like encoding and decoding in different formats. The interface
 * enables different implementations of the methods. So they can be optimised
 * for different platforms like Android or desktops.
 *
 * @author schuessler
 *
 */
public interface ICryptoUtils
{
    /**
     * This methods encodes an instance of "SecretKey" in a hexadecimal
     * representation and returns it as String.
     *
     * @param key - The instance of "SecretKey" to be encoded in a hexadecimal
     *  representation
     * @param lowercase - TRUE, if the output should be converted into lower
     *  cases, otherwise FALSE
     *
     * @return The hexadecimal representation of the passed instance of
     *  "Secret Key"
     */
    public String encodeSecretKeyAsHex(SecretKey key, boolean lowercase);

    /**
     * This method decodes a base64 encoded string.
     *
     * @param base64String - The base64 encoded string to be decoded
     *
     * @return The decoded string
     */
    public byte[] decodeBase64(String base64String);

    /**
     * This method encodes binary data into its base64 encoded string
     * representation.
     *
     * @param binaryData - The binary data to be base64 encoded
     *
     * @return The base64 encoded binary data
     */
    public String encodeBase64(byte[] binaryData);

    /**
     * This method encodes binary data into its hexadecimal encoded string
     * representation.
     *
     * @param binaryData - The binary data to be hexadecimal encoded
     * @param lowercase - TRUE, if the output should be converted into lower
     *  cases, otherwise FALSE
     *
     * @return The hexadecimal encoded binary data
     */
    public String encodeHex(byte[] binaryData, boolean lowercase);

    /**
     * This method decodes a hexadecimal encoded string to the binary data,
     * which the encoded string represents.
     *
     * @param hex - The hexadecimal encoded string
     *
     * @return The binary data, which were represented by the encoded string.
     *
     * @throws UnsupportedEncodingException Thrown, when an encoding exception
     *  has occured
     */
    public byte[] decodeHex(String hex) throws UnsupportedEncodingException;
}

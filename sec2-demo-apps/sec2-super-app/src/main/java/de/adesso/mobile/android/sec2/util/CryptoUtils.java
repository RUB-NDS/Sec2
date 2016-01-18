package de.adesso.mobile.android.sec2.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Arrays;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 * This class provides some cryptographic utils.
 *
 * @author schuessler
 */
public class CryptoUtils
{
    private static final int HASH_ITERATIONS = 5;
    private static final String HEX_REGEX = "[0-9A-Fa-f]*";
    private static final String ENCODING_ERROR =
            "The variable \"{0}\" is not hexadecimal encoded.";

    private CryptoUtils(){}

    /**
     * Checks, if a password is equal to the hash of a password. The hash of
     * the password has to be hexadecimal encoded.
     *
     * @param password - The password to be checked
     * @param hashedPw - The hash of a password
     *
     * @return TRUE, if H(password) == hashedPw, where H is the hashfunction.
     *  Otherwise FALSE.
     *
     * @throws NoSuchAlgorithmException Thrown, if a non-supported
     *  hash-algorithm was requested
     * @throws UnsupportedEncodingException Thrown, if there was an
     *  encoding-problem
     */
    public static boolean checkLoginPw(String password, String hashedPw)
            throws NoSuchAlgorithmException, UnsupportedEncodingException
            {
        String hashedInputPw = null;

        //The whole validation process must be executed, even if password or
        //hashedPW are NULL or empty, to avoid timing attacs
        if (password == null)
        {
            password = "";
        }
        if (hashedPw == null)
        {
            hashedPw = "";
        }
        //Check, if the hashedPw is hexadecimal encoded
        if (!hashedPw.matches(HEX_REGEX))
        {
            throw new UnsupportedEncodingException(MessageFormat.format(
                    ENCODING_ERROR, "hashedPw"));
        }
        //Calculate hash of the password
        hashedInputPw =
                encodeHex(hashBytes(password.getBytes("UTF-8")), true);

        //if hashedPw is empty, because no login password was stored before, it
        //can be directly compared with the typed in password
        if (hashedPw.isEmpty())
        {
            return password.equals(hashedPw);
        }
        else
        {
            return hashedInputPw.equalsIgnoreCase(hashedPw);
        }
            }

    public static String encodeHex(final byte[] binaryData, final boolean lowercase)
    {
        final String output = new String(Hex.encodeHex(binaryData));

        if (lowercase)
        {
            return output.toLowerCase();
        }
        else
        {
            return output.toUpperCase();
        }
    }

    public static byte[] decodeHex(final String hex)
            throws UnsupportedEncodingException
            {
        if (!hex.matches(HEX_REGEX))
        {
            throw new UnsupportedEncodingException(MessageFormat.format(
                    ENCODING_ERROR, "hex"));
        }

        try
        {
            return Hex.decodeHex(hex.toCharArray());
        }
        catch (final DecoderException de)
        {
            throw new UnsupportedEncodingException(de.getMessage());
        }
            }

    /**
     * Calculates hash of the binary input. The input must not be NULL,
     * otherwise a NullPointerException is thrown.
     *
     * @param input - The input to be hashed.
     *
     * @return The hashed input
     *
     * @throws NoSuchAlgorithmException Thrown, if a non-supported
     *  hash-algorithm was requested
     */
    public static byte[] hashBytes(final byte[] input) throws NoSuchAlgorithmException
    {
        MessageDigest digest = null;
        byte[] inputCopy = null;

        if (input == null)
        {
            throw new NullPointerException(
                    "Variable \"input\" must not be NULL.");
        }
        //Copy input first
        inputCopy = Arrays.copyOf(input, input.length);
        digest = MessageDigest.getInstance("SHA-512");
        digest.reset();
        digest.update(new byte[]{0});
        for (int i = 0; i < HASH_ITERATIONS; i++)
        {
            digest.reset();
            inputCopy = digest.digest(inputCopy);
        }

        return inputCopy;
    }
}

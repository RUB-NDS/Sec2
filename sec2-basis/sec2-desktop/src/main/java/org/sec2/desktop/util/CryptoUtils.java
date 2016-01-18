package org.sec2.desktop.util;

import java.io.UnsupportedEncodingException;

import javax.crypto.SecretKey;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.sec2.mwserver.core.util.ICryptoUtils;

/**
 * This class implements the ICryptoUtils-interface for desktop platforms.
 * 
 * @author schuessler
 */
public class CryptoUtils implements ICryptoUtils
{
    /* (non-Javadoc)
     * @see org.sec2.mwserver.core.util.ICryptoUtils#encodeSecretKeyAsHex(javax.crypto.SecretKey, boolean)
     */
    @Override
    public String encodeSecretKeyAsHex(final SecretKey key, final boolean lowercase)
    {
        return new String(Hex.encodeHex(key.getEncoded(), lowercase));
    }

    /* (non-Javadoc)
     * @see org.sec2.mwserver.core.util.ICryptoUtils#decodeBase64(java.lang.String)
     */
    @Override
    public byte[] decodeBase64(final String base64String)
    {
        return Base64.decodeBase64(base64String);
    }

    /* (non-Javadoc)
     * @see org.sec2.mwserver.core.util.ICryptoUtils#encodeBase64(byte[])
     */
    @Override
    public String encodeBase64(final byte[] binaryData)
    {
        return Base64.encodeBase64String(binaryData);
    }

    @Override
    public String encodeHex(final byte[] binaryData, final boolean lowercase)
    {
        return new String(Hex.encodeHex(binaryData, lowercase));
    }

    @Override
    public byte[] decodeHex(final String hex) throws UnsupportedEncodingException
    {
        if(!hex.matches("[0-9A-Fa-f]*")) throw new UnsupportedEncodingException("The variable \"hex\" is not hexadecimal encoded.");

        try
        {
            return Hex.decodeHex(hex.toCharArray());
        }
        catch(final DecoderException de)
        {
            throw new UnsupportedEncodingException(de.getMessage());
        }
    }
}
package org.sec2.android.util;

import javax.crypto.SecretKey;

import android.test.AndroidTestCase;

/**
 * This JUnit-Class tests the methods of class CryptoUtils.
 * 
 * @author nike
 */
public final class CryptoUtilsTest extends AndroidTestCase
{
    private final TestSecretKey key = new TestSecretKey();
    private final CryptoUtils cryptoUtils = new CryptoUtils();

    /**
     * This method tests the encodeSecretKeyAsHex()-method.
     */
    public void testEncodeSecretKeyAsHex()
    {
        assertEquals("0a0a0a", cryptoUtils.encodeSecretKeyAsHex(key, true));
        assertEquals("0A0A0A", cryptoUtils.encodeSecretKeyAsHex(key, false));
    }

    /**
     * This method tests the decodeBase64()-method.
     */
    public void testDecodeBase64()
    {
        final byte[] base64 = cryptoUtils.decodeBase64("test");

        assertEquals(3, base64.length);
        assertEquals(-75, base64[0]);
        assertEquals(-21, base64[1]);
        assertEquals(45, base64[2]);
    }

    /**
     * This method tests the encodeBase64()-method.
     */
    public void testEncodeBase64()
    {
        byte[] base64 = new byte[]{-75, -21, 45};

        assertEquals("test", cryptoUtils.encodeBase64(base64));
    }

    /**
     * This method tests the checkLoginPw()-method.
     */
    public void testCheckLoginPw()
    {
        try
        {
            assertFalse(cryptoUtils.checkLoginPw("test", "7e57"));
            assertTrue(cryptoUtils.checkLoginPw("test", "ee01cb94f0933c645543d"
                    + "92a488d3552ff9fba7e9455c4a2abc427c44a406dbcdf89a6a954c2"
                    + "e8eba3c6bd555d9d175728cf396a1a37acd31d374f610442c693"));
        }
        catch(Exception e)
        {
            fail();
        }
    }

    /**
     * This method tests the encodeHex()-method.
     */
    public void testEncodeHex()
    {
        final byte[] binaryData = new byte[]{10, 10, 10};

        assertEquals("0a0a0a", cryptoUtils.encodeHex(binaryData, true));
        assertEquals("0A0A0A", cryptoUtils.encodeHex(binaryData, false));
    }

    /**
     * This method tests the decodeHex()-method.
     */
    public void testDecodeHex()
    {
        byte[] binaryData = null;

        try
        {
            binaryData = cryptoUtils.decodeHex("0a0a0a");
            assertEquals(3, binaryData.length);
            for(int i = 0; i < binaryData.length; i++)
            {
                assertEquals(10, binaryData[i]);
            }
        }
        catch(Exception e)
        {
            fail();
        }
    }

    /**
     * This method tests the hashBytes()-method.
     */
    public void testHashBytes()
    {
        final byte[] input = new byte[]{10, 10, 10};
        byte[] output = null;

        try
        {
            output = cryptoUtils.hashBytes(input);
            assertEquals("0e8426450b13f04fc57ca59394ff1931031b2508fed4c0427db0"
                    + "70c5297b312602617505e0c46abe60449d846516f7b9a2c62ce9162"
                    + "0617cacc2cf9c4873bc3a",
                    cryptoUtils.encodeHex(output, true));
        }
        catch(Exception e)
        {
            fail();
        }
    }

    private static final class TestSecretKey implements SecretKey
    {
        private static final long serialVersionUID = 3847677604249227559L;
        private static final byte[] testKey = new byte[]{10, 10, 10};

        @Override
        public byte[] getEncoded()
        {
            return testKey;
        }

        @Override
        public String getAlgorithm()
        {
            return "testAlgorithm";
        }

        @Override
        public String getFormat()
        {
            return "testFormat";
        }
    }
}

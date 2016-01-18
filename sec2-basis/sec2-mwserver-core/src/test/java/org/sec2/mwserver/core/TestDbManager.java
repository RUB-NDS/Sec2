package org.sec2.mwserver.core;

import java.util.Hashtable;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.sec2.persistence.IPersistenceManager;

/**
 * Dummy-implementation for test-purposes.
 * 
 * @author nike
 */
public class TestDbManager implements IPersistenceManager
{
    private static final byte[] KEY = new byte[]{-90, 70, -117, 127, 42,
        12, -127, -74, 59, -69, 99, -86, 33, 85, 53, 94, -62, 75, 108, -7,
        4, 124, 23, 56, -70, -124, -3, -120, 46, -22, 24, -89, 116, -61,
        109, -10, 117, 28, -53, 67, -65, -64, 24, 77, -120, -44, -62, -97,
        89, -49, -42, -117, 75, 15, 11, -19, -89, 57, -13, -49, -98, -96,
        -88, 15};
    private static final String ALGORITHM = "HMACSHA512";
    private static final String FORMAT = "RAW";

    private Hashtable<String, SecretKey> appKeys =
            new Hashtable<String, SecretKey>();

    /* (non-Javadoc)
     * @see org.sec2.persistence.IPersistenceManager#saveAppAuthKey(javax.crypto.SecretKey, java.lang.String)
     */
    @Override
    public boolean saveAppAuthKey(SecretKey key, String appName)
    {
        appKeys.put(appName, key);

        return true;
    }

    /* (non-Javadoc)
     * @see org.sec2.persistence.IPersistenceManager#getAppAuthKey(java.lang.String)
     */
    @Override
    public SecretKey getAppAuthKey(String appName)
    {
        SecretKey key = appKeys.get(appName);

        return key != null ? key : new TestSecretKey();
    }

    /* (non-Javadoc)
     * @see org.sec2.persistence.IPersistenceManager#deleteAppAuthKey(java.lang.String)
     */
    @Override
    public boolean deleteAppAuthKey(String appName)
    {
        //Nothing to do here, Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.sec2.persistence.IPersistenceManager#getRegisteredAppIds()
     */
    @Override
    public String[] getRegisteredAppIds()
    {
        //Nothing to do here, Auto-generated method stub
        return null;
    }

    @Override
    public boolean isNonceInDb(String nonce)
    {
        return false;
    }

    @Override
    public boolean saveNonceInDb(long timestamp, String nonce)
    {
        //Nothing to do here, Auto-generated method stub
        return true;
    }

    @Override
    public boolean deleteOldNonceInDb(long threshold)
    {
        return true;
    }

    private final class TestSecretKey extends SecretKeySpec
    {
        private static final long serialVersionUID = -3569325883119680687L;

        private final String format;

        public TestSecretKey()
        {
            super(KEY, ALGORITHM);
            this.format = FORMAT;
        }

        @Override
        public String getFormat()
        {
            return format;
        }
    }
}

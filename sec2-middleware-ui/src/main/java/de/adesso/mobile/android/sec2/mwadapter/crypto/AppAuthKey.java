package de.adesso.mobile.android.sec2.mwadapter.crypto;

/**
 * Bean-Class for the apps authentication key. Wraps the key and the name of the hash algorithm for which the key was created.
 * @author schuessler
 *
 */
public class AppAuthKey
{
    private String key = null;
    private String algorithm = null;

    /**
     * The constructor for this class, expecting the app-authentication-key and
     * its corresponding algorithm (e.g., Hmac512).
     *
     * @param key - The app-authentication-key
     * @param algorithm - The corresponding algorithm
     */
    public AppAuthKey(String key, String algorithm)
    {
        this.key = key;
        this.algorithm = algorithm;
    }

    /**
     * Returns the app-authentication-key.
     *
     * @return The app-authentication-key
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Sets the app-authentication-key.
     *
     * @param key - The app-authentication-key
     */
    public void setKey(String key)
    {
        this.key = key;
    }

    /**
     * Returns the corresponding algorithm of the key (e.g., Hmac512).
     *
     * @return The corresponding algorithm of the key
     */
    public String getAlgorithm()
    {
        return algorithm;
    }

    /**
     * Sets the corresponding algorithm of the key (e.g., Hmac512).
     * 
     * @param algorithm - The corresponding algorithm of the key
     */
    public void setAlgorithm(String algorithm)
    {
        this.algorithm = algorithm;
    }
}

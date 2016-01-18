package de.adesso.mobile.android.sec2.mwadapter.util;

/**
 * This class holds the keys to get access to the corresponding preference values.
 *
 * @author schuessler
 */
public final class PreferenceKeys
{
    /**
     * The key to access the preference for the port on which the
     * Sec2-middleware is listening on.
     */
    public static final String KEY_MIDDLEWARE_PORT = "middleware_port";

    /**
     * The key to access the preference for the host name of the
     * cloud-storage-service, where data can be stored.
     */
    public static final String KEY_CLOUD_HOST = "cloud_host";

    /**
     * The key to access the preference for the port on which the
     * cloud-storage-service, where data can be stored, is listening on.
     */
    public static final String KEY_CLOUD_PORT = "cloud_port";

    /**
     * The key to access the preference for the location, where data can be
     * be stored on the cloud-storage-service.
     */
    public static final String KEY_CLOUD_LOCATION = "cloud_location";

    private PreferenceKeys(){}
}

package de.adesso.mobile.android.sec2.mwadapter.util;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import de.adesso.mobile.android.sec2.mwadapter.crypto.AppAuthKey;


/**
 * Class for a centralised access to the SharedPreference objects of the MwAdapter library.
 * 
 * @author schuessler
 *
 */
public final class MwAdapterPreferenceManager
{
    private static final String MW_ADAPTER_PREFS = "mwAdapterPrefs";
    private static final String KEY_AUTH_KEY = "auth_key";
    private static final String KEY_AUTH_KEY_ALGORITHM = "auth_key_algorithm";

    private final Context context;

    /**
     * The constructor for this class. It expects the context as parameter.
     *
     * @param context - The context
     */
    public MwAdapterPreferenceManager(final Context context)
    {
        this.context = context;
    }

    /**
     * Returns the App key hex encoded as a String whereas the key may be NULL or empty.
     * @return The App key
     */
    public String getAppAuthKey()
    {
        return context.getSharedPreferences(MW_ADAPTER_PREFS, Context.MODE_PRIVATE).getString(KEY_AUTH_KEY, null);
    }

    /**
     * Saves the App authentication key in the preferences. An existing key is overwritten. Returns FALSE if the passed key object
     * is NULL or if the key couldn't successfully saved. Otherwise TRUE is returned.
     * 
     * @param key - The App authentication key to be saved
     * @return TRUE, if the could successfully be saved, otherwise FALSE
     */
    public boolean saveAppAuthKey(final AppAuthKey key)
    {
        Editor editor = null;

        if(key != null)
        {
            editor = context.getSharedPreferences(MW_ADAPTER_PREFS, Context.MODE_PRIVATE).edit();
            editor.putString(KEY_AUTH_KEY, key.getKey());
            editor.putString(KEY_AUTH_KEY_ALGORITHM, key.getAlgorithm());
            return editor.commit();
        }
        else return false;
    }

    /**
     * Returns the algorithm the app key has to be used for as a string. The algorithm may be NULL or empty.
     * @return The algorithm
     */
    public String getAppAuthKeyAlgorithm()
    {
        return context.getSharedPreferences(MW_ADAPTER_PREFS, Context.MODE_PRIVATE).getString(KEY_AUTH_KEY_ALGORITHM, null);
    }

    /**
     * Returns the port stored in the preferences for the communication with the middleware. The method may throw
     * a NumberFormatException if the stored port can't be parsed to an integer.
     * 
     * @return The port
     */
    public int getMiddlewarePort()
    {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PreferenceKeys.KEY_MIDDLEWARE_PORT, "50001"));
    }

    /**
     * Returns the hostname of the cloud service, which is stored in the preferences. If no hostname was stored, an empty string
     * is returned
     * 
     * @return The hostname of the cloud service
     */
    public String getCloudHostName()
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PreferenceKeys.KEY_CLOUD_HOST, "");
    }

    /**
     * Returns the port stored in the preferences for the communication with the cloud. The method may throw
     * a NumberFormatException if the stored port can't be parsed to an integer.
     * 
     * @return The port
     */
    public int getCloudPort()
    {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PreferenceKeys.KEY_CLOUD_PORT, "80"));
    }

    /**
     * Returns the path to the directory, where the resource is stored within the cloud service. If no path was stored in the preferences,
     * an empty string is returned
     * 
     * @return The directory path within the cloud service
     */
    public String getCloudPath()
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PreferenceKeys.KEY_CLOUD_LOCATION, "");
    }
}

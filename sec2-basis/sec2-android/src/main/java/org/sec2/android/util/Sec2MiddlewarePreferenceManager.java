package org.sec2.android.util;

import org.sec2.android.model.SessionKey;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;


/**
 * Class for a centralised access to the SharedPreference objects of the
 * Sec2-middleware.
 *
 * @author schuessler
 */
public final class Sec2MiddlewarePreferenceManager
{
    private static final String SEC2_MIDDLEWARE_PREFS = "sec2MiddlewarePrefs";
    private static final String KEY_SESSION_KEY = "session_key";
    private static final String KEY_SESSION_KEY_ALGORITHM =
            "session_key_algorithm";
    private static final String KEY_SESSION_KEY_TOKEN = "session_key_token";

    private final Context context;

    /**
     * The constructor for this class.
     *
     * @param context - The context
     */
    public Sec2MiddlewarePreferenceManager(final Context context)
    {
        this.context = context;
    }

    /**
     * Returns the string-representation of the session-key. The key may be
     * NULL or empty.
     *
     * @return The session-key
     */
    public String getSessionKey()
    {
        return context.getSharedPreferences(SEC2_MIDDLEWARE_PREFS,
                Context.MODE_PRIVATE).getString(KEY_SESSION_KEY, null);
    }

    /**
     * Saves the session key in the preferences. An existing key is
     * overwritten. Returns FALSE if the passed key object is NULL or if the
     * key couldn't successfully saved. Otherwise TRUE is returned.
     *
     * @param key - The session key to be saved
     *
     * @return TRUE, if the could successfully be saved, otherwise FALSE
     */
    public boolean saveSessionKey(final SessionKey key)
    {
        Editor editor = null;

        if (key != null)
        {
            editor = context.getSharedPreferences(SEC2_MIDDLEWARE_PREFS,
                    Context.MODE_PRIVATE).edit();
            editor.putString(KEY_SESSION_KEY, key.getKey());
            editor.putString(KEY_SESSION_KEY_ALGORITHM, key.getAlgorithm());
            editor.putString(KEY_SESSION_KEY_TOKEN, key.getSessionToken());
            return editor.commit();
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns the algorithm the session-key has to be used for as a string.
     * The algorithm may be NULL or empty.
     *
     * @return The algorithm
     */
    public String getSessionKeyAlgorithm()
    {
        return context.getSharedPreferences(SEC2_MIDDLEWARE_PREFS,
                Context.MODE_PRIVATE).getString(KEY_SESSION_KEY_ALGORITHM,
                        null);
    }

    /**
     * Returns the session token which is associated with the session key.
     *
     * @return The session token
     */
    public String getSessionToken()
    {
        return context.getSharedPreferences(SEC2_MIDDLEWARE_PREFS,
                Context.MODE_PRIVATE).getString(KEY_SESSION_KEY_TOKEN, null);
    }

    /**
     * Returns the port stored in the preferences, where the
     * Sec2-middleware-server is listening on. The method may throw a
     * NumberFormatException if the stored port can't be parsed to an integer.
     *
     * @return The port
     */
    public int getServerListenPort()
    {
        return Integer.parseInt(
                PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PreferenceKeys.KEY_SERVER_PORT, "50001"));
    }

    /**
     * Returns the hash of the login password encoded in its hexadecimal
     * representation.
     *
     * @return The hash of the login password encoded in its hexadecimal
     *  representation
     */
    public String getHashedLoginPassword()
    {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PreferenceKeys.KEY_LOGIN_PW, "");
    }
}

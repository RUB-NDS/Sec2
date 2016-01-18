package de.adesso.mobile.android.sec2.app;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import de.adesso.mobile.android.sec2.util.LogHelper;

/**
 * Sec2Application
 * @author mschmitz
 */
public class Sec2Application extends Application {

    private static final Class<?> c = Sec2Application.class;
    private static final String APP_NAME = "Sec2-App";

    /**
     * onCreate
     */
    @Override
    public void onCreate() {
        super.onCreate();
        LogHelper.logV(c, "onCreate");
    }

    public String getServer() {
        final SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        return sharedPrefs.getString("exist", "");
    }

    public String getDatabase() {
        final SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        return sharedPrefs.getString("database", "");
    }

    public String getAppName() {
        return APP_NAME;
    }

    public String getPackageNameString() {
        return getPackageManager().getApplicationLabel(getApplicationInfo()).toString();
    }
}

package de.adesso.mobile.android.sec2.app;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.adesso.mobile.android.sec2.util.IconCache;
import de.adesso.mobile.android.sec2.util.LogHelper;

/**
 * Sec2Application
 * @author mschmitz
 */
public class Sec2Application extends Application {

    private static final Class<?> c = Sec2Application.class;
    private static final String APP_NAME = "Sec2-App";
    private static SharedPreferences preferences = null;

    /**
     * onCreate
     */
    @Override
    public void onCreate() {
        super.onCreate();
        IconCache.getInstance().initialize(getApplicationContext());

        LogHelper.logV(c, "onCreate");
    }

    //TODO: Wird das benötigt?
    public String getServer() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPrefs.getString("exist", "");
    }

    //TODO: Wird das benötigt?
    public String getDatabase() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPrefs.getString("database", "");
    }

    public SharedPreferences getPreferences()
    {
    	if(preferences == null)
    		preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	
    	return preferences; 
    }
    
    public String getAppName()
    {
    	return APP_NAME;
    }
}

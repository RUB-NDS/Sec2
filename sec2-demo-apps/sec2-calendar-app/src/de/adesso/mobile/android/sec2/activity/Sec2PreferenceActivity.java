package de.adesso.mobile.android.sec2.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;
import de.adesso.mobile.android.sec2.app.Sec2Application;

public abstract class Sec2PreferenceActivity extends PreferenceActivity {

    protected static final int PREFERENCES = 9310;
    protected Sec2Application app;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = ((Sec2Application) getApplication());
    }
}

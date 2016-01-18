package de.adesso.mobile.android.sec2.activity;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import de.adesso.mobile.android.sec2.app.Sec2Application;

/**
 * Sec2ListActivity
 * @author hoppe
 */
public abstract class Sec2ListActivity extends ListActivity {

    protected static final int PREFERENCES = 9310;
    protected Sec2Application app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = ((Sec2Application) getApplication());
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {}

        if (keyCode == KeyEvent.KEYCODE_BACK) {}

        return super.onKeyDown(keyCode, event);
    }

}

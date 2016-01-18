package de.adesso.mobile.android.sec2.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import de.adesso.mobile.android.sec2.CalendarActivity;
import de.adesso.mobile.android.sec2.EventActivity;
import de.adesso.mobile.android.sec2.ExplorerActivity;
import de.adesso.mobile.android.sec2.NoticeCreateActivity;
import de.adesso.mobile.android.sec2.NoticeListActivity;
import de.adesso.mobile.android.sec2.R;
import de.adesso.mobile.android.sec2.TaskCreateActivity;
import de.adesso.mobile.android.sec2.TaskListActivity;
import de.adesso.mobile.android.sec2.util.LogHelper;

/**
 * Sec2Activity
 * @author hoppe
 */
public abstract class Sec2Activity extends Activity {

    protected static final String KEY_MIDDLEWARE_PORT = "middleware_port";
    protected static final String KEY_AUTH_KEY = "auth_key";
    protected static final String KEY_AUTH_KEY_ALGORITHM = "auth_key_algorithm";
    protected static final int PREFERENCES = 9310;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.logV(this.getClass(), "onCreate()");
    }

    @Override
    protected void onResume() {
        LogHelper.logV(this.getClass(), "onResume()");
        super.onResume();
    }

    @Override
    protected void onPause() {
        LogHelper.logV(this.getClass(), "onPause()");
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.notice_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        menu.findItem(R.id.calendar).setEnabled(!(this instanceof EventActivity));
        menu.findItem(R.id.documents).setEnabled(!(this instanceof ExplorerActivity));
        menu.findItem(R.id.tasks).setEnabled(!(this instanceof TaskCreateActivity));
        menu.findItem(R.id.notices).setEnabled(!(this instanceof NoticeCreateActivity));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.calendar:
                final Intent calendarIntent = new Intent(getApplicationContext(),
                        CalendarActivity.class);
                calendarIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(calendarIntent);
                return true;
            case R.id.documents:
                final Intent documentsIntent = new Intent(getApplicationContext(),
                        ExplorerActivity.class);
                documentsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(documentsIntent);
                return true;
            case R.id.tasks:
                final Intent tasksIntent = new Intent(getApplicationContext(),
                        TaskListActivity.class);
                tasksIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(tasksIntent);
                return true;
            case R.id.notices:
                final Intent noticesIntent = new Intent(getApplicationContext(),
                        NoticeListActivity.class);
                noticesIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(noticesIntent);
                return true;
            case R.id.settings:
                startActivityForResult(new Intent(getApplicationContext(),
                        Sec2PreferenceActivity.class), PREFERENCES);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

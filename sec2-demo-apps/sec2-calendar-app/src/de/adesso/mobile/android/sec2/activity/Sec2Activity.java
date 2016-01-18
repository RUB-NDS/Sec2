package de.adesso.mobile.android.sec2.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import de.adesso.mobile.android.sec2.app.Sec2Application;

/**
 * Sec2Activity
 * @author hoppe
 */
public abstract class Sec2Activity extends Activity {

	protected static final String KEY_MIDDLEWARE_PORT = "middleware_port";
	protected static final String KEY_AUTH_KEY = "auth_key";
	protected static final String KEY_AUTH_KEY_ALGORITHM = "auth_key_algorithm";
	protected static final int PREFERENCES = 9310;
	protected Sec2Application app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = ((Sec2Application) getApplication());
	}

	//    @Override
	//    public boolean onCreateOptionsMenu(Menu menu) {
	//        getMenuInflater().inflate(R.menu.notice_menu, menu);
	//        return super.onCreateOptionsMenu(menu);
	//    }
	//
	//    @Override
	//    public boolean onOptionsItemSelected(MenuItem item) {
	//        switch (item.getItemId()) {
	//            case R.id.calendar:
	//                LogHelper.logV("calendar");
	//                return true;
	//            case R.id.documents:
	//                LogHelper.logV("documents");
	//                startActivity(new Intent(getApplicationContext(), FileChooserActivity.class));
	//                return true;
	//            case R.id.tasks:
	//                LogHelper.logE("tasks");
	//                return true;
	//            case R.id.notices:
	//                LogHelper.logE("notices");
	//                startActivity(new Intent(getApplicationContext(), NoticeListActivity.class));
	//                return true;
	//            case R.id.settings:
	//                LogHelper.logE("settings");
	//                startActivityForResult(new Intent(getApplicationContext(), Preferences1Activity.class), 100);
	//                return true;
	//        }
	//        return super.onOptionsItemSelected(item);
	//    }

	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_SEARCH) {}

		if (keyCode == KeyEvent.KEYCODE_BACK) {}

		return super.onKeyDown(keyCode, event);
	}

}

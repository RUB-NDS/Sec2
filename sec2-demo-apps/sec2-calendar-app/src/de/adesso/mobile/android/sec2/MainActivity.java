package de.adesso.mobile.android.sec2;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import de.adesso.mobile.android.sec2.activity.Sec2Activity;
import de.adesso.mobile.android.sec2.adapter.MainAdapter;
import de.adesso.mobile.android.sec2.model.MainItem;
import de.adesso.mobile.android.sec2.mwadapter.gui.MwAdapterPreferenceActivity;
import de.adesso.mobile.android.sec2.mwadapter.gui.UsersInfoActivity;
import de.adesso.mobile.android.sec2.mwadapter.gui.GroupInfoActivity;
import de.adesso.mobile.android.sec2.util.LogHelper;

public class MainActivity extends Sec2Activity {

	private static final Class<?> c = MainActivity.class;
	private GridView gridView;
	private MainAdapter mainAdapter;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);

		initListeners();
		initGridViewAdapter();
		LogHelper.logV(c, "onCreate");
	}

	@Override
	protected void onResume() {
		LogHelper.logV(c, "onResume");
		super.onResume();
	}

	/**
	 * initListeners
	 */
	private void initListeners() {
		gridView = (GridView) findViewById(R.id.desktop_grid);
	}

	/**
	 * initGridViewAdapter
	 */
	private void initGridViewAdapter() {
		mainAdapter = new MainAdapter(this);

		mainAdapter.add(new MainItem(getString(R.string.menu_calendar), R.drawable.desktop_calendar, null));
		mainAdapter.add(new MainItem(getString(R.string.menu_documents), R.drawable.desktop_documents, new Intent(getApplicationContext(),
				FileChooserActivity.class)));
		mainAdapter.add(new MainItem(getString(R.string.menu_tasks), R.drawable.desktop_tasks, 
				new Intent(getApplicationContext(), TaskListActivity.class)));
		mainAdapter.add(new MainItem(getString(R.string.menu_notices), R.drawable.desktop_notices,
				new Intent(getApplicationContext(), NoticeListActivity.class)));
		mainAdapter.add(new MainItem(getString(R.string.menu_usersinfos), R.drawable.desktop_userinfos, new Intent(getApplicationContext(),
				UsersInfoActivity.class)));
		mainAdapter.add(new MainItem(getString(R.string.menu_groupinfos), R.drawable.desktop_groupinfos, new Intent(getApplicationContext(),
				GroupInfoActivity.class)));
		mainAdapter.add(new MainItem(getString(R.string.menu_settings), R.drawable.desktop_preferences, new Intent(getApplicationContext(),
				MwAdapterPreferenceActivity.class)));

		gridView.setAdapter(mainAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
				final MainItem item = (MainItem) mainAdapter.getItem(position);
				if (item.intent != null) {
					startActivity(item.intent);
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		return false;
	}

}

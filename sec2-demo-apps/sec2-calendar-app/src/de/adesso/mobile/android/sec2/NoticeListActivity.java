package de.adesso.mobile.android.sec2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import de.adesso.mobile.android.sec2.activity.Sec2ListActivity;
import de.adesso.mobile.android.sec2.adapter.NoticeListAdapter;
import de.adesso.mobile.android.sec2.model.NoticeList;
import de.adesso.mobile.android.sec2.model.NoticeListItem;
import de.adesso.mobile.android.sec2.mwadapter.gui.MwAdapterPreferenceActivity;
import de.adesso.mobile.android.sec2.service.Service;
import de.adesso.mobile.android.sec2.util.BundleHelper;
import de.adesso.mobile.android.sec2.util.LogHelper;

/**
 * NoticeActivity
 * @author bruch
 */
public class NoticeListActivity extends Sec2ListActivity {

	private static final Class<?> c = NoticeListActivity.class;

	private ListView lView;
	private NoticeListAdapter nla;

	private String android_id;
	private final int OPEN_NOTE = 1093;

	private List<NoticeListItem> noticeList = new ArrayList<NoticeListItem>();

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.notice);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);

		initTitle();
		initListeners();

		LogHelper.logV(c, "onCreate");
	}

	private void initTitle() {
		findViewById(R.id.titlebar_add).setVisibility(View.VISIBLE);
		lView = (ListView) findViewById(android.R.id.list);
	}

	private void initListeners() {
		((ImageButton) findViewById(R.id.titlebar_add)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {
				final Intent intent = new Intent();
				intent.setClass(NoticeListActivity.this, NoticeCreateActivity.class);
				startActivityForResult(intent, OPEN_NOTE);
			}
		});

		nla = new NoticeListAdapter(this);
		lView.setAdapter(nla);

		lView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
				final Intent noticeCreateIntent = new Intent(NoticeListActivity.this, NoticeCreateActivity.class);
				BundleHelper.bundleNoticeListItem(noticeCreateIntent, nla.getItem(position));
				startActivityForResult(noticeCreateIntent, OPEN_NOTE);
			}
		});
		registerForContextMenu(lView);

		//TODO Exist-Server-Communication
		android_id = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);
		//        loadPreferences();
		new GetXmlFromExistTask(NoticeListActivity.this).execute(app.getServer(), app.getDatabase(), android_id);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.notice_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.calendar:
			LogHelper.logV("calendar");
			return true;
		case R.id.documents:
			LogHelper.logV("documents");
			startActivity(new Intent(getApplicationContext(), FileChooserActivity.class));
			return true;
		case R.id.tasks:
			LogHelper.logE("tasks");
			return true;
		case R.id.notices:
			LogHelper.logE("notices");
			final Intent i = new Intent(getApplicationContext(), NoticeListActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			return true;
		case R.id.settings:
			LogHelper.logE("settings");
			startActivityForResult(new Intent(getApplicationContext(), MwAdapterPreferenceActivity.class), PREFERENCES);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		LogHelper.logE("requestCode: " + requestCode + " resultCode: " + resultCode + " data: " + data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case OPEN_NOTE:
				if (BundleHelper.unbundleNoticeListItem(data.getExtras()) != null) handleOpenNote(BundleHelper.unbundleNoticeListItem(data.getExtras()));
				break;
			case PREFERENCES:
				//                    loadPreferences();
				new GetXmlFromExistTask(NoticeListActivity.this).execute(app.getServer(), app.getDatabase(), android_id);
				break;
			}
		}
	}

	private void handleOpenNote(final NoticeListItem noticeListItem) {
		int i = 0;
		if (noticeListItem.nid < 0) {
			if (noticeList.size() == 0) {
				noticeListItem.nid = 1;
			} else {
				noticeListItem.nid = noticeList.get(noticeList.size() - 1).nid + 1;
			}
		} else {
			while (i < noticeList.size()) {
				if (noticeListItem.nid == noticeList.get(i).nid) {
					noticeList.remove(i);
				} else {
					i++;
				}
			}
		}
		noticeList.add(noticeListItem);
		Collections.sort(noticeList, new NoticeListItemComparator());
		final NoticeList nList = new NoticeList();
		nList.noticeListItem = noticeList;
		new PushtXmlToExistTask(NoticeListActivity.this).execute(nList, app.getServer(), app.getDatabase(), android_id);
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.notice_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		final AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.open:
			final Intent noticeCreateIntent = new Intent(NoticeListActivity.this, NoticeCreateActivity.class);
			BundleHelper.bundleNoticeListItem(noticeCreateIntent, nla.getItem(menuInfo.position));
			startActivityForResult(noticeCreateIntent, OPEN_NOTE);
			return true;
		case R.id.delete:
			noticeList.remove(menuInfo.position);
			nla.clear();
			nla.addAll(noticeList);
			new PushtXmlToExistTask(NoticeListActivity.this).execute(noticeList, app.getServer(), app.getDatabase(), android_id);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	//    private void loadPreferences() {
	//        //        savePreferences();
	//        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	//        server = sharedPrefs.getString("exist", "");
	//        database = sharedPrefs.getString("database", "");
	//        LogHelper.logV("SharedPref: " + server + " " + database);
	//    }

	//    private void savePreferences() {
	//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	//        SharedPreferences.Editor editor = sharedPreferences.edit();
	//        editor.putString("exist", "10.0.2.2");
	//        editor.putString("database", "localhost");
	//        editor.commit();
	//    }

	/**
	 * GetXmlFromExist
	 * @author hoppe
	 */
	private class GetXmlFromExistTask extends Service.GetXmlFromExist {

		@SuppressWarnings ("unused")
		private final Class<?> c = GetXmlFromExistTask.class;

		public GetXmlFromExistTask(final Context context) {
			super(context);
		}

		@Override
		protected void onPostExecuteWithoutException(final List<NoticeListItem> result) {
			super.onPostExecuteWithoutException(result);
			noticeList = result;
			nla.addAll(noticeList);
		}

		@Override
		protected void onPostExecuteWithException(final List<NoticeListItem> result) {
			super.onPostExecuteWithException(result);
			noticeList.clear();
			nla.clear();
			Toast.makeText(getApplicationContext(), "Couldn't connect to Server", Toast.LENGTH_LONG).show();
		}

	}

	/**
	 * PushtXmlToExistTask
	 * @author hoppe
	 */
	private class PushtXmlToExistTask extends Service.PushXmlToExist {

		@SuppressWarnings ("unused")
		private final Class<?> c = GetXmlFromExistTask.class;

		public PushtXmlToExistTask(final Context context) {
			super(context);
		}

		@Override
		protected void onPostExecute(final Boolean result) {
			super.onPostExecute(result);
		}

		@Override
		protected void onPostExecuteWithException(final Boolean result) {
			nla.clear();
			nla.addAll(noticeList);
			nla.notifyDataSetChanged();
			Toast.makeText(getApplicationContext(), "Couldn't connect to Server", Toast.LENGTH_LONG).show();
			super.onPostExecuteWithException(result);
		}

		@Override
		protected void onPostExecuteWithoutException(final Boolean result) {
			nla.clear();
			nla.addAll(noticeList);
			nla.notifyDataSetChanged();
			Toast.makeText(getApplicationContext(), "Upload completed", Toast.LENGTH_LONG).show();
			super.onPostExecuteWithoutException(result);
		}

	}

	/**
	 *  comparator used to sort the list of Items
	 */
	class NoticeListItemComparator implements Comparator<NoticeListItem> {

		@Override
		public int compare(final NoticeListItem object1, final NoticeListItem object2) {
			return (object1.nid < object2.nid ? -1 : (object1.nid == object2.nid ? 0 : 1));
		}
	}

}

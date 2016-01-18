package de.adesso.mobile.android.sec2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.TextAppearanceSpan;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;
import de.adesso.mobile.android.sec2.activity.Sec2Activity;
import de.adesso.mobile.android.sec2.app.Sec2Application;
import de.adesso.mobile.android.sec2.model.EncryptedData;
import de.adesso.mobile.android.sec2.model.Lock;
import de.adesso.mobile.android.sec2.model.NoticeListItem;
import de.adesso.mobile.android.sec2.model.NoticeSelection;
import de.adesso.mobile.android.sec2.mwadapter.gui.MwAdapterPreferenceActivity;
import de.adesso.mobile.android.sec2.util.BundleHelper;
import de.adesso.mobile.android.sec2.util.LogHelper;

/**
 * NoticeCreateActivity
 * @author hoppe
 */
public class NoticeCreateActivity extends Sec2Activity {

	private static final Class<?> c = NoticeCreateActivity.class;
	private NoticeListItem noticeListItem;

	@SuppressWarnings ("unused")
	private Sec2Application app;

	private final ArrayList<SpanFlag> spanFlagList = new ArrayList<SpanFlag>();

	private Bundle bundle;

	private EditText noticecreate_text_top;
	private EditText noticecreate_header_text_top;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.noticecreate);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);

		initTitle();
		initFromBundle();
		initListeners();

		LogHelper.logV(c, "onCreate");
	}

	private void initTitle() {
		noticecreate_text_top = (EditText) findViewById(R.id.noticecreate_text_top);
		noticecreate_header_text_top = (EditText) findViewById(R.id.noticecreate_header_text_top);
		this.registerForContextMenu(noticecreate_text_top);
	}

	private void initFromBundle() {
		bundle = getIntent().getExtras();
		// checking if we are going to modify an existing notice or if we create a new one
		if (bundle != null) {
			LogHelper.logV("Getting Parcel from Bundle");

			noticeListItem = BundleHelper.unbundleNoticeListItem(bundle);

			noticecreate_header_text_top.setText(noticeListItem.subject);
			noticecreate_text_top.setText(noticeListItem.getContent());

			initSpanFlagList(noticeListItem.noticeSelectionList);

			((ToggleButton) findViewById(R.id.noticecreate_lock)).setChecked(noticeListItem.lock == Lock.LOCKED);

			mergeSpanFlag();
			initiateEditTextSpanFlag();

			noticeListItem.noticeSelectionList.clear();
		} else {
			LogHelper.logV("No Parcel inside Bundle");

			noticecreate_header_text_top.setText("");
			noticecreate_text_top.setText("");

			noticeListItem = new NoticeListItem(-1, "", "", 0, new ArrayList<NoticeSelection>());
		}
	}

	private void initSpanFlagList(final ArrayList<NoticeSelection> noticeSelectionList) {
		int currentItem = 0;
		LogHelper.logV("initSpanFlagList with " + noticeSelectionList.size() + " element(s)");
		for (int i = 0; i < noticeSelectionList.size(); i++) {
			if (noticeSelectionList.get(i).encryptedData != null) {
				spanFlagList.add(new SpanFlag(currentItem, currentItem + noticeSelectionList.get(i).section.length()));
				LogHelper.logV("element: " + currentItem + " - " + noticeSelectionList.get(i).section.length());
			}

			currentItem = currentItem + noticeSelectionList.get(i).section.length();
		}
	}

	private void initListeners() {

		noticecreate_text_top.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
				if (count > 0) {
					((ToggleButton) findViewById(R.id.noticecreate_lock)).setChecked(false);
					splitSpanFlag(start, count);
				} else {
					deleteSpanFlag(start, before);
				}
			}

			@Override
			public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {}

			@Override
			public void afterTextChanged(final Editable s) {}
		});
		((Button) findViewById(R.id.noticecreate_save)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {
				preparNoticeItem();

				final Intent noticeCreateIntent = new Intent();
				noticeCreateIntent.setClass(NoticeCreateActivity.this, NoticeListActivity.class);
				BundleHelper.bundleNoticeListItem(noticeCreateIntent, noticeListItem);
				setResult(RESULT_OK, noticeCreateIntent);
				NoticeCreateActivity.this.finish();
			}
		});

		((ToggleButton) findViewById(R.id.noticecreate_lock)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {
				if (((ToggleButton) findViewById(R.id.noticecreate_lock)).isChecked()) {
					encryptText(0, noticecreate_text_top.getText().length());
				} else {
					decryptText(0, noticecreate_text_top.getText().length());
				}

			}
		});

	}

	/**
	 * method to encrypt the selected textarea of the EditText
	 */
	public void encryptText(final int startSelection, final int endSelection) {
		LogHelper.logV("encryptText: " + startSelection + " - " + endSelection);
		if (startSelection != endSelection) {
			encryptTheEditText((startSelection < endSelection ? startSelection : endSelection), (startSelection < endSelection ? endSelection : startSelection));
			spanFlagList.add(new SpanFlag((startSelection < endSelection ? startSelection : endSelection), (startSelection < endSelection ? endSelection
					: startSelection)));
			Collections.sort(spanFlagList, new SpanFlagComparator());
			mergeSpanFlag();
		}
	}

	public void encryptTheEditText(final int startSelection, final int endSelection) {
		final Spannable spannableEditText = noticecreate_text_top.getText();
		LogHelper.logV("encryptTheEditText: " + startSelection + " - " + endSelection);
		spannableEditText.setSpan(new TextAppearanceSpan(getApplicationContext(), R.style.text_red), startSelection, endSelection,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}

	public void spanFlagInfo(final int startSelection, final int endSelection) {
		final Spannable spannableEditText = noticecreate_text_top.getText();
		final TextAppearanceSpan[] editTextAppearance = spannableEditText.getSpans(startSelection, endSelection, TextAppearanceSpan.class);
		LogHelper.logV("Going to remove " + editTextAppearance.length + " item(s)");
		for (final TextAppearanceSpan textAppearanceSpan : editTextAppearance) {
			LogHelper.logE("spanFlagInfo: " + textAppearanceSpan.getTextSize());
		}
	}

	/**
	 * method to decrypt the selected textarea of the EditText
	 */
	public void decryptText(final int startSelection, final int endSelection) {
		if (startSelection != endSelection) {
			decryptTheEditText((startSelection < endSelection ? startSelection : endSelection), (startSelection < endSelection ? endSelection : startSelection));
			removeSpanFlag((startSelection < endSelection ? startSelection : endSelection), (startSelection < endSelection ? endSelection : startSelection));
		}
	}

	public void decryptTheEditText(final int startSelection, final int endSelection) {
		LogHelper.logV("Remove encryption from: " + startSelection + " to " + endSelection);
		final Spannable spannableEditText = noticecreate_text_top.getText();
		final TextAppearanceSpan[] editTextAppearance = spannableEditText.getSpans(startSelection, endSelection, TextAppearanceSpan.class);
		LogHelper.logV("Going to remove " + editTextAppearance.length + " item(s)");
		for (int i = 0; i < editTextAppearance.length; i++) {
			spannableEditText.removeSpan(editTextAppearance[i]);
		}
	}

	public void initiateEditTextSpanFlag() {
		for (int i = 0; i < spanFlagList.size(); i++) {
			((Spannable) noticecreate_text_top.getText()).setSpan(new TextAppearanceSpan(getApplicationContext(), R.style.text_red), spanFlagList.get(i).start,
					spanFlagList.get(i).end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}

	public void removeSpanFlag(final int start, final int end) {
		final ArrayList<SpanFlag> temp = new ArrayList<SpanFlag>();
		int i = 0;
		while (i < spanFlagList.size()) {
			if (spanFlagList.get(i).start >= start && spanFlagList.get(i).end <= (end)) {
				//spanFlag is remove-area
				spanFlagList.remove(i);
			} else {
				if (spanFlagList.get(i).start < start && spanFlagList.get(i).end > end) {
					// delete inner part
					temp.add(new SpanFlag(spanFlagList.get(i).start, start));
					temp.add(new SpanFlag(end, spanFlagList.get(i).end));
					encryptTheEditText(spanFlagList.get(i).start, start);
					encryptTheEditText(end, spanFlagList.get(i).end);
					spanFlagList.remove(i);
				} else {
					if (spanFlagList.get(i).start >= start && spanFlagList.get(i).end > end) {
						// delete left part
						spanFlagList.get(i).start = end;
						encryptTheEditText(end, spanFlagList.get(i).end);
						i++;
					} else {
						if (spanFlagList.get(i).start < start && spanFlagList.get(i).end > start && spanFlagList.get(i).end <= end) {
							// delete right part
							spanFlagList.get(i).end = start;
							encryptTheEditText(spanFlagList.get(i).start, start);
							i++;
						} else {
							i++;
						}
					}
				}
			}
		}
		spanFlagList.addAll(temp);
		Collections.sort(spanFlagList, new SpanFlagComparator());
		mergeSpanFlag();
	}

	/**
	 *  method to split the elements when new text is entered inside the EditText
	 */
	public void splitSpanFlag(final int start, final int count) {
		final ArrayList<SpanFlag> temp = new ArrayList<SpanFlag>();
		int i = 0;
		while (i < spanFlagList.size()) {
			if (spanFlagList.get(i).start >= start) {
				spanFlagList.get(i).start = spanFlagList.get(i).start + count;
				spanFlagList.get(i).end = spanFlagList.get(i).end + count;
				i++;
			} else {
				if (spanFlagList.get(i).start < start && spanFlagList.get(i).end >= (start + count)) {
					decryptTheEditText(start, (start + count));
					encryptTheEditText(spanFlagList.get(i).start, start);
					encryptTheEditText((start + count), (spanFlagList.get(i).end + count));
					temp.add(new SpanFlag(spanFlagList.get(i).start, start));
					temp.add(new SpanFlag((start + count), (spanFlagList.get(i).end + count)));
					spanFlagList.remove(i);
				} else {
					i++;
				}
			}
		}
		spanFlagList.addAll(temp);
		Collections.sort(spanFlagList, new SpanFlagComparator());
	}

	/**
	 *  method to modify existing areas that are currently encrypted
	 */
	public void deleteSpanFlag(final int start, final int before) {
		final ArrayList<SpanFlag> temp = new ArrayList<SpanFlag>();
		int i = 0;
		while (i < spanFlagList.size()) {
			if (spanFlagList.get(i).start >= (start + before)) {
				LogHelper.logE("delete left side");
				// delete left side
				spanFlagList.get(i).start = spanFlagList.get(i).start - before;
				spanFlagList.get(i).end = spanFlagList.get(i).end - before;
				i++;
			} else {
				if (spanFlagList.get(i).start >= start && spanFlagList.get(i).end <= (start + before)) {
					LogHelper.logE("delete all");
					// delete all
					spanFlagList.remove(i);
				} else {
					if (spanFlagList.get(i).start < start && spanFlagList.get(i).end > (start + before)) {
						// delete inner part
						LogHelper.logV("delete inner part");
						temp.add(new SpanFlag(spanFlagList.get(i).start, start));
						temp.add(new SpanFlag((start), (spanFlagList.get(i).end - before)));
						spanFlagList.remove(i);
					} else {
						if (spanFlagList.get(i).start >= start && spanFlagList.get(i).end > (start + before)) {
							// delete left part
							LogHelper.logE("delete left part");
							spanFlagList.get(i).start = start;
							spanFlagList.get(i).end = (spanFlagList.get(i).end - before);
							i++;
						} else {
							if (spanFlagList.get(i).start < start && spanFlagList.get(i).end > start && spanFlagList.get(i).end <= (start + before)) {
								// delete right part
								LogHelper.logE("delete right part");
								spanFlagList.get(i).end = start;
								i++;
							} else {
								i++;
							}
						}
					}
				}
			}
		}
		spanFlagList.addAll(temp);
		Collections.sort(spanFlagList, new SpanFlagComparator());
		mergeSpanFlag();
	}

	/**
	 *  method to merge elements which overlap same textareas
	 */
	public void mergeSpanFlag() {
		int i = 1;
		while (i < spanFlagList.size()) {
			if (spanFlagList.get(i - 1).end > spanFlagList.get(i).end) {
				spanFlagList.remove(i);
			} else {
				if (spanFlagList.get(i - 1).end >= spanFlagList.get(i).start) {
					spanFlagList.get(i - 1).end = spanFlagList.get(i).end;
					spanFlagList.remove(i);
				} else {
					i++;
				}
			}
		}
		int j = 0;
		while (j < spanFlagList.size()) {
			if (spanFlagList.get(j).end == spanFlagList.get(j).start) {
				spanFlagList.remove(j);
			} else {
				j++;
			}
		}
	}

	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		LogHelper.logV("Saving noticeListItem to Database");
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setResult(RESULT_CANCELED);
			NoticeCreateActivity.this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void preparNoticeItem() {
		int start = 0;
		final int end = noticecreate_text_top.getText().length();
		if (spanFlagList.size() > 0) {
			for (int i = 0; i < spanFlagList.size(); i++) {
				if (spanFlagList.get(i).start == start) {
					LogHelper.logE(noticecreate_text_top.getText().toString());
					noticeListItem.noticeSelectionList.add(new NoticeSelection(noticecreate_text_top.getText().toString()
							.substring(spanFlagList.get(i).start, spanFlagList.get(i).end), new EncryptedData()));
					start = spanFlagList.get(i).end;
				} else {
					noticeListItem.noticeSelectionList.add(new NoticeSelection(noticecreate_text_top.getText().toString()
							.substring(start, spanFlagList.get(i).start), null));
					noticeListItem.noticeSelectionList.add(new NoticeSelection(noticecreate_text_top.getText().toString()
							.substring(spanFlagList.get(i).start, spanFlagList.get(i).end), new EncryptedData()));
					start = spanFlagList.get(i).end;
				}
			}
			if (start != end) {
				noticeListItem.noticeSelectionList.add(new NoticeSelection(noticecreate_text_top.getText().toString().substring(start, end), null));
			}
		} else {
			noticeListItem.noticeSelectionList.add(new NoticeSelection(noticecreate_text_top.getText().toString().substring(start, end), null));
		}
		final Calendar calendar = Calendar.getInstance();
		noticeListItem.date = "" + calendar.get(Calendar.DAY_OF_MONTH) + "." + calendar.get(Calendar.MONTH) + "." + calendar.get(Calendar.YEAR);
		noticeListItem.subject = noticecreate_header_text_top.getText().toString();
		if (spanFlagList.size() < 1) {
			noticeListItem.lock = Lock.UNLOCKED;
		} else {
			if (spanFlagList.size() == 1) {
				noticeListItem.lock = Lock.getEnum((spanFlagList.get(0).end == end && spanFlagList.get(0).start == 0 ? 2 : 1));
			} else {
				noticeListItem.lock = Lock.PARTIALLY;
			}
		}
	}

	/**
	 *  additional entries
	 */
	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View view, final ContextMenu.ContextMenuInfo menuInfo) {
		LogHelper.logV("Creating context menu for view=" + view);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.notice_context_menu_encrypt, menu);
		super.onCreateContextMenu(menu, view, menuInfo);
	}

	/**
	 *  functionality additionally added into the contextmenu
	 */
	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_encrypt_some:
			encryptText(noticecreate_text_top.getSelectionStart(), noticecreate_text_top.getSelectionEnd());
			return true;
		case R.id.menu_decrypt_some:
			decryptText(noticecreate_text_top.getSelectionStart(), noticecreate_text_top.getSelectionEnd());
			return true;
		case R.id.menu_encrypt_all:
			encryptText(0, noticecreate_text_top.length());
			return true;
		case R.id.menu_decrypt_all:
			decryptText(0, noticecreate_text_top.length());
			return true;
		case R.id.menu_delete_all:
			spanFlagList.clear();
			noticecreate_text_top.setText("");
			return true;
		}
		return super.onContextItemSelected(item);
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

	/**
	 *  comparator used to sort the list of selectionareas
	 */
	class SpanFlagComparator implements Comparator<SpanFlag> {

		@Override
		public int compare(final SpanFlag object1, final SpanFlag object2) {
			return (object1.start < object2.start ? -1 : (object1.start == object2.start ? 0 : 1));
		}
	}

	/**
	 *  class that currently represents the selectionarea.
	 */
	class SpanFlag {

		int start;
		int end;

		public SpanFlag(final int start, final int end) {
			this.start = start;
			this.end = end;
		}
	}

}

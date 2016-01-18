package de.adesso.mobile.android.sec2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.TextAppearanceSpan;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import de.adesso.mobile.android.sec2.activity.Sec2Activity;
import de.adesso.mobile.android.sec2.model.Lock;
import de.adesso.mobile.android.sec2.model.TaskListItem;
import de.adesso.mobile.android.sec2.model.TaskSelection;
import de.adesso.mobile.android.sec2.util.BundleHelper;
import de.adesso.mobile.android.sec2.util.LogHelper;

/**
 * TaskCreateActivity
 * 
 * @author benner
 */
public class TaskCreateActivity extends Sec2Activity {

	private static final Class<?> c = TaskCreateActivity.class;
	private TaskListItem taskListItem;
	private ArrayList<SpanFlag> spanFlagList = new ArrayList<SpanFlag>();

	private Bundle bundle;
	static final int DUE_DATE_DIALOG_ID = 0;
	static final int REMINDER_DATE_DIALOG_ID = 1;
	static final int REMINDER_TIME_DIALOG_ID = 2;
	static int DATE_DIALOG_ID_SWITCH = 0;
	
	private EditText taskcreate_text_top;
	private EditText taskcreate_header_text_top;
	private CheckBox task_state;
	private EditText due_date;
	private CheckBox reminder;
	private EditText date_reminder;
	private EditText time_reminder;
	// Hilfsvariablen
	private int year;
	private int month;
	private int day;
	private int hour;
	private int minute;
	private DatePickerDialog.OnDateSetListener dateSetListener;
	private TimePickerDialog.OnTimeSetListener timeSetListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.taskcreate);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.titlebar);

		initTitle();

		// getting bundle
		this.bundle = getIntent().getExtras();
		initFromBundle();

		initListeners();

		LogHelper.logV(c, "onCreate");

	}

	@Override
	protected void onResume() {
		LogHelper.logV("onResume");
		super.onResume();
	}

	private void initFromBundle() {
		// checking if we are going to modify an existing task or if we create a
		// new one
		if (bundle != null) {
			// if the element already exists extract the task-element
			LogHelper.logV("Getting Parcel from Bundle");
			// taskListItem = BundleHelper.unbundleNoticeListItem(bundle);
			taskcreate_header_text_top.setText(taskListItem.subject);
			taskcreate_text_top.setText(taskListItem.content);
			LogHelper.logE("Adding " + taskListItem.taskSelectionList.size()
					+ " selections");
			for (int i = 0; i < taskListItem.taskSelectionList.size(); i++) {
				LogHelper.logE("start: "
						+ taskListItem.taskSelectionList.get(i).start
						+ " end: " + taskListItem.taskSelectionList.get(i).end);
				spanFlagList.add(new SpanFlag(taskListItem.taskSelectionList
						.get(i).start,
						taskListItem.taskSelectionList.get(i).end));
			}
			mergeSpanFlag();
			initiateEditTextSpanFlag();
			taskListItem.taskSelectionList.clear();
		} else {
			// if the element doesn't exist create a new task
			LogHelper.logV("No Parcel inside Bundle");
			taskcreate_header_text_top.setText("");
			taskcreate_text_top.setText("");
			findViewById(R.id.label_state).setVisibility(View.GONE);
			findViewById(R.id.task_state).setVisibility(View.GONE);
			// get the current date
			final Calendar c = Calendar.getInstance();
			this.year = c.get(Calendar.YEAR);
			this.month = c.get(Calendar.MONTH);
			this.day = c.get(Calendar.DAY_OF_MONTH);
			this.hour = c.get(Calendar.HOUR_OF_DAY);
			this.minute = c.get(Calendar.MINUTE);
			
			updateDisplay(DUE_DATE_DIALOG_ID);
			// due_date.setText(new
			// SimpleDateFormat("dd-MM-yyyy").format(c.getTime()));

			taskListItem = new TaskListItem(-1, "", "", 0, 1, false, "");
		}
	}

	private void initListeners() {

		taskcreate_text_top.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				LogHelper.logE("onTextChanged: start:" + start + " before:"
						+ before + " count:" + count);
				if (count > 0) {
					taskListItem.content = taskcreate_text_top.getText()
							.toString();
					splitSpanFlag(start, count);
				} else {
					taskListItem.content = taskcreate_text_top.getText()
							.toString();
					deleteSpanFlag(start, before);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		this.dateSetListener = new DatePickerDialog.OnDateSetListener() {

			@Override
			public void onDateSet(DatePicker view, int localYear, int monthOfYear,
					int dayOfMonth) {
				year = localYear;
				month = monthOfYear;
				day = dayOfMonth;
				updateDisplay(DATE_DIALOG_ID_SWITCH);
			}
		};
		
		this.timeSetListener = new TimePickerDialog.OnTimeSetListener() {
			
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int localMinute) {
				hour = hourOfDay;
				minute = localMinute;
				updateDisplay(REMINDER_TIME_DIALOG_ID);
			}
		};
		
		this.reminder.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					findViewById(R.id.reminder_container).setVisibility(View.VISIBLE);
				}else{
					findViewById(R.id.reminder_container).setVisibility(View.GONE);
				}
				
			}
		});
		
		  ((Button) findViewById(R.id.taskcreate_save)).setOnClickListener(new OnClickListener() {

	            @Override
	            public void onClick(View v) {
	                //            addNoticeToDatabase(noticeListItem);
	                //            addAllSelectionToDatabase(noticeListItem);
	                prepareTaskItem();
	                Intent taskCreateIntent = new Intent();
	                taskCreateIntent.setClass(TaskCreateActivity.this, TaskListActivity.class);
	                BundleHelper.bundleTaskListItem(taskCreateIntent, taskListItem);
	                LogHelper.logV(taskListItem.taskSelectionList.toString());
	                setResult(RESULT_OK, taskCreateIntent);
	                TaskCreateActivity.this.finish();
	            }
	        });

	}
	
	 private void prepareTaskItem() {
	        for (int i = 0; i < spanFlagList.size(); i++) {
	            taskListItem.taskSelectionList.add(new TaskSelection(-1, spanFlagList.get(i).start, spanFlagList.get(i).end, taskListItem.nid));
	        }
	        if (bundle != null) {
	            // task already exists. only update the data inside database
	            final Calendar calendar = Calendar.getInstance();
	            taskListItem.date = "" + calendar.get(Calendar.DAY_OF_MONTH) + "." + calendar.get(Calendar.MONTH) + "." + calendar.get(Calendar.YEAR);
	            taskListItem.subject = taskcreate_header_text_top.getText().toString();
	            taskListItem.content = taskcreate_text_top.getText().toString();
	            if (spanFlagList.size() < 1) {
	                taskListItem.lock = Lock.UNLOCKED;
	            } else {
	                if (spanFlagList.size() == 1) {
	                    taskListItem.lock = Lock.getEnum((spanFlagList.get(0).end == taskListItem.content.length() && spanFlagList.get(0).start == 0 ? 2 : 1));
	                } else {
	                	taskListItem.lock = Lock.PARTIALLY;
	                }
	            }
	            LogHelper.logV("" + taskListItem.lock.getType());
	        } else {
	            // task doesn't exist. insert new notice inside database
	            final Calendar calendar = Calendar.getInstance();
	            taskListItem.date = "" + calendar.get(Calendar.DAY_OF_MONTH) + "." + calendar.get(Calendar.MONTH) + "." + calendar.get(Calendar.YEAR);
	            taskListItem.subject = taskcreate_header_text_top.getText().toString();
	            //            noticeListItem.content = noticecreate_text_bottom.getText().toString();
	            taskListItem.content = taskcreate_text_top.getText().toString();
	            if (spanFlagList.size() < 1) {
	                taskListItem.lock = Lock.UNLOCKED;
	            } else {
	                if (spanFlagList.size() == 1) {
	                    taskListItem.lock = Lock.getEnum((spanFlagList.get(0).end == taskListItem.content.length() && spanFlagList.get(0).start == 0 ? 2 : 1));
	                } else {
	                	taskListItem.lock = Lock.PARTIALLY;
	                }
	            }
	            taskListItem.nid = -1;
	            LogHelper.logV("" + taskListItem.lock.getType());
	        }
	    }

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case DUE_DATE_DIALOG_ID:
			return new DatePickerDialog(this, dateSetListener, year, month, day);
		case REMINDER_DATE_DIALOG_ID:
			return new DatePickerDialog(this, dateSetListener, year, month, day);
		case REMINDER_TIME_DIALOG_ID:
			return new TimePickerDialog(this, timeSetListener, hour, minute, true);
		}
		return super.onCreateDialog(id);
	}

	// updates the date in the TextView
	private void updateDisplay(int dialogID) {
		
		switch (dialogID) {
		case DUE_DATE_DIALOG_ID:
			this.due_date.setText(new StringBuilder()
			// Month is 0 based so add 1
			.append(day).append("-").append(month + 1).append("-")
			.append(year).append(" "));
			break;
		case REMINDER_DATE_DIALOG_ID:
			this.date_reminder.setText(new StringBuilder()
			// Month is 0 based so add 1
			.append(day).append("-").append(month + 1).append("-")
			.append(year).append(" "));
			break;
		case REMINDER_TIME_DIALOG_ID:
			this.time_reminder.setText(new StringBuilder()
			.append(hour).append(":").append(minute)+(" "));
			break;
		}
		
	}

	public void showDialog(View v) {
		switch (v.getId()) {
		case R.id.due_date:
			DATE_DIALOG_ID_SWITCH = DUE_DATE_DIALOG_ID;
			showDialog(DUE_DATE_DIALOG_ID);
			break;
		case R.id.date_reminder:
			DATE_DIALOG_ID_SWITCH = REMINDER_DATE_DIALOG_ID;
			showDialog(REMINDER_DATE_DIALOG_ID);
			break;
		case R.id.time_reminder:
			showDialog(REMINDER_TIME_DIALOG_ID);
			break;
		default:
			break;
		}

	}

	private void initTitle() {
		taskcreate_text_top = (EditText) findViewById(R.id.taskcreate_text_top);
		taskcreate_header_text_top = (EditText) findViewById(R.id.taskcreate_header_text_top);
		this.due_date = (EditText) findViewById(R.id.due_date);
		this.date_reminder = (EditText) findViewById(R.id.date_reminder);
		this.time_reminder = (EditText) findViewById(R.id.time_reminder);
		this.registerForContextMenu(taskcreate_text_top);
		this.reminder = (CheckBox) findViewById(R.id.reminder);
		
	}

	/**
	 * method to encrypt the selected textarea of the EditText
	 */
	public void encryptEditText(int startSelection, int endSelection) {
		if (startSelection != endSelection) {
			encryptTheEditText((startSelection < endSelection ? startSelection
					: endSelection),
					(startSelection < endSelection ? endSelection
							: startSelection));
			spanFlagList.add(new SpanFlag(
					(startSelection < endSelection ? startSelection
							: endSelection),
					(startSelection < endSelection ? endSelection
							: startSelection)));
			printSpanFlagList(spanFlagList);
			Collections.sort(spanFlagList, new SpanFlagComparator());
			mergeSpanFlag();
			printSpanFlagList(spanFlagList);
		}
	}

	public void encryptTheEditText(int startSelection, int endSelection) {
		Spannable spannableEditText = (Spannable) taskcreate_text_top.getText();
		LogHelper.logE("Adding " + (endSelection - startSelection) + " spans");
		for (int i = startSelection; i < endSelection; i++) {
			LogHelper.logE("Adding span: " + i + " to " + (i + 1));
			spannableEditText.setSpan(new TextAppearanceSpan(
					getApplicationContext(), R.style.text_red), i, (i + 1),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}

	/**
	 * method to decrypt the selected textarea of the EditText
	 */
	public void decryptEditText(int startSelection, int endSelection) {
		if (startSelection != endSelection) {
			removeSpanFlag((startSelection < endSelection ? startSelection
					: endSelection),
					(startSelection < endSelection ? endSelection
							: startSelection));
			printSpanFlagList(spanFlagList);
			decryptTheEditText((startSelection < endSelection ? startSelection
					: endSelection),
					(startSelection < endSelection ? endSelection
							: startSelection));
		}
	}

	public void decryptTheEditText(int startSelection, int endSelection) {
		LogHelper.logE("Remove encryption from: " + startSelection + " to "
				+ endSelection);
		Spannable spannableEditText = taskcreate_text_top.getText();
		TextAppearanceSpan[] editTextAppearance = spannableEditText.getSpans(
				startSelection, endSelection, TextAppearanceSpan.class);
		LogHelper.logE("Going to remove " + editTextAppearance.length
				+ " item(s)");
		for (int i = 0; i < editTextAppearance.length; i++) {
			spannableEditText.removeSpan(editTextAppearance[i]);
		}
	}

	public void initiateEditTextSpanFlag() {
		for (int i = 0; i < spanFlagList.size(); i++) {
			for (int j = spanFlagList.get(i).start; j < spanFlagList.get(i).end; j++)
				((Spannable) taskcreate_text_top.getText()).setSpan(
						new TextAppearanceSpan(getApplicationContext(),
								R.style.text_red), j, (j + 1),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}

	public void removeSpanFlag(int start, int end) {
		LogHelper.logE("Deleting all between: " + start + " - " + end);
		ArrayList<SpanFlag> temp = new ArrayList<SpanFlag>();
		int i = 0;
		while (i < spanFlagList.size()) {
			if (spanFlagList.get(i).start >= start
					&& spanFlagList.get(i).end <= (end)) {
				// spanFlag is remove-area
				spanFlagList.remove(i);
			} else {
				if (spanFlagList.get(i).start < start
						&& spanFlagList.get(i).end > end) {
					// delete inner part
					temp.add(new SpanFlag(spanFlagList.get(i).start, start));
					temp.add(new SpanFlag(end, spanFlagList.get(i).end));
					spanFlagList.remove(i);
				} else {
					if (spanFlagList.get(i).start >= start
							&& spanFlagList.get(i).end > end) {
						// delete left part
						LogHelper.logE("delete left part");
						spanFlagList.get(i).start = end;
						i++;
					} else {
						if (spanFlagList.get(i).start < start
								&& spanFlagList.get(i).end > start
								&& spanFlagList.get(i).end <= end) {
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
		spanFlagList.addAll(temp);
		Collections.sort(spanFlagList, new SpanFlagComparator());
		mergeSpanFlag();
		printSpanFlagList(spanFlagList);
	}

	/**
	 * method to split the elements when new text is entered inside the EditText
	 */
	public void splitSpanFlag(int start, int count) {
		ArrayList<SpanFlag> temp = new ArrayList<SpanFlag>();
		int i = 0;
		while (i < spanFlagList.size()) {
			if (spanFlagList.get(i).start >= start) {
				spanFlagList.get(i).start = spanFlagList.get(i).start + count;
				spanFlagList.get(i).end = spanFlagList.get(i).end + count;
				i++;
			} else {
				if (spanFlagList.get(i).start < start
						&& spanFlagList.get(i).end >= (start + count)) {
					temp.add(new SpanFlag(spanFlagList.get(i).start, start));
					temp.add(new SpanFlag((start + count),
							(spanFlagList.get(i).end + count)));
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
	 * method to modify existing areas that are currently encrypted
	 */
	public void deleteSpanFlag(int start, int before) {
		for (int y = 0; y < spanFlagList.size(); y++) {
			LogHelper.logV("ALLBefore - Start: " + spanFlagList.get(y).start
					+ " End: " + spanFlagList.get(y).end);
		}
		LogHelper.logE("Deleting all between: " + start + " - " + before);
		ArrayList<SpanFlag> temp = new ArrayList<SpanFlag>();
		int i = 0;
		while (i < spanFlagList.size()) {
			if (spanFlagList.get(i).start >= (start + before)) {
				LogHelper.logE("delete left side");
				// delete left side
				spanFlagList.get(i).start = spanFlagList.get(i).start - before;
				spanFlagList.get(i).end = spanFlagList.get(i).end - before;
				i++;
			} else {
				if (spanFlagList.get(i).start >= start
						&& spanFlagList.get(i).end <= (start + before)) {
					LogHelper.logE("delete all");
					// delete all
					spanFlagList.remove(i);
				} else {
					if (spanFlagList.get(i).start < start
							&& spanFlagList.get(i).end > (start + before)) {
						// delete inner part
						LogHelper.logV("delete inner part");
						temp.add(new SpanFlag(spanFlagList.get(i).start, start));
						temp.add(new SpanFlag((start),
								(spanFlagList.get(i).end - before)));
						spanFlagList.remove(i);
					} else {
						if (spanFlagList.get(i).start >= start
								&& spanFlagList.get(i).end > (start + before)) {
							// delete left part
							LogHelper.logE("delete left part");
							spanFlagList.get(i).start = start;
							spanFlagList.get(i).end = (spanFlagList.get(i).end - before);
							i++;
						} else {
							if (spanFlagList.get(i).start < start
									&& spanFlagList.get(i).end > start
									&& spanFlagList.get(i).end <= (start + before)) {
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
		for (int y = 0; y < spanFlagList.size(); y++) {
			LogHelper.logV("ALL - Start: " + spanFlagList.get(y).start
					+ " End: " + spanFlagList.get(y).end);
		}
	}

	/**
	 * method to merge elements which overlap same textareas
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

	// @Override
	// public void onConfigurationChanged(Configuration newConfig) {
	// super.onConfigurationChanged(newConfig);
	// setContentView(R.layout.taskcreate);
	// initTitle();
	// initListeners();
	// }

	/**
	 * BackButton is currently used to save modification on a task. Probably
	 * need a better way to save
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		LogHelper.logV("Saving taskListItem to Database");
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// addNoticeToDatabase(taskListItem);
			// addAllSelectionToDatabase(taskListItem);
			// preparNoticeItem();
			// Intent noticeCreateIntent = new Intent();
			// noticeCreateIntent.setClass(TaskCreateActivity.this,
			// NoticeListActivity.class);
			// BundleHelper.bundleNoticeListItem(noticeCreateIntent,
			// taskListItem);
			// LogHelper.logV(taskListItem.noticeSelectionList.toString());
			// setResult(RESULT_OK, noticeCreateIntent);
			// TaskCreateActivity.this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	

	private void printSpanFlagList(ArrayList<SpanFlag> spanFlagList) {
		LogHelper.logV("spanFlagList:");
		for (int i = 0; i < spanFlagList.size(); i++) {
			LogHelper.logE("start: " + spanFlagList.get(i).start + " end: "
					+ spanFlagList.get(i).end);
		}
	}

	/**
	 * additional entries
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenu.ContextMenuInfo menuInfo) {
		LogHelper.logV("Creating context menu for view=" + view);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.notice_context_menu_encrypt, menu);
		super.onCreateContextMenu(menu, view, menuInfo);
	}

	/**
	 * functionality additionally added into the contextmenu
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		LogHelper.logV("Context item selected as=" + item.toString());
		LogHelper.logV("Context item selected as=" + item.getItemId());
		switch (item.getItemId()) {
		case R.id.menu_encrypt_some:
			encryptEditText(taskcreate_text_top.getSelectionStart(),
					taskcreate_text_top.getSelectionEnd());
			return true;
		case R.id.menu_decrypt_some:
			decryptEditText(taskcreate_text_top.getSelectionStart(),
					taskcreate_text_top.getSelectionEnd());
			return true;
		case R.id.menu_encrypt_all:
			encryptEditText(0, taskcreate_text_top.length());
			return true;
		case R.id.menu_decrypt_all:
			decryptEditText(0, taskcreate_text_top.length());
			return true;
		case R.id.menu_delete_all:
			spanFlagList.clear();
			taskcreate_text_top.setText("");
			return true;
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * comparator used to sort the list of selectionareas
	 */
	class SpanFlagComparator implements Comparator<SpanFlag> {

		@Override
		public int compare(SpanFlag object1, SpanFlag object2) {
			return (object1.start < object2.start ? -1
					: (object1.start == object2.start ? 0 : 1));
		}
	}

	/**
	 * class that currently represents the selectionarea. will be exchanged with
	 * NoticeSelection
	 */
	class SpanFlag {

		int start;
		int end;

		public SpanFlag(int start, int end) {
			this.start = start;
			this.end = end;
		}
	}

}

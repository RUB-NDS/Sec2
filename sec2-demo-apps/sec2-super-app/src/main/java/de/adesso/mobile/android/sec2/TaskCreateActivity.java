
package de.adesso.mobile.android.sec2;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.ToggleButton;
import de.adesso.mobile.android.sec2.activity.Sec2Activity;
import de.adesso.mobile.android.sec2.model.Lock;
import de.adesso.mobile.android.sec2.model.Priority;
import de.adesso.mobile.android.sec2.model.Task;
import de.adesso.mobile.android.sec2.mwadapter.PutRequestProperties;
import de.adesso.mobile.android.sec2.mwadapter.util.MwAdapterPreferenceManager;
import de.adesso.mobile.android.sec2.service.Service;
import de.adesso.mobile.android.sec2.util.CheckedGroupHandler;
import de.adesso.mobile.android.sec2.util.Constants;
import de.adesso.mobile.android.sec2.util.SpanFlag;
import de.adesso.mobile.android.sec2.util.SpanFlagHelper;
import de.adesso.mobile.android.sec2.util.TaskDomDocumentCreator;

/**
 * TaskCreateActivity
 * 
 * @author benner/hoppe
 */
public class TaskCreateActivity extends Sec2Activity {

    private static final int RESULT_ID = 1095;

    private static final int NO_KEY_OR_ALGORITHM = 0;
    private static final int ERROR = 1;
    private static final int SUCCESS = 2;

    private EditText mInputArea;
    private EditText mSubject;
    private CheckBox mState;
    private EditText mDueDate;
    private CheckBox mReminder;
    private EditText mReminderDate;
    private EditText mReminderTime;
    private ToggleButton mLock;
    private Spinner mPriority;

    private SpanFlagHelper mSpanFlagHelper;

    private TaskDomDocumentCreator mTaskCreator = null;
    private boolean mNewTask = true;
    private String mErrorMessage = "";

    private Calendar mCalendarDueDate = null;
    private Calendar mCalendarReminder = null;

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("dd/MM/yyyy",
            Locale.getDefault());
    private final SimpleDateFormat mTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.taskcreate);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);

        initViewElements();
        initFromBundle();
        initListeners();
    }

    private void initViewElements() {
        mInputArea = (EditText) findViewById(R.id.taskcreate_text_top);
        mSubject = (EditText) findViewById(R.id.taskcreate_header_text_top);
        mDueDate = (EditText) findViewById(R.id.due_date);
        mReminderDate = (EditText) findViewById(R.id.date_reminder);
        mReminderTime = (EditText) findViewById(R.id.time_reminder);
        mReminder = (CheckBox) findViewById(R.id.reminder);
        mState = (CheckBox) findViewById(R.id.task_state);
        mLock = (ToggleButton) findViewById(R.id.taskcreate_lock);
        mPriority = (Spinner) findViewById(R.id.spinner_priority);

        registerForContextMenu(mInputArea);
    }

    private void updateDateFields() {
        mDueDate.setText(mDateFormat.format(mCalendarDueDate.getTime()));
        if (mReminder.isChecked()) {
            mReminderDate.setText(mDateFormat.format(mCalendarReminder.getTime()));
            mReminderTime.setText(mTimeFormat.format(mCalendarReminder.getTime()));
        }
    }

    private void initFromBundle() {
        mSpanFlagHelper = new SpanFlagHelper(TaskCreateActivity.this, mInputArea);

        mCalendarDueDate = Calendar.getInstance();
        mCalendarReminder = Calendar.getInstance();

        mTaskCreator = (TaskDomDocumentCreator) (getIntent()
                .getSerializableExtra(Constants.INTENT_EXTRA_TASK));
        // checking if we are going to modify an existing event or if we create a new one
        if (mTaskCreator != null && mTaskCreator.getTask() != null) {
            final Task task = mTaskCreator.getTask();

            mNewTask = false;
            mSubject.setText(task.getSubject());
            mCalendarDueDate = task.getDueDate();
            mState.setChecked(task.getIsDone());
            mPriority.setSelection(task.getPriority().getType());
            mLock.setChecked(mTaskCreator.getTask().getLock() == Lock.LOCKED);
            if (task.getReminderDate() != null) {
                mCalendarReminder = task.getReminderDate();
                mReminder.setChecked(true);
                findViewById(R.id.reminder_container).setVisibility(View.VISIBLE);
            }

            mInputArea.setText(getInputAreaText());

            mSpanFlagHelper.initiateText();
        } else {
            mSubject.setText("");
            mInputArea.setText("");
        }

        updateDateFields();
    }

    private String getInputAreaText() {
        final Task task = mTaskCreator.getTask();
        final StringBuilder displayedText = new StringBuilder(task.getTaskText());
        SpanFlag spanFlag = null;
        final int numberOfSpanFlags = (task.getPartsToEncrypt() != null ? task.getPartsToEncrypt()
                .size()
                : 0);
        int position = 0;

        for (int i = 0; i < numberOfSpanFlags; i++) {
            position = displayedText.indexOf(Task.PLACE_HOLDER, position);
            if (position >= 0) {
                displayedText.replace(position, position + Task.PLACE_HOLDER.length(),
                        task.getPartsToEncrypt().get(i));
                spanFlag = new SpanFlag(position, position
                        + task.getPartsToEncrypt().get(i).length());
                mSpanFlagHelper.addSpanFlag(spanFlag);
                position = spanFlag.end;
            }
        }

        return displayedText.toString();
    }

    @SuppressLint("NewApi")
    private void initListeners() {
        mInputArea.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before,
                    final int count) {
                if (count > 0) {
                    mLock.setChecked(false);
                    mSpanFlagHelper.splitSpanFlag(start, count);
                } else {
                    mSpanFlagHelper.deleteSpanFlag(start, before);
                }
            }

            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count,
                    final int after) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
            }
        });

        if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
            mInputArea.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

                @Override
                public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(final ActionMode mode) {
                }

                @Override
                public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
                    mode.setTitle("");
                    menu.clear();
                    final MenuInflater inflater = getMenuInflater();
                    inflater.inflate(R.menu.event_context_menu, menu);
                    return true;
                }

                @Override
                public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_encrypt:
                            mSpanFlagHelper.encrypt(mInputArea.getSelectionStart(),
                                    mInputArea.getSelectionEnd());
                            return true;
                        case R.id.menu_decrypt:
                            mSpanFlagHelper.decrypt(mInputArea.getSelectionStart(),
                                    mInputArea.getSelectionEnd());
                            return true;
                        default:
                            return false;
                    }
                }
            });
        } else {
            registerForContextMenu(mInputArea);
        }

        mLock.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                if (mLock.isChecked()) {
                    mSpanFlagHelper.encryptAll();
                } else {
                    mSpanFlagHelper.decryptAll();
                }

                if (!mInputArea.isFocused()) {
                    mInputArea.requestFocus();
                }

            }
        });

        mReminder.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                findViewById(R.id.reminder_container).setVisibility(
                        isChecked ? View.VISIBLE : View.GONE);
                updateDateFields();
            }
        });

    }

    public final void showDatePickerDialog(final EditText editText, final Calendar calendar) {
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            private boolean mChange = true;

            @Override
            public void onDateSet(final DatePicker view, final int year, final int monthOfYear,
                    final int dayOfMonth) {
                // @see http://code.google.com/p/android/issues/detail?id=34833
                // onDateSet() is bugged in Android 4.1+. it will be called twice.
                if (mChange) {
                    mChange ^= true;
                    calendar.set(year, monthOfYear, dayOfMonth);
                    updateDateFields();
                }

            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    public final void showTimePickerDialog(final EditText editText, final Calendar calendar) {
        new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(final TimePicker view, final int hourOfDay, final int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                updateDateFields();
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.due_date:
                showDatePickerDialog(mDueDate, mCalendarDueDate);
                break;
            case R.id.date_reminder:
                if (mCalendarReminder == null) {
                    mCalendarReminder = Calendar.getInstance();
                }
                showDatePickerDialog(mReminderDate, mCalendarReminder);
                break;
            case R.id.time_reminder:
                if (mCalendarReminder == null) {
                    mCalendarReminder = Calendar.getInstance();
                }
                showTimePickerDialog(mReminderTime, mCalendarReminder);
                break;
            case R.id.taskcreate_save:
                saveTask();
                break;
            default:
                break;
        }
    }

    @Override
    protected Dialog onCreateDialog(final int id) {

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(R.string.task_create_success_title);

        switch (id) {
            case SUCCESS:
                alertDialogBuilder.setMessage(R.string.task_create_success_true);
                alertDialogBuilder.setPositiveButton(android.R.string.ok, listenerResultOk);
                break;
            case NO_KEY_OR_ALGORITHM:
                alertDialogBuilder.setMessage(R.string.task_create_success_false_key);
                alertDialogBuilder.setPositiveButton(android.R.string.ok, listenerResultCanceled);
                break;
            case ERROR:
                alertDialogBuilder.setMessage(getString(R.string.task_create_success_false_error)
                        + mErrorMessage);
                alertDialogBuilder.setPositiveButton(android.R.string.ok, listenerResultCanceled);
                break;
            default:
                break;
        }
        return alertDialogBuilder.create();

    }

    private final DialogInterface.OnClickListener listenerResultOk = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            TaskCreateActivity.this.finish();
        }
    };

    private final DialogInterface.OnClickListener listenerResultCanceled = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            setResult(Activity.RESULT_CANCELED);
        }
    };

    /**
     * additional entries
     */
    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View view,
            final ContextMenu.ContextMenuInfo menuInfo) {
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
                mSpanFlagHelper.encrypt(mInputArea.getSelectionStart(),
                        mInputArea.getSelectionEnd());
                return true;
            case R.id.menu_decrypt_some:
                mSpanFlagHelper.decrypt(mInputArea.getSelectionStart(),
                        mInputArea.getSelectionEnd());
                return true;
            case R.id.menu_encrypt_all:
                mSpanFlagHelper.encryptAll();
                return true;
            case R.id.menu_decrypt_all:
                mSpanFlagHelper.decryptAll();
                return true;
            case R.id.menu_delete_all:
                mSpanFlagHelper.clear();
                mInputArea.setText("");
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        String xmlDocument = null;

        switch (resultCode) {
            case Activity.RESULT_OK:
                xmlDocument = data.getStringExtra(Constants.INTENT_EXTRA_RESULT);
                if (xmlDocument != null && !xmlDocument.isEmpty()) {
                    saveTask(xmlDocument);
                } else {
                    setResult(Activity.RESULT_CANCELED);
                }
                break;
            case Activity.RESULT_CANCELED:
                setResult(Activity.RESULT_CANCELED);
                break;
            default:
                break;
        }
    }

    private void saveTask() {
        final int spanFlagCount = mSpanFlagHelper.getCount();
        final List<String> partsToEncrypt = new ArrayList<String>();
        final StringBuilder sb = new StringBuilder(mInputArea.getText().toString());
        final Intent intent = new Intent();
        Task task = new Task();
        String documentName = null;
        SpanFlag spanFlag = null;
        int offset = 0;

        final String subject = mSubject.getText().toString();

        if (mNewTask) {
            mTaskCreator = new TaskDomDocumentCreator();
            task.setCreationDate(new GregorianCalendar());
            documentName = "task_" + subject + "_" + task.getCreationDate().getTimeInMillis()
                    + ".xml";
            documentName = documentName.replaceAll("\\s", "_"); // Ersetze Leerstellen durch "_"
            mTaskCreator.setDocumentName(documentName);
            task.setFilename(documentName);
        } else {
            task = mTaskCreator.getTask();
        }

        task.setDueDate(mCalendarDueDate);
        task.setIsDone(mState.isChecked());
        task.setLock(mSpanFlagHelper.determineLockStatus());
        // TODO: @hoppe: (26.02.2013) might need a change in future
        task.setPriority(Priority
                .getEnum(getResources().getIntArray(R.array.priority_values)[((Spinner) findViewById(R.id.spinner_priority))
                        .getSelectedItemPosition()]));
        if (mReminder.isChecked()) {
            task.setReminderDate(mCalendarReminder);
        } else {
            task.setReminderDate(null);
        }
        task.setSubject(subject);

        for (int i = 0; i < spanFlagCount; i++) {
            spanFlag = mSpanFlagHelper.getSpanFlag(i);
            final String subString = sb.substring(spanFlag.start - offset, spanFlag.end - offset);
            partsToEncrypt.add(subString);
            sb.replace(spanFlag.start - offset, spanFlag.end - offset, Task.PLACE_HOLDER);
            offset += subString.length() - Task.PLACE_HOLDER.length();
        }
        task.setTaskText(sb.toString());
        task.setPartsToEncrypt(partsToEncrypt);
        mTaskCreator.setTask(task);

        if (spanFlagCount > 0) {
            intent.setClass(this, GroupChooserActivity.class);
            intent.putExtra(Constants.INTENT_EXTRA_DOM, mTaskCreator);
            startActivityForResult(intent, RESULT_ID);
        }
        // If there is nothing to encrypt send the XML document directly
        else {
            try {

                final String taskDocument = new String(mTaskCreator.createDomDocument(
                        new CheckedGroupHandler()).getBytes("UTF-8"), "UTF-8");
                saveTask(taskDocument);
            } catch (final Exception e) {
                if (e.getMessage() != null) {
                    mErrorMessage = e.getMessage();
                } else {
                    mErrorMessage = "";
                }
                showDialog(ERROR);
                setResult(Activity.RESULT_CANCELED);
            }
        }
    }

    private void saveTask(final String xmlContent) {
        final MwAdapterPreferenceManager prefManager = new MwAdapterPreferenceManager(
                TaskCreateActivity.this);
        final String key = prefManager.getAppAuthKey();
        final String algorithm = prefManager.getAppAuthKeyAlgorithm();
        final String taskName = mTaskCreator.getDocumentName();
        String path = null;
        PutRequestProperties requestProperties = null;
        PutRequestToUrlTask task = null;

        if (key != null && algorithm != null) {
            path = prefManager.getCloudPath();
            if (path.isEmpty() || path.endsWith("/")) {
                requestProperties = new PutRequestProperties(prefManager.getCloudHostName(), path
                        + taskName, prefManager.getCloudPort(), xmlContent);
            } else {
                requestProperties = new PutRequestProperties(prefManager.getCloudHostName(), path
                        + "/" + taskName, prefManager.getCloudPort(), xmlContent);
            }
            task = new PutRequestToUrlTask(TaskCreateActivity.this, taskName, key, algorithm,
                    getApplication().getPackageName(), prefManager.getMiddlewarePort(),
                    requestProperties);
            task.execute();
        } else {
            showDialog(NO_KEY_OR_ALGORITHM);
            setResult(Activity.RESULT_CANCELED);
        }
    }

    private class PutRequestToUrlTask extends Service.PutRequestToUrl {

        private final String mTaskName;

        public PutRequestToUrlTask(final Activity activity, final String taskName,
                final String key, final String algorithm, final String appName, final int port,
                final PutRequestProperties properties) {
            super(activity, key, algorithm, appName, port, properties, activity
                    .getString(R.string.service_task));
            mTaskName = taskName;
        }

        @Override
        protected void onPostExecuteWithoutException(final InputStream result) {
            super.onPostExecuteWithoutException(result);
            if (mNewTask) {
                showDialog(SUCCESS);
                final Intent resultIntent = new Intent(TaskCreateActivity.this,
                        TaskListActivity.class);
                resultIntent.putExtra(Constants.INTENT_EXTRA_RESULT, mTaskCreator);
                setResult(Activity.RESULT_OK, resultIntent);
            } else {
                showDialog(SUCCESS);
                final Intent resultIntent = new Intent();
                resultIntent.putExtra(Constants.INTENT_EXTRA_RESULT, mTaskCreator);
                setResult(Activity.RESULT_OK, resultIntent);
            }
        }

        @Override
        protected void onPostExecuteWithException(final InputStream result) {
            super.onPostExecuteWithException(result);
            if (mException.getMessage() != null) {
                mErrorMessage = mException.getMessage();
            } else {
                mErrorMessage = "";
            }
            showDialog(ERROR);
        }

    }

}

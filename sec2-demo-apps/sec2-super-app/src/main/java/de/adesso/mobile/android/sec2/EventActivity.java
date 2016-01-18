
package de.adesso.mobile.android.sec2;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import nu.style.LimitedRangeDatePickerDialog;
import nu.style.LimitedRangeTimePickerDialog;
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
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.ToggleButton;
import de.adesso.mobile.android.sec2.activity.Sec2Activity;
import de.adesso.mobile.android.sec2.dialog.TaskProgressDialog;
import de.adesso.mobile.android.sec2.model.Event;
import de.adesso.mobile.android.sec2.model.Lock;
import de.adesso.mobile.android.sec2.mwadapter.PutRequestProperties;
import de.adesso.mobile.android.sec2.mwadapter.model.User;
import de.adesso.mobile.android.sec2.mwadapter.util.MwAdapterPreferenceManager;
import de.adesso.mobile.android.sec2.service.Service;
import de.adesso.mobile.android.sec2.util.CheckedGroupHandler;
import de.adesso.mobile.android.sec2.util.Constants;
import de.adesso.mobile.android.sec2.util.EventDomDocumentCreator;
import de.adesso.mobile.android.sec2.util.SpanFlag;
import de.adesso.mobile.android.sec2.util.SpanFlagHelper;

public final class EventActivity extends Sec2Activity {

    private static final int RESULT_ID = 1095;

    private static final int NO_KEY_OR_ALGORITHM = 0;
    private static final int ERROR = 1;
    private static final int SUCCESS = 3;

    private EditText mDateFromEt, mDateToEt;
    private CheckBox mCheckBox;
    private Calendar mDateFrom, mDateTo;
    private final SimpleDateFormat mSdfShort = new SimpleDateFormat("dd-MM-yyyy",
            Locale.getDefault());
    private final SimpleDateFormat mSdfLong = new SimpleDateFormat("dd-MM-yyyy HH:mm",
            Locale.getDefault());

    private ToggleButton mLock;
    private EditText mInputArea;
    private EditText mEventSubject;
    private EditText mEventLocation;
    private Button mEventAttendees;
    private Spinner mEventRepeat;
    private Spinner mEventReminder;

    private String mErrorMessage = "";

    private SpanFlagHelper mSpanFlagHelper;

    private EventDomDocumentCreator mEventCreator = null;
    private boolean mNewEvent = true;

    private User[] mKnownUsers;
    private SparseBooleanArray mSelectedItems;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.event);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);

        initViewElements();
        initFromBundle();
        initListeners();

        new GetAllUsersTask(this).execute();
    }

    private void initViewElements() {
        mDateFromEt = (EditText) findViewById(R.id.event_from_et);
        mDateToEt = (EditText) findViewById(R.id.event_to_et);
        mCheckBox = (CheckBox) findViewById(R.id.even_cb_whole_day);

        mInputArea = (EditText) findViewById(R.id.event_et);
        mLock = ((ToggleButton) findViewById(R.id.event_lock));
        mEventSubject = (EditText) findViewById(R.id.event_subject);
        mEventLocation = (EditText) findViewById(R.id.event_location_et);
        mEventAttendees = (Button) findViewById(R.id.event_attendees_btn);

        mEventRepeat = (Spinner) findViewById(R.id.event_repeat_sp);
        mEventReminder = (Spinner) findViewById(R.id.event_reminder_sp);
    }

    private void initFromBundle() {
        mSpanFlagHelper = new SpanFlagHelper(EventActivity.this, mInputArea);

        mEventCreator = (EventDomDocumentCreator) (getIntent()
                .getSerializableExtra(Constants.INTENT_EXTRA_EVENT));
        // checking if we are going to modify an existing event or if we create a new one
        if (mEventCreator != null && mEventCreator.getEvent() != null) {
            mNewEvent = false;

            final Event event = mEventCreator.getEvent();

            mEventSubject.setText(event.getSubject());
            mDateFrom = event.getBegin();
            mDateTo = event.getEnd();
            mCheckBox.setChecked(event.isWholeDay());
            mEventLocation.setText(event.getLocation());
            mEventRepeat.setSelection(Integer.valueOf(event.getEventRepeatRate()));
            mEventReminder.setSelection(Integer.valueOf(event.getReminder()));
            mInputArea.setText(getInputAreaText());
            mLock.setChecked(event.getLock() == Lock.LOCKED);

            mSpanFlagHelper.initiateText();
        } else {
            mEventSubject.setText("");
            mInputArea.setText("");
            mDateFrom = Calendar.getInstance();
            mDateTo = Calendar.getInstance();
            mEventAttendees.setText(String.format(getString(R.string.event_attendees_selected), 0));
        }
        updateDateFields();
    }

    private String getInputAreaText() {
        final Event event = mEventCreator.getEvent();
        final StringBuilder displayedText = new StringBuilder(event.getEventText());
        SpanFlag spanFlag = null;
        final int numberOfSpanFlags = (event.getPartsToEncrypt() == null ? 0 : event
                .getPartsToEncrypt().size());
        int position = 0;

        for (int i = 0; i < numberOfSpanFlags; i++) {
            position = displayedText.indexOf(Event.PLACE_HOLDER, position);
            if (position >= 0) {
                displayedText.replace(position, position + Event.PLACE_HOLDER.length(),
                        event.getPartsToEncrypt().get(i));
                spanFlag = new SpanFlag(position, position
                        + event.getPartsToEncrypt().get(i).length());
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
                    final MenuInflater inflater = mode.getMenuInflater();
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
    }

    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.event_from_et:
            case R.id.event_button_from:
                showDatePickerDialog(mDateFromEt, mDateFrom, Picker.FROM);
                break;
            case R.id.event_to_et:
            case R.id.event_button_to:
                showDatePickerDialog(mDateToEt, mDateTo, Picker.TO);
                break;
            case R.id.event_button_upload:
                saveEvent();
                break;
            case R.id.even_cb_whole_day:
                updateDateFields();
                break;
            case R.id.event_attendees_btn:
                showAttendeesDialog();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        String xmlDocument = null;

        switch (resultCode) {
            case Activity.RESULT_OK:
                xmlDocument = data.getStringExtra(Constants.INTENT_EXTRA_RESULT);
                if (xmlDocument != null && !xmlDocument.isEmpty()) {
                    saveEvent(xmlDocument);
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

    private void saveEvent() {
        final int spanFlagCount = mSpanFlagHelper.getCount();
        // final String[] partsToEncrypt = new String[spanFlagCount];
        final List<String> partsToEncrypt = new ArrayList<String>();
        final StringBuilder sb = new StringBuilder(mInputArea.getText().toString());
        final Intent intent = new Intent();
        Event event = null;
        String documentName = null;
        SpanFlag spanFlag = null;
        int offset = 0;

        final String subject = mEventSubject.getText().toString();
        if (mNewEvent) {
            mEventCreator = new EventDomDocumentCreator();
            event = new Event();
            event.setCreationDate(new GregorianCalendar());
            documentName = "event_" + subject + "_" + event.getCreationDate().getTimeInMillis()
                    + ".xml";
            documentName = documentName.replaceAll("\\s", "_"); // Replace space with "_"
            mEventCreator.setDocumentName(documentName);
        } else {
            event = mEventCreator.getEvent();
        }

        event.setSubject(subject);
        event.setLock(mSpanFlagHelper.determineLockStatus());
        event.setBegin(mDateFrom);
        event.setEnd(mDateTo);
        event.setWholeDay(mCheckBox.isChecked());
        event.setLocation(mEventLocation.getText().toString());

        if (mSelectedItems != null) {
            final StringBuilder sb2 = new StringBuilder();
            for (int i = 0; i < mSelectedItems.size(); i++) {
                if (mSelectedItems.get(mSelectedItems.keyAt(i))) {
                    sb2.append(mKnownUsers[mSelectedItems.keyAt(i)].getUserId()
                            + (mSelectedItems.size() - 1 != i ? "," : ""));
                }
            }
            event.setParticipants(sb2.toString());
        }

        event.setEventRepeatRate(String.valueOf(mEventRepeat.getSelectedItemPosition()));
        event.setReminder(String.valueOf(mEventReminder.getSelectedItemPosition()));

        for (int i = 0; i < spanFlagCount; i++) {
            spanFlag = mSpanFlagHelper.getSpanFlag(i);
            final String subString = sb.substring(spanFlag.start - offset, spanFlag.end - offset);
            partsToEncrypt.add(subString);
            sb.replace(spanFlag.start - offset, spanFlag.end - offset, Event.PLACE_HOLDER);
            offset += subString.length() - Event.PLACE_HOLDER.length();
        }
        event.setEventText(sb.toString());
        event.setPartsToEncrypt(partsToEncrypt);
        mEventCreator.setEvent(event);

        if (spanFlagCount > 0) {
            intent.setClass(this, GroupChooserActivity.class);
            intent.putExtra(Constants.INTENT_EXTRA_DOM, mEventCreator);
            startActivityForResult(intent, RESULT_ID);
        }
        // If there is nothing to encrypt the XML document can be send directly
        else {
            try {
                final String eventDocument = new String(mEventCreator.createDomDocument(
                        new CheckedGroupHandler()).getBytes("UTF-8"), "UTF-8");
                saveEvent(eventDocument);
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

    private void saveEvent(final String xmlContent) {
        final MwAdapterPreferenceManager prefManager = new MwAdapterPreferenceManager(
                EventActivity.this);
        final String key = prefManager.getAppAuthKey();
        final String algorithm = prefManager.getAppAuthKeyAlgorithm();
        final String eventName = mEventCreator.getDocumentName();
        String path = null;
        PutRequestProperties requestProperties = null;
        PutRequestToUrlTask task = null;

        if (key != null && algorithm != null) {
            path = prefManager.getCloudPath();
            if (path.isEmpty() || path.endsWith("/")) {
                requestProperties = new PutRequestProperties(prefManager.getCloudHostName(), path
                        + eventName, prefManager.getCloudPort(), xmlContent);
            } else {
                requestProperties = new PutRequestProperties(prefManager.getCloudHostName(), path
                        + "/" + eventName, prefManager.getCloudPort(), xmlContent);
            }
            task = new PutRequestToUrlTask(EventActivity.this, eventName, key, algorithm,
                    getApplication().getPackageName(), prefManager.getMiddlewarePort(),
                    requestProperties);
            task.execute();
        } else {
            showDialog(NO_KEY_OR_ALGORITHM);
            setResult(Activity.RESULT_CANCELED);
        }
    }

    private void updateDateFields() {
        mDateFromEt.setText((mCheckBox.isChecked() ? mSdfShort : mSdfLong).format(mDateFrom
                .getTime()));
        mDateToEt.setText((mCheckBox.isChecked() ? mSdfShort : mSdfLong).format(mDateTo.getTime()));
    }

    private enum Picker {
        FROM, TO;
    }

    public void showDatePickerDialog(final EditText editText, final Calendar calendar,
            final Picker picker) {
        new LimitedRangeDatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            boolean change = true;

            @Override
            public void onDateSet(final DatePicker view, final int year, final int monthOfYear,
                    final int dayOfMonth) {
                // @see http://code.google.com/p/android/issues/detail?id=34833
                // onDateSet() is bugged in Android 4.1+. it will be called twice.
                if (change) {
                    change ^= true;
                    calendar.set(year, monthOfYear, dayOfMonth);
                    if (picker.equals(Picker.FROM)
                            && calendar.getTimeInMillis() > mDateTo.getTimeInMillis()) {
                        mDateTo.setTime(calendar.getTime());
                        mDateToEt.setText((mCheckBox.isChecked() ? mSdfShort : mSdfLong)
                                .format(mDateTo.getTime()));
                    }
                    if (mCheckBox.isChecked()) {
                        editText.setText(mSdfShort.format(calendar.getTime()));
                    } else {
                        showTimePickerDialog(editText, calendar, picker);
                    }
                }

            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), picker.equals(Picker.TO) ? mDateFrom : null,
                null).show();
    }

    public void showTimePickerDialog(final EditText editText, final Calendar calendar,
            final Picker picker) {
        new LimitedRangeTimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(final TimePicker view, final int hourOfDay, final int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                if (picker.equals(Picker.FROM)
                        && calendar.getTimeInMillis() > mDateTo.getTimeInMillis()) {
                    mDateTo.setTime(calendar.getTime());
                    updateDateFields();
                } else {
                    editText.setText(mSdfLong.format(calendar.getTime()));
                }

            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true,
                picker.equals(Picker.TO) ? mDateFrom : null, null, calendar).show();
    }

    private void showAttendeesDialog() {
        if (mKnownUsers != null) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(EventActivity.this);
            final String[] knownUsersList = new String[mKnownUsers.length];

            // TODO: @hoppe: (04.04.2013) debug. switch with getUserName() when it goes live.
            // getUserName() will display "[Kein Eintrag gefunden]" when the selected user is not in
            // the
            // address book
            for (int i = 0; i < mKnownUsers.length; i++) {
                knownUsersList[i] = mKnownUsers[i].getUserName();// getUserEmail();//
                                                                 // .getUserName();
            }

            boolean[] selectedItems = null;
            if (mSelectedItems != null) {
                selectedItems = new boolean[mKnownUsers.length];
                for (int i = 0; i < mSelectedItems.size(); i++) {
                    if (mSelectedItems.get(mSelectedItems.keyAt(i))) {
                        selectedItems[mSelectedItems.keyAt(i)] = true;
                    }
                }
            }
            builder.setMultiChoiceItems(knownUsersList, selectedItems,
                    new DialogInterface.OnMultiChoiceClickListener() {

                        @Override
                        public void onClick(final DialogInterface dialog, final int which,
                                final boolean isChecked) {
                        }
                    });

            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(final DialogInterface dialog) {
                    mSelectedItems = ((AlertDialog) dialog).getListView().getCheckedItemPositions();
                    int count = 0;
                    for (int i = 0; i < mSelectedItems.size(); i++) {
                        if (mSelectedItems.get(mSelectedItems.keyAt(i))) {
                            count++;
                        }
                    }
                    mEventAttendees.setText(String.format(
                            getString(R.string.event_attendees_selected),
                            count));
                }
            });

            builder.create().show();
        }
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(RESULT_CANCELED);
            EventActivity.this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     *  additional entries
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
    protected Dialog onCreateDialog(final int id) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(R.string.event_create_success_title);

        switch (id) {
            case SUCCESS:
                alertDialogBuilder.setMessage(R.string.event_create_success_true);
                alertDialogBuilder.setPositiveButton(android.R.string.ok, listenerResultOk);
                break;
            case NO_KEY_OR_ALGORITHM:
                alertDialogBuilder.setMessage(R.string.event_create_success_false_key);
                alertDialogBuilder.setPositiveButton(android.R.string.ok, listenerResultCanceled);
                break;
            case ERROR:
                alertDialogBuilder.setMessage(getString(R.string.event_create_success_false_error)
                        + mErrorMessage);
                alertDialogBuilder.setPositiveButton(android.R.string.ok, listenerResultCanceled);
                break;
        }

        return alertDialogBuilder.create();
    }

    private final DialogInterface.OnClickListener listenerResultOk = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            EventActivity.this.finish();
        }
    };

    private final DialogInterface.OnClickListener listenerResultCanceled = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            setResult(Activity.RESULT_CANCELED);
        }
    };

    private class PutRequestToUrlTask extends Service.PutRequestToUrl {

        private final String eventName;

        public PutRequestToUrlTask(final Activity activity, final String eventName,
                final String key, final String algorithm, final String appName, final int port,
                final PutRequestProperties properties) {
            super(activity, key, algorithm, appName, port, properties, activity
                    .getString(R.string.service_event));
            this.eventName = eventName;
        }

        @Override
        protected void onPostExecuteWithoutException(final InputStream result) {
            super.onPostExecuteWithoutException(result);
            if (mNewEvent) {
                showDialog(SUCCESS);
                final Intent resultIntent = new Intent(EventActivity.this,
                        CalendarActivity.class);
                resultIntent.putExtra(Constants.INTENT_EXTRA_RESULT, mEventCreator);
                setResult(Activity.RESULT_OK, resultIntent);
            } else {
                showDialog(SUCCESS);
                final Intent resultIntent = new Intent();
                resultIntent.putExtra(Constants.INTENT_EXTRA_RESULT, mEventCreator);
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

    private class GetAllUsersTask extends Service.GetAllUsersTask {

        public GetAllUsersTask(final Activity activity) {
            super(activity, new TaskProgressDialog(activity));
        }

        @Override
        protected void onPostExecuteWithoutException(final User[] result) {
            super.onPostExecuteWithoutException(result);
            if (result != null) {
                mKnownUsers = result;
                if (mEventCreator != null) {
                    mSelectedItems = new SparseBooleanArray();
                    final String[] attendeesArray = mEventCreator.getEvent().getAttendeesIdList();
                    if (attendeesArray != null && attendeesArray.length > 0) {
                        for (int i = 0; i < mKnownUsers.length; i++) {
                            for (int j = 0; j < attendeesArray.length; j++) {
                                if (mKnownUsers[i].getUserId().equalsIgnoreCase(attendeesArray[j])) {
                                    mSelectedItems.append(i, true);
                                }
                            }

                        }
                        mEventAttendees
                                .setText(String.format(
                                        getString(R.string.event_attendees_selected),
                                        mSelectedItems.size()));
                    } else {
                        mEventAttendees.setText(String.format(
                                getString(R.string.event_attendees_selected), 0));
                    }
                }
            }
        }

    }

}

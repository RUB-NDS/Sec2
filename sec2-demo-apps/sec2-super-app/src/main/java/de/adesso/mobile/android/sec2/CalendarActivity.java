
package de.adesso.mobile.android.sec2;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import de.adesso.mobile.android.sec2.activity.Sec2ListActivity;
import de.adesso.mobile.android.sec2.adapter.CalendarListAdapter;
import de.adesso.mobile.android.sec2.adapter.CalendarViewAdapter;
import de.adesso.mobile.android.sec2.dialog.TaskProgressDialog;
import de.adesso.mobile.android.sec2.model.Event;
import de.adesso.mobile.android.sec2.model.Lock;
import de.adesso.mobile.android.sec2.mwadapter.DeleteRequestProperties;
import de.adesso.mobile.android.sec2.mwadapter.util.MwAdapterPreferenceManager;
import de.adesso.mobile.android.sec2.service.Service;
import de.adesso.mobile.android.sec2.util.AlertHelper;
import de.adesso.mobile.android.sec2.util.Constants;
import de.adesso.mobile.android.sec2.util.EventDomDocumentCreator;
import de.adesso.mobile.android.sec2.util.MonthSwitcher;

public final class CalendarActivity extends Sec2ListActivity {

    private static final String TAG = "CalendarActivity";

    private static final int NO_KEY_OR_ALGORITHM = 0;
    private static final int ERROR = 1;
    private static final int SUCCESS = 2;

    private static final int VIEW_EVENT = 9312;
    private static final int CREATE_EVENT = 9311;

    private ListView mListView;
    private CalendarListAdapter mCalendarListAdapter;
    private CalendarViewAdapter mCalendarViewAdapter;
    private TextView mCalendarDate;

    private String mErrorMessage = null;

    private Calendar mMonth;
    private final SimpleDateFormat mDateFormat = new SimpleDateFormat(
            "MMMM yyyy", Locale.getDefault());

    /*
     * (non-Javadoc)
     * @see de.adesso.mobile.android.sec2.activity.Sec2ListActivity#onCreate(android .os.Bundle)
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.calendar);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.titlebar);

        initViewElements();
        initListeners();
        initCalendar();
    }

    /**
     * method to initialize the view elements
     */
    private void initViewElements() {
        mListView = (ListView) findViewById(android.R.id.list);
        mCalendarDate = (TextView) findViewById(R.id.calendar_date);
    }

    /**
     * method to initialize the calendar
     */
    private void initCalendar() {
        mMonth = Calendar.getInstance();
        mCalendarDate.setText(mDateFormat.format(mMonth.getTime()));
        mCalendarViewAdapter = new CalendarViewAdapter(this, mMonth);
        final GridView calendarGridview = (GridView) findViewById(R.id.gridview);
        calendarGridview.setAdapter(mCalendarViewAdapter);

        calendarGridview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> parent, final View v,
                    final int position, final long id) {
                List<EventDomDocumentCreator> tempList = (List<EventDomDocumentCreator>) mCalendarViewAdapter
                        .getItem(position);

                switch (tempList.size()) {
                    case 0:
                        break;
                    case 1:
                        showDetailViewForEvent(tempList.get(0));
                        break;

                    default:
                        showEventPickerDialog(tempList);
                        break;
                }
            }
        });

    }

    private List<EventDomDocumentCreator> createDummyEvents() {
        List<EventDomDocumentCreator> events = new ArrayList<EventDomDocumentCreator>();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());

        Event event1 = new Event();
        Calendar event1Calendar1 = (Calendar) calendar.clone();
        event1Calendar1.set(Calendar.DAY_OF_MONTH,
                event1Calendar1.get(Calendar.DAY_OF_MONTH) + 1);
        Calendar event1Calendar2 = (Calendar) calendar.clone();
        event1Calendar2.set(Calendar.DAY_OF_MONTH,
                event1Calendar2.get(Calendar.DAY_OF_MONTH) + 1);
        event1.setBegin(event1Calendar1);
        event1.setEnd(event1Calendar2);
        event1.setEventText("Event Text");
        event1.setLocation("Location");
        event1.setLock(Lock.UNLOCKED);
        event1.setSubject("Betreff");
        event1.setWholeDay(true);
        event1.setParticipants("");
        event1.setEventRepeatRate(String.valueOf(0));
        event1.setReminder(String.valueOf(0));

        EventDomDocumentCreator eventDomDocumentCreator = new EventDomDocumentCreator(
                event1.getSubject(), event1);

        events.add(eventDomDocumentCreator);

        return events;
    }

    /**
     * method to refresh the current calendar when an user interaction occured
     */

    /**
     * method to initialize listener on the view elements
     */
    private void initListeners() {
        findViewById(R.id.titlebar_add).setVisibility(View.VISIBLE);
        ((ImageButton) findViewById(R.id.titlebar_add))
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(final View v) {
                        final Intent intent = new Intent();
                        intent.setClass(CalendarActivity.this,
                                EventActivity.class);
                        startActivityForResult(intent, CREATE_EVENT);
                    }
                });

        mCalendarListAdapter = new CalendarListAdapter(this);
        mListView.setAdapter(mCalendarListAdapter);

        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> parent,
                    final View view, final int position, final long id) {
                final Intent eventIntent = new Intent(CalendarActivity.this,
                        EventActivity.class);
                eventIntent.putExtra(Constants.INTENT_EXTRA_EVENT,
                        mCalendarListAdapter.getItem(position));
                startActivityForResult(eventIntent, VIEW_EVENT);
            }
        });

        findViewById(R.id.date_next).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(final View v) {
                        updateCalendar(MonthSwitcher.NEXT);

                    }
                });
        findViewById(R.id.date_previous).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(final View v) {
                        updateCalendar(MonthSwitcher.PREVIOUS);
                    }
                });

        registerForContextMenu(mListView);

        new GetEventListTask(CalendarActivity.this).execute();
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(final int requestCode,
            final int resultCode, final Intent data) {
        EventDomDocumentCreator event = null;

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CREATE_EVENT:
                    event = (EventDomDocumentCreator) (data
                            .getSerializableExtra(Constants.INTENT_EXTRA_RESULT));
                    if (event != null) {
                        mCalendarListAdapter.add(event);
                        mCalendarViewAdapter.add(event);
                    }
                    break;
                case VIEW_EVENT:
                    event = (EventDomDocumentCreator) (data
                            .getSerializableExtra(Constants.INTENT_EXTRA_RESULT));
                    if (event != null) {
                        mCalendarListAdapter.update(event);
                        mCalendarViewAdapter
                                .update(mCalendarListAdapter.getItems());
                    }
                    break;
                case PREFERENCES:
                    new GetEventListTask(CalendarActivity.this).execute();
                    break;
                default:
                    break;
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View,
     * android.view.ContextMenu.ContextMenuInfo)
     */
    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.notice_context_menu, menu);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        Intent eventIntent = null;

        switch (item.getItemId()) {
            case R.id.open:
                eventIntent = new Intent(CalendarActivity.this, EventActivity.class);
                eventIntent.putExtra(Constants.INTENT_EXTRA_EVENT,
                        mCalendarListAdapter.getItem(menuInfo.position));
                startActivityForResult(eventIntent, VIEW_EVENT);
                return true;
            case R.id.delete:
                deleteEvent(menuInfo.position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * method to delete an event
     * 
     * @param eventPosition
     *            the position of the item to delete
     */
    private final void deleteEvent(final int eventPosition) {
        final EventDomDocumentCreator eventDocument = mCalendarListAdapter
                .getItem(eventPosition);
        final MwAdapterPreferenceManager prefManager = new MwAdapterPreferenceManager(
                CalendarActivity.this);
        final String key = prefManager.getAppAuthKey();
        final String algorithm = prefManager.getAppAuthKeyAlgorithm();
        DeleteRequestProperties requestProperties = null;
        final String eventName = eventDocument.getDocumentName();
        String path = null;

        DeleteRequestToUrlTask deleteRequestToUrlTask = null;

        if (key != null && algorithm != null) {
            path = prefManager.getCloudPath();
            if (path.isEmpty() || path.endsWith("/")) {
                requestProperties = new DeleteRequestProperties(
                        prefManager.getCloudHostName(), path + eventName,
                        prefManager.getCloudPort());
            } else {
                requestProperties = new DeleteRequestProperties(
                        prefManager.getCloudHostName(), path + "/" + eventName,
                        prefManager.getCloudPort());
            }
            deleteRequestToUrlTask = new DeleteRequestToUrlTask(this,
                    eventName, eventPosition, key, algorithm, getApplication()
                            .getPackageName(), prefManager.getMiddlewarePort(),
                    requestProperties);
            deleteRequestToUrlTask.execute();

        } else {
            showDialog(NO_KEY_OR_ALGORITHM);
        }
    }

    /**
     * DeleteRequestToUrlTask
     * 
     * @author hoppe
     * 
     */
    private class DeleteRequestToUrlTask extends Service.DeleteRequestToUrl {

        private String mEventName = "";
        private int mEventPosition = -1;

        protected DeleteRequestToUrlTask(final Activity activity,
                final String eventName, final int eventPosition,
                final String key, final String algorithm, final String appName,
                final int port, final DeleteRequestProperties properties) {
            super(activity, key, algorithm, appName, port, properties, activity
                    .getString(R.string.service_task));
            this.mEventName = eventName;
            this.mEventPosition = eventPosition;
        }

        @Override
        protected void onPostExecuteWithoutException(final InputStream result) {
            super.onPostExecuteWithoutException(result);
            mCalendarListAdapter.remove(mEventPosition);
            mCalendarViewAdapter.update(mCalendarListAdapter.getItems());
            showDialog(SUCCESS);
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

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(final int id) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        alertDialogBuilder.setTitle(R.string.event_delete_success_title);

        switch (id) {
            case SUCCESS:
                alertDialogBuilder.setMessage(R.string.event_delete_success_true);
                break;
            case NO_KEY_OR_ALGORITHM:
                alertDialogBuilder
                        .setMessage(R.string.event_delete_success_false_key);
                break;
            case ERROR:
                alertDialogBuilder
                        .setMessage(getString(R.string.event_delete_success_false_error)
                                + mErrorMessage);
                break;
        }
        alertDialogBuilder.setNeutralButton(R.string.ok, null);
        alertDialogBuilder.setCancelable(false);

        return alertDialogBuilder.create();
    }

    /**
     * GetEventListFromExistTask
     * 
     * @author hoppe
     */
    private class GetEventListTask extends Service.GetEventListTask {

        protected GetEventListTask(final Activity activity) {
            super(activity, new TaskProgressDialog(activity));
        }

        @Override
        protected void onPostExecuteWithoutException(
                final List<EventDomDocumentCreator> result) {
            super.onPostExecuteWithoutException(result);
            mCalendarListAdapter.addAll(result);
            mCalendarViewAdapter.addAll(result);
        }

        @Override
        protected void onPostExecuteWithException(
                final List<EventDomDocumentCreator> result) {
            super.onPostExecuteWithException(result);
            mCalendarListAdapter.clear();
            mCalendarViewAdapter.clear();
            AlertHelper.showAlertDialog(mActivity,
                    mActivity.getString(R.string.error),
                    mException.getMessage());
        }

    }

    /**
     * 
     * @param monthSwitcher
     */
    private final void updateCalendar(final MonthSwitcher monthSwitcher) {
        mCalendarViewAdapter.refreshDays(monthSwitcher);
        mCalendarDate.setText(mDateFormat.format(mCalendarViewAdapter
                .getCurrentCalendar().getTime()));
    }

    /**
     * @param events
     *            event list for a day
     */
    private final void showEventPickerDialog(
            final List<EventDomDocumentCreator> events) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(
                CalendarActivity.this);
        final String[] eventArray = new String[events.size()];

        final SimpleDateFormat formatTitle = new SimpleDateFormat("dd.MM.yyyy",
                Locale.getDefault());
        builder.setTitle(String.format(
                getString(R.string.dialog_headline),
                formatTitle.format(events.get(0).getEvent().getBegin()
                        .getTime())));

        for (int i = 0; i < events.size(); i++) {
            eventArray[i] = events.get(i).getEvent().getSubject();
        }
        builder.setItems(eventArray, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                showDetailViewForEvent(events.get(which));
            }
        });

        builder.create().show();
    }

    private final void showDetailViewForEvent(
            final EventDomDocumentCreator event) {
        final Intent eventIntent = new Intent(CalendarActivity.this,
                EventActivity.class);
        eventIntent.putExtra(Constants.INTENT_EXTRA_EVENT, event);
        startActivityForResult(eventIntent, VIEW_EVENT);
    }

}

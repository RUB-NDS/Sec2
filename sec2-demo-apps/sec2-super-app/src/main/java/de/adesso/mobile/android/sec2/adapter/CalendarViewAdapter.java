
package de.adesso.mobile.android.sec2.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.util.MonthDisplayHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.adesso.mobile.android.sec2.R;

import de.adesso.mobile.android.sec2.model.EventType;
import de.adesso.mobile.android.sec2.util.EventDomDocumentCreator;
import de.adesso.mobile.android.sec2.util.MonthSwitcher;

/**
 * @author hoppe
 *
 */
public class CalendarViewAdapter extends BaseAdapter {

    private final int FIRST_DAY_OF_WEEK = 1; // Sunday = 0, Monday = 1
    private final int COLUMN_COUNT = 7;

    private final Context mContext;
    private final LayoutInflater mInflater;

    private final Calendar mMonth;
    private final Calendar mToday;

    private MonthDisplayHelper mMonthDisplayHelper;

    private int previousMonth;
    private int currentMonth;
    private int nextMonth;

    /**
     * @param c
     * @param monthCalendar
     */
    public CalendarViewAdapter(final Context c, final Calendar monthCalendar) {
        mContext = c;
        mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mToday = Calendar.getInstance();

        mMonth = (Calendar) monthCalendar.clone();
        mMonth.set(Calendar.DAY_OF_MONTH, 1);

        mMonthDisplayHelper = new MonthDisplayHelper(mMonth.get(Calendar.YEAR),
                mMonth.get(Calendar.MONTH), Calendar.MONDAY);
    }

    public void clear() {
        mEventMap.clear();
        notifyDataSetChanged();
    }

    /**
     * Calendar.SUNDAY = 1
     * Calendar.MONDAY = 2
     * ...
     * Calendar.SATURDAY = 7
     */
    @Override
    public int getCount() {
        previousMonth = (mMonth.get(Calendar.DAY_OF_WEEK) - FIRST_DAY_OF_WEEK - 1);
        previousMonth = (previousMonth < 0 ? COLUMN_COUNT + previousMonth : previousMonth);
        currentMonth = mMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
        nextMonth = (COLUMN_COUNT - ((previousMonth + currentMonth) % COLUMN_COUNT)) % COLUMN_COUNT;
        return (previousMonth + currentMonth + nextMonth);
    }

    @Override
    public Object getItem(final int position) {
        Calendar cal = (Calendar) mMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH,
                mMonthDisplayHelper.getDayAt(position / COLUMN_COUNT, position
                        % COLUMN_COUNT));

        return getEventsByDate(cal);

    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    public Calendar getCurrentCalendar() {
        return mMonth;
    }

    public void setCurrentCalendar(final Calendar calendar) {
        mMonth.setTime(calendar.getTime());
        mMonth.set(Calendar.DAY_OF_MONTH, 1);
    }

    public Calendar getTodayCalendar() {
        return mToday;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.calendar_month_item, parent, false);
            holder.day = (TextView) convertView.findViewById(R.id.date);
            holder.image = (ImageView) convertView.findViewById(R.id.image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final String value = String.valueOf(mMonthDisplayHelper.getDayAt(position / COLUMN_COUNT,
                position % COLUMN_COUNT));
        holder.day.setText(value);

        if (mMonthDisplayHelper.isWithinCurrentMonth(position / COLUMN_COUNT, position
                % COLUMN_COUNT)) {
            convertView.setBackgroundColor(determineColor(mMonth, Integer.valueOf(value)));
            holder.day.setTextColor(Color.BLACK);
            holder.image.setVisibility(determineImage(mMonth, Integer.valueOf(value)));
        } else {
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.white));
            holder.day.setTextColor(Color.GRAY);
            holder.image.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    public void refreshDays(final MonthSwitcher monthSwitcher) {
        switch (monthSwitcher) {
            case PREVIOUS:
                mMonth.add(Calendar.MONTH, -1);
                break;
            case NEXT:
                mMonth.add(Calendar.MONTH, 1);
                break;
            default:
                break;
        }
        mMonthDisplayHelper = new MonthDisplayHelper(mMonth.get(Calendar.YEAR),
                mMonth.get(Calendar.MONTH), Calendar.MONDAY);
        notifyDataSetChanged();
    }

    private final Map<String, List<EventHolder>> mEventMap = new HashMap<String, List<EventHolder>>();
    private final SimpleDateFormat mKeyFormat = new SimpleDateFormat("yyyy-MM-dd",
            Locale.getDefault());

    private void addItem(final EventDomDocumentCreator event) {
        EventHolder eventHolder = new EventHolder(event.getEvent().getSubject(), EventType.EVENT,
                event.getEvent().getBegin(), event.getEvent().getEnd(), event);
        if (mEventMap.containsKey(mKeyFormat.format(eventHolder.mBegin.getTime()))) {
            mEventMap.get(mKeyFormat.format(eventHolder.mBegin.getTime())).add(eventHolder);
        } else {
            List<EventHolder> events = new ArrayList<EventHolder>();
            events.add(eventHolder);
            mEventMap.put(mKeyFormat.format(eventHolder.mBegin.getTime()), events);
        }
    }

    public void add(final EventDomDocumentCreator item) {
        addItem(item);
        notifyDataSetChanged();
    }

    public void addAll(final List<EventDomDocumentCreator> events) {
        for (EventDomDocumentCreator event : events) {
            addItem(event);
        }
        notifyDataSetChanged();
    }

    public void update(final List<EventDomDocumentCreator> items) {
        mEventMap.clear();
        addAll(items);
    }

    private List<EventDomDocumentCreator> getEventsByDate(final Calendar calendar) {
        List<EventDomDocumentCreator> mReturnValues = new ArrayList<EventDomDocumentCreator>();
        if (calendar == null) {
            return mReturnValues;
        } else {
            if (mEventMap.containsKey(mKeyFormat.format(calendar.getTime()))) {
                for (EventHolder event : mEventMap.get((mKeyFormat.format(calendar.getTime())))) {
                    mReturnValues.add(event.mEvent);
                }
                return mReturnValues;
            } else {
                return mReturnValues;
            }
        }
    }

    private int determineColor(final Calendar calendar, final int day) {
        Calendar date = (Calendar) calendar.clone();
        date.set(Calendar.DAY_OF_MONTH, day);
        if (mKeyFormat.format(date.getTime()).equalsIgnoreCase(mKeyFormat.format(mToday.getTime()))) {
            return mContext.getResources()
                    .getColor(R.color.holo_blue_bright);
        } else {
            return mContext.getResources().getColor(
                    R.color.white);
        }
    }

    public int determineImage(final Calendar calendar, final int day) {
        Calendar date = (Calendar) calendar.clone();
        date.set(Calendar.DAY_OF_MONTH, day);
        if (mEventMap.containsKey(mKeyFormat.format(date.getTime()))) {
            return View.VISIBLE;
        } else {
            return View.INVISIBLE;
        }
    }

    private static class ViewHolder {
        TextView day;
        ImageView image;
    }

    public class EventHolder {

        public String mName;
        public EventType mType;
        public Calendar mBegin;
        public Calendar mEnd;
        public EventDomDocumentCreator mEvent;

        public EventHolder(final String name, final EventType type, final Calendar begin,
                final Calendar end, final EventDomDocumentCreator event) {
            mName = name;
            mType = type;
            mBegin = (Calendar) begin.clone();
            mEnd = (Calendar) end.clone();
            mEvent = event;
        }

    }

}

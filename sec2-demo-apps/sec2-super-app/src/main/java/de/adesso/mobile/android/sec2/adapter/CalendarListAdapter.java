
package de.adesso.mobile.android.sec2.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.adesso.mobile.android.sec2.R;
import de.adesso.mobile.android.sec2.model.Event;
import de.adesso.mobile.android.sec2.util.EventDomDocumentCreator;

/**
 * CalendarListAdapter
 * @author hoppe
 */
public class CalendarListAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;
    private final List<EventDomDocumentCreator> mItems = new ArrayList<EventDomDocumentCreator>();

    private static final int VIEW_ALTERNATIVE = 0;
    private static final int VIEW_NORMAL = 1;
    private static final int VIEW_TYPES = 2;

    public CalendarListAdapter(final Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }

    public void remove(final EventDomDocumentCreator item) {
        mItems.remove(item);
        notifyDataSetChanged();
    }

    public void remove(final int itemPosition) {
        mItems.remove(itemPosition);
        notifyDataSetChanged();
    }

    public void add(final EventDomDocumentCreator item) {
        mItems.add(item);
        Collections.sort(mItems, new TimeComparator());
        notifyDataSetChanged();
    }

    public void addAll(final List<EventDomDocumentCreator> list) {
        mItems.addAll(list);
        Collections.sort(mItems, new TimeComparator());
        notifyDataSetChanged();
    }

    public void update(final EventDomDocumentCreator item) {
        mItems.remove(item);
        mItems.add(item);
        Collections.sort(mItems, new TimeComparator());
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    public final List<EventDomDocumentCreator> getItems() {
        return mItems;
    }

    @Override
    public EventDomDocumentCreator getItem(final int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        ViewHolder holder;
        final Event item = getItem(position).getEvent();

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.calendar_item, parent, false);

            holder = new ViewHolder();
            holder.subject = (TextView) convertView.findViewById(R.id.subject);
            holder.lock = (ImageView) convertView.findViewById(R.id.lock);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.subject.setText(item.getSubject());
        switch (item.getLock()) {
            case UNLOCKED:
                holder.lock.setImageResource(R.drawable.unlocked);
                break;
            case PARTIALLY:
                holder.lock.setImageResource(R.drawable.halflocked);
                break;
            case LOCKED:
                holder.lock.setImageResource(R.drawable.locked);
                break;
        }

        return convertView;
    }

    static class ViewHolder {

        TextView subject;
        ImageView lock;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPES;
    }

    @Override
    public int getItemViewType(final int position) {
        if (position % 2 == 0) {
            return VIEW_NORMAL;
        } else {
            return VIEW_ALTERNATIVE;
        }
    }

    private static class TimeComparator implements Comparator<EventDomDocumentCreator> {

        @Override
        public int compare(final EventDomDocumentCreator o1, final EventDomDocumentCreator o2) {

            return o1.getEvent().getBegin().compareTo(o2.getEvent().getBegin());

        }

    }

}

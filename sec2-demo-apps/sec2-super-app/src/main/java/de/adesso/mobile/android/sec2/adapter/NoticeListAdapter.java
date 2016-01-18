package de.adesso.mobile.android.sec2.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.adesso.mobile.android.sec2.R;
import de.adesso.mobile.android.sec2.model.Notice;
import de.adesso.mobile.android.sec2.util.NoticeDomDocumentCreator;

/**
 * NoticeListAdapter
 * @author bruch
 */
public class NoticeListAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private final List<NoticeDomDocumentCreator> items = new ArrayList<NoticeDomDocumentCreator>();

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("dd-MM-yyyy",
            Locale.getDefault());

    private static final int VIEW_ALTERNATIVE = 0;
    private static final int VIEW_NORMAL = 1;
    private static final int VIEW_TYPES = 2;

    public NoticeListAdapter(final Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public void remove(final NoticeDomDocumentCreator item) {
        items.remove(item);
        notifyDataSetChanged();
    }

    public void remove(final int itemPosition) {
        items.remove(itemPosition);
        notifyDataSetChanged();
    }

    public void add(final NoticeDomDocumentCreator item) {
        items.add(item);
        Collections.sort(items, new TimeComparator());
        notifyDataSetChanged();
    }

    public void addAll(final List<NoticeDomDocumentCreator> list) {
        items.addAll(list);
        Collections.sort(items, new TimeComparator());
        notifyDataSetChanged();
    }

    public void update(final NoticeDomDocumentCreator item) {
        items.remove(item);
        items.add(item);
        Collections.sort(items, new TimeComparator());
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public NoticeDomDocumentCreator getItem(final int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        ViewHolder holder;
        final Notice item = getItem(position).getNotice();
        final GregorianCalendar date = item.getCreationDate();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.notice_item, null);

            holder = new ViewHolder();
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.subject = (TextView) convertView.findViewById(R.id.subject);
            holder.lock = (ImageView) convertView.findViewById(R.id.lock);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.date.setText(mDateFormat.format(date.getTime()));

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

        TextView date;
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

    private static class TimeComparator implements Comparator<NoticeDomDocumentCreator> {

        @Override
        public int compare(final NoticeDomDocumentCreator o1, final NoticeDomDocumentCreator o2) {

            return o1.getNotice().getCreationDate().compareTo(o2.getNotice().getCreationDate());

        }

    }

}

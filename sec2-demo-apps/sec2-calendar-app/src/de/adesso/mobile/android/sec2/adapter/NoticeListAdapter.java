package de.adesso.mobile.android.sec2.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.adesso.mobile.android.sec2.R;
import de.adesso.mobile.android.sec2.model.NoticeListItem;

/**
 * NoticeListAdapter
 * @author bruch
 */
public class NoticeListAdapter extends BaseAdapter {

    @SuppressWarnings ("unused")
    private static final Class<?> c = NoticeListAdapter.class;

    private final LayoutInflater inflater;
    private final List<NoticeListItem> items = new ArrayList<NoticeListItem>();

    public final int VIEW_ALTERNATIVE = 0;
    public final int VIEW_NORMAL = 1;
    public final int VIEW_TYPES = 2;

    public NoticeListAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public void remove(NoticeListItem item) {
        items.remove(item);
        notifyDataSetChanged();
    }

    public void add(NoticeListItem item) {
        items.add(item);
        notifyDataSetChanged();
    }

    public void addAll(List<NoticeListItem> list) {
        //        list.addAll(list);
        items.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public NoticeListItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        final NoticeListItem item = getItem(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.notice_item, null);

            holder = new ViewHolder();
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.subject = (TextView) convertView.findViewById(R.id.subject);
            holder.lock = (ImageView) convertView.findViewById(R.id.lock);

            convertView.setTag(holder);

            //            if (getItemViewType(position) == VIEW_NORMAL) {
            //                convertView.setBackgroundColor(0x80ffffff);
            //            } else {
            //                convertView.setBackgroundColor(0x80eeeeee);
            //            }

        } else {

            holder = (ViewHolder) convertView.getTag();
        }

        holder.date.setText(item.date);
        holder.subject.setText(item.subject);

        switch (item.lock) {
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
    public int getItemViewType(int position) {
        if (position % 2 == 0) return VIEW_NORMAL;
        else return VIEW_ALTERNATIVE;
    }

}

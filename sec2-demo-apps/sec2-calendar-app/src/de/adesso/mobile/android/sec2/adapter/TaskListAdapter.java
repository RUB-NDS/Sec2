package de.adesso.mobile.android.sec2.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import de.adesso.mobile.android.sec2.R;
import de.adesso.mobile.android.sec2.model.Priority;
import de.adesso.mobile.android.sec2.model.TaskListItem;

/**
 * TaskListAdapter
 * @author benner
 */
public class TaskListAdapter extends BaseAdapter {

    @SuppressWarnings ("unused")
    private static final Class<?> c = TaskListAdapter.class;

    private final LayoutInflater inflater;
    private final List<TaskListItem> items = new ArrayList<TaskListItem>();

    public final int VIEW_ALTERNATIVE = 0;
    public final int VIEW_NORMAL = 1;
    public final int VIEW_TYPES = 2;

    public TaskListAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public void remove(TaskListItem item) {
        items.remove(item);
        notifyDataSetChanged();
    }

    public void add(TaskListItem item) {
        items.add(item);
        notifyDataSetChanged();
    }

    public void addAll(List<TaskListItem> list) {
        items.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public TaskListItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        final TaskListItem item = getItem(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.task_item, null);

            holder = new ViewHolder();
            holder.subject = (TextView) convertView.findViewById(R.id.subject);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.state = (CheckBox) convertView.findViewById(R.id.state);
            holder.priority = (TextView) convertView.findViewById(R.id.priority);
            holder.lock = (ImageView) convertView.findViewById(R.id.lock);

            convertView.setTag(holder);

            if (getItemViewType(position) == VIEW_NORMAL) {
                convertView.setBackgroundColor(0x80ffffff);
            } else {
                convertView.setBackgroundColor(0x80eeeeee);
            }

        } else {

            holder = (ViewHolder) convertView.getTag();
        }
        holder.subject.setText(item.subject);
        holder.date.setText(item.date);
        holder.state.setChecked(item.isChecked);
        holder.priority.setText(item.priority.name());

        switch (item.lock.getType()) {
            case 0:
                holder.lock.setImageResource(R.drawable.unlocked);
                break;
            case 1:
                holder.lock.setImageResource(R.drawable.halflocked);
                break;
            case 2:
                holder.lock.setImageResource(R.drawable.locked);
                break;
        }

        return convertView;
    }

    static class ViewHolder {

    	TextView subject;
    	TextView date;
    	CheckBox state;
    	TextView priority;
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

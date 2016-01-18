package de.adesso.mobile.android.sec2.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import de.adesso.mobile.android.sec2.R;
import de.adesso.mobile.android.sec2.model.Task;
import de.adesso.mobile.android.sec2.util.TaskDomDocumentCreator;

/**
 * TaskListAdapter
 * @author benner
 */
public class TaskListAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;
    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("dd-MM-yyyy",
            Locale.getDefault());
    private final List<TaskDomDocumentCreator> mItems = new ArrayList<TaskDomDocumentCreator>();

    private final Context mContext;

    private static final int VIEW_ALTERNATIVE = 0;
    private static final int VIEW_NORMAL = 1;
    private static final int VIEW_TYPES = 2;

    public TaskListAdapter(final Context context) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }

    public void remove(final int itemPosition) {
        mItems.remove(itemPosition);
        notifyDataSetChanged();
    }

    public void remove(final TaskDomDocumentCreator item) {
        mItems.remove(item);
        notifyDataSetChanged();
    }

    public void add(final TaskDomDocumentCreator item) {
        mItems.add(item);
        Collections.sort(mItems, new TimeComparator());
        notifyDataSetChanged();
    }

    public void addAll(final List<TaskDomDocumentCreator> list) {
        if (list != null) {
            mItems.addAll(list);
            Collections.sort(mItems, new TimeComparator());
            notifyDataSetChanged();
        }
    }

    public void update(final TaskDomDocumentCreator item) {
        mItems.remove(item);
        mItems.add(item);
        Collections.sort(mItems, new TimeComparator());
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public TaskDomDocumentCreator getItem(final int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        ViewHolder holder;
        final Task item = getItem(position).getTask();

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.task_item, null);

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
        holder.subject.setText(item.getSubject());
        holder.date.setText(mDateFormat.format(item.getDueDate().getTime()));
        holder.state.setChecked(item.getIsDone());

        holder.priority
                .setText(mContext.getResources().getStringArray(R.array.priority_entries)[item
                        .getPriority().getType()]);

        switch (item.getLock().getType()) {
            case 0:
                holder.lock.setImageResource(R.drawable.unlocked);
                break;
            case 1:
                holder.lock.setImageResource(R.drawable.halflocked);
                break;
            case 2:
                holder.lock.setImageResource(R.drawable.locked);
                break;
            default:
                holder.lock.setImageResource(R.drawable.unlocked);
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
    public int getItemViewType(final int position) {
        if (position % 2 == 0) {
            return VIEW_NORMAL;
        } else {
            return VIEW_ALTERNATIVE;
        }
    }

    private static class TimeComparator implements Comparator<TaskDomDocumentCreator> {

        @Override
        public int compare(final TaskDomDocumentCreator o1, final TaskDomDocumentCreator o2) {

            return o1.getTask().getDueDate().compareTo(o2.getTask().getDueDate());

        }

    }

}

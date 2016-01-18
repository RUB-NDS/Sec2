package org.sec2.android.app.component;

import java.util.ArrayList;

import org.sec2.android.model.CountedGroup;
import org.sec2.middleware.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * An adapter extending the standard ArrayAdapter from Android for
 * CountedGroup-objects. It uses the layout defined in
 * counted_group_list_item.xml
 *
 * @author schuessler
 */
public class CountedGroupArrayAdapter extends ArrayAdapter<CountedGroup>
{
    private final ArrayList<CountedGroup> groups;

    /**
     * The constructor of this adapter.
     *
     * @param context - The context
     * @param groups - The list of groups
     */
    public CountedGroupArrayAdapter(final Context context,
            final ArrayList<CountedGroup> groups)
    {
        super(context, R.layout.counted_group_list_item, groups);
        if (groups != null)
        {
            this.groups = groups;
        }
        else
        {
            this.groups = new ArrayList<CountedGroup>();
        }
    }

    @Override
    public View getView(final int position, View convertView,
            final ViewGroup parent)
    {
        TextViewHolder viewHolder = null;
        CountedGroup group = null;
        final Context context = getContext();
        final StringBuilder memberCount = new StringBuilder("(");

        if (convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.counted_group_list_item, null);

            viewHolder = new TextViewHolder();
            viewHolder.groupName =
                    (TextView)(convertView.findViewById(R.id.cgli_group_name));
            viewHolder.count =
                    (TextView)(convertView.findViewById(R.id.cgli_count));

            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (TextViewHolder)(convertView.getTag());
        }

        group = groups.get(position);
        if (group != null)
        {
            viewHolder.groupName.setText(group.getGroupName());
            if (group.getMemberCount() >= 0)
            {
                memberCount.append(group.getMemberCount());
            }
            else
            {
                memberCount.append(context.getString(R.string.na));
            }
        }
        else
        {
            viewHolder.groupName.setText(context.getString(
                    R.string.null_literal));
            memberCount.append(context.getString(R.string.na));
        }
        memberCount.append(")");
        viewHolder.count.setText(memberCount.toString());

        return convertView;
    }

    /**
     * Adds a group to the list of groups managed by the adapter. If the passed
     * group is NULL a NullPointerException is thrown.
     *
     * @param group - The group to be added.
     */
    @Override
    public void add(final CountedGroup group)
    {
        if (group == null)
        {
            throw new NullPointerException("The variable \"group\" must not be"
                    + " NULL!");
        }
        groups.add(group);
        notifyDataSetChanged();
    }

    /**
     * Updates a group in the list of groups managed by the adapter by
     * replacing the group at position with the passed group. If the passed
     * group is NULL a NullPointerException is thrown.
     *
     * @param group - The group to be updated. The group must not be NULL,
     *  otherwise a NullPointerException is thrown
     * @param position - The position of the group, which is to be updated.
     */
    public void update(final CountedGroup group, final int position)
    {
        if (group == null)
        {
            throw new NullPointerException("The variable \"group\" must not be"
                    + " NULL!");
        }

        groups.remove(position);
        groups.add(position, group);
        notifyDataSetChanged();
    }

    /**
     * Removes the group at position from the list of groups managed by the
     * adapter.
     *
     * @param position - The position of the group, which is to be deleted.
     */
    public void remove(final int position)
    {
        groups.remove(position);
        notifyDataSetChanged();
    }

    private class TextViewHolder
    {
        private TextView groupName;
        private TextView count;
    }
}

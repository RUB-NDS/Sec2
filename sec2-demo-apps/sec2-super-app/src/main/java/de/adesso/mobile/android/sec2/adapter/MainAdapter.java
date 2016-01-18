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
import de.adesso.mobile.android.sec2.model.MainItem;

/**
 * MainAdapter
 * @author hoppe
 */
public class MainAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<MainItem> mList = new ArrayList<MainItem>();

    /**
     * Constructor
     */
    public MainAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void clear() {
        mList.clear();
    }

    public void add(MainItem item) {
        mList.add(item);
        notifyDataSetChanged();
    }

    public void addAll(List<MainItem> items) {
        if (items != null) {
            mList.addAll(items);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final MainItem item = mList.get(position);

        ViewHolder holder;

        if (convertView == null) {

            holder = new ViewHolder();

            convertView = mInflater.inflate(R.layout.main_item, null);
            holder.title = (TextView) convertView.findViewById(R.id.main_item_title);
            holder.image = (ImageView) convertView.findViewById(R.id.main_item_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.title.setText(item.title);
        holder.image.setImageResource(item.resId);

        return convertView;
    }

    /**
     * A nested top-level class is a member classes with a static modifier. A nested top-level class is just like any 
     * other top-level class except that it is declared within another class or interface. Nested top-level classes 
     * are typically used as a convenient way to group related classes without creating a new package.
     * 
     * @author hoppe
     */
    private static class ViewHolder {

        TextView title;
        ImageView image;
    }

}

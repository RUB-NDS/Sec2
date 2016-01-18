package de.adesso.mobile.android.sec2.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.adesso.mobile.android.sec2.R;
import de.adesso.mobile.android.sec2.model.FileChooser;
import de.adesso.mobile.android.sec2.model.FileComparator;
import de.adesso.mobile.android.sec2.model.ListItem;
import de.adesso.mobile.android.sec2.model.SecureFileChooser;
import de.adesso.mobile.android.sec2.service.Service;
import de.adesso.mobile.android.sec2.util.Filehelper;
import de.adesso.mobile.android.sec2.util.LogHelper;

public class FileChooserAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<ListItem> list = new ArrayList<ListItem>();
    private Context context;
    private Service.ImageLoadingTask imageLoadingTask;

    private static final int VIEW_TYPES_COUNT = 2;
    private static final int VIEW_TYPE_FILE = 0;
    private static final int VIEW_TYPE_SECURE_FILE = 1;

    private File existFolder = new File(Environment.getExternalStorageDirectory() + File.separator + "Android", "");

    public FileChooserAdapter(final Context context) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        imageLoadingTask = null;
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    public void add(ListItem item) {
        list.add(item);
        sortList(new FileComparator());
    }

    public void add(File file) {
        if (!file.isHidden()) {
            if (file.isDirectory()) {
                list.add(new FileChooser(Uri.parse(file.getAbsolutePath()).getLastPathSegment(), true, file, null, Filehelper
                        .mapMimeTypeDrawable(file, context)));
            } else {
                if (file.getAbsolutePath().contains(existFolder.getAbsolutePath())) {
                    list.add(new SecureFileChooser(Uri.parse(file.getAbsolutePath()).getLastPathSegment(), false, file, null, Filehelper.mapMimeTypeDrawable(
                            file, context), "PATH"));
                } else {
                    list.add(new FileChooser(Uri.parse(file.getAbsolutePath()).getLastPathSegment(), false, file, null, Filehelper.mapMimeTypeDrawable(file,
                            context)));
                }
            }
        }
        sortList(new FileComparator());
    }

    public void addAll(File folder) {
        if (folder != null) {
            for (int i = 0; i < folder.listFiles().length; i++) {
                if (!folder.listFiles()[i].isHidden()) {
                    if (folder.listFiles()[i].isDirectory()) {
                        list.add(new FileChooser(Uri.parse(folder.listFiles()[i].getAbsolutePath()).getLastPathSegment(), true, folder.listFiles()[i], null,
                                Filehelper.mapMimeTypeDrawable(folder.listFiles()[i], context)));
                    } else {
                        if (folder.getAbsolutePath().contains(existFolder.getAbsolutePath())) {
                            list.add(new SecureFileChooser(Uri.parse(folder.listFiles()[i].getAbsolutePath()).getLastPathSegment(), false,
                                    folder.listFiles()[i], null, Filehelper.mapMimeTypeDrawable(folder.listFiles()[i], context), "PATH"));
                        } else {
                            list.add(new FileChooser(Uri.parse(folder.listFiles()[i].getAbsolutePath()).getLastPathSegment(), false, folder.listFiles()[i],
                                    null, Filehelper.mapMimeTypeDrawable(folder.listFiles()[i], context)));
                        }
                    }
                }
            }
            sortList(new FileComparator());
        }
    }

    public void addAll(List<ListItem> items) {
        if (items != null) {
            list.addAll(items);
            sortList(new FileComparator());
        }
    }

    void sortList(Comparator<ListItem> c) {
        Collections.sort(list, c);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    public Object getList() {
        return list;
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position) instanceof SecureFileChooser) {
            return VIEW_TYPE_SECURE_FILE;
        }
        if (getItem(position) instanceof FileChooser) {
            return VIEW_TYPE_FILE;
        }
        //        if (getItem(position) instanceof ?) {
        //            return VIEW_TYPE_?;
        //        }
        return -1;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPES_COUNT;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ListItem item = list.get(position);

        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();

            convertView = inflater.inflate(R.layout.item, null);
            holder.image = (ImageView) convertView.findViewById(R.id.image);
            //            holder.title = (MarqueeViewSingle) convertView.findViewById(R.id.title);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        switch (getItemViewType(position)) {
            case VIEW_TYPE_SECURE_FILE:
                SecureFileChooser secureFileChooserItem = (SecureFileChooser) item;
                holder.title.setText(secureFileChooserItem.title);
                //        holder.title.setText1(item.title);
                //        holder.title.startMarquee();
                holder.image.setImageResource(R.drawable.file_encrypted);
                break;
            case VIEW_TYPE_FILE:
                FileChooser fileChooserItem = (FileChooser) item;
                holder.title.setText(fileChooserItem.title);
                if (fileChooserItem.isDir) {
                    holder.image.setImageResource(R.drawable.folder);
                } else {
                    holder.image.setImageDrawable(fileChooserItem.image);
                }
                break;
        }

        return convertView;
    }

    /**
     * A nested top-level class is a member classes with a static modifier. A nested top-level class is just like any 
     * other top-level class except that it is declared within another class or interface. Nested top-level classes 
     * are typically used as a convenient way to group related classes without creating a new package.
     * 
     * @author mschmitz
     */
    private static class ViewHolder {

        ImageView image;
        //        MarqueeViewSingle title;
        TextView title;
    }

    public void initTask(File folder) {
        if (imageLoadingTask == null) {
            imageLoadingTask = new Service.ImageLoadingTask(context, this, folder.getAbsolutePath());
            imageLoadingTask.execute();
        } else {
            if ((imageLoadingTask.getStatus() != AsyncTask.Status.RUNNING)) {
                imageLoadingTask = new Service.ImageLoadingTask(context, this, folder.getAbsolutePath());
                imageLoadingTask.execute();
            }
        }
    }

    public void reInitTask(File folder) {
        if (imageLoadingTask != null && imageLoadingTask.getStatus() != AsyncTask.Status.RUNNING) {
            LogHelper.logE("Stopping the Task");
            imageLoadingTask.cancel(true);
        }
        imageLoadingTask = new Service.ImageLoadingTask(context, this, folder.getAbsolutePath());
        imageLoadingTask.execute();
    }

    public void stopTask() {
        if (imageLoadingTask != null && imageLoadingTask.getStatus() == AsyncTask.Status.RUNNING) {
            LogHelper.logE("imageLoadingTask.getStatus(): " + imageLoadingTask.getStatus());
            imageLoadingTask.cancel(true);
        }
    }

}

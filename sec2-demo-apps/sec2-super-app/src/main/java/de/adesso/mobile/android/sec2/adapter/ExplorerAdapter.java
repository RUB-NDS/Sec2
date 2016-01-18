
package de.adesso.mobile.android.sec2.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.adesso.mobile.android.sec2.R;
import de.adesso.mobile.android.sec2.db.FilesDbHandler;
import de.adesso.mobile.android.sec2.model.FileItem;
import de.adesso.mobile.android.sec2.model.ListItem;
import de.adesso.mobile.android.sec2.model.Sec2FileItem;
import de.adesso.mobile.android.sec2.util.FileComparator;
import de.adesso.mobile.android.sec2.util.FileHelper;

/**
 * Adapter to manage the file system 
 * 
 * @author hoppe
 *
 */
public class ExplorerAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;
    private final List<ListItem> mList = new ArrayList<ListItem>();
    private final Context mContext;

    public static final int VIEW_TYPE_FILE = 0;
    public static final int VIEW_TYPE_SEC2_FILE = 1;
    private static final int VIEW_TYPES_COUNT = 2;

    private AsyncTask<Object, Void, Void> mMapMimeTypeTask;
    private String mCurrentFolder;
    private final FileComparator mFileComparator = new FileComparator();
    private final HashMap<String, ArrayList<ListItem>> mMap;

    /**
     * Constructor
     * @param context Context to create and inflate the layout
     */
    public ExplorerAdapter(final Context context) {
        this.mContext = context;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mMapMimeTypeTask = null;
        this.mMap = new HashMap<String, ArrayList<ListItem>>();
    }

    /**
     * Method to clear the current list
     */
    public void clear() {
        mList.clear();
        notifyDataSetChanged();
    }

    /**
     * Method to add an item to the current list
     * @param file the file to add
     */
    public void addDownload(final File file) {
        ArrayList<ListItem> downloadList = mMap.get(FileHelper.DOWNLOAD_FOLDER_PATH);
        if (downloadList == null) {
            downloadList = new ArrayList<ListItem>();
            downloadList.add(new FileItem(file.getName(), false, file, FileHelper.mapMimeType(file,
                    mContext)));
            mMap.put(FileHelper.DOWNLOAD_FOLDER_PATH, downloadList);
        } else {
            downloadList.add(new FileItem(file.getName(), false, file, FileHelper.mapMimeType(file,
                    mContext)));
        }

        sortList(downloadList, mFileComparator);
    }

    /**
     * Method to add a sec2 item to the current list
     * @param fileName name of the file
     */
    public void addSec2(final String fileName) {
        ArrayList<ListItem> sec2List = mMap.get(FileHelper.SEC2_FOLDER_PATH);
        if (sec2List == null) {
            sec2List = new ArrayList<ListItem>();
            sec2List.add(new Sec2FileItem(fileName, mContext.getResources().getDrawable(
                    R.drawable.file_encrypted)));
            mMap.put(FileHelper.SEC2_FOLDER_PATH, sec2List);
        } else {
            sec2List.add(new Sec2FileItem(fileName, mContext.getResources().getDrawable(
                    R.drawable.file_encrypted)));
        }
        sortList(sec2List, mFileComparator);
    }

    /**
     * Method to add an item to the current list
     * @param file the file to add
     */
    public void add(final File file) {
        if (!file.isHidden()) {
            if (file.isDirectory()) {
                mList.add(new FileItem(file.getName(), true, file, mContext.getResources()
                        .getDrawable(R.drawable.folder)));
            } else {
                if (mMap.containsKey(mCurrentFolder)) {
                    mList.add(new FileItem(file.getName(), false, file, FileHelper.mapMimeType(
                            file, mContext)));
                    mMap.put(mCurrentFolder,
                            (ArrayList<ListItem>) ((ArrayList<ListItem>) mList).clone());
                }
            }
        }
        sortList(mFileComparator);
        notifyDataSetChanged();
        mapMimeType();
    }

    /**
     * Method to remove an item from the current list
     * @param item the item to remove
     */
    public void remove(final ListItem item) {
        mList.remove(item);
        mMap.put(mCurrentFolder, (ArrayList<ListItem>) ((ArrayList<ListItem>) mList).clone());
        notifyDataSetChanged();
    }

    /**
     * Mehod to initialize the adapter
     */
    public void init() {
        mCurrentFolder = FileHelper.ROOT_PATH;

        final ArrayList<ListItem> list = new ArrayList<ListItem>();
        final File[] files = FileHelper.ROOT_PATH_FOLDER.listFiles();

        if (files != null) {
            for (final File file : files) {
                if (!file.isHidden()) {
                    if (file.isDirectory()) {
                        list.add(new FileItem(file.getName(), true, file, mContext.getResources()
                                .getDrawable(R.drawable.folder)));
                    } else {
                        list.add(new FileItem(file.getName(), false, file, FileHelper.mapMimeType(
                                file, mContext)));
                    }
                }
            }
        }
        if (!FileHelper.DOWNLOAD_FOLDER.exists()) {
            FileHelper.DOWNLOAD_FOLDER.mkdirs();
            list.add(new FileItem(FileHelper.DOWNLOAD_FOLDER_NAME, true,
                    FileHelper.DOWNLOAD_FOLDER, mContext.getResources().getDrawable(
                            R.drawable.folder)));
        }
        list.add(new Sec2FileItem(FileHelper.SEC2_FOLDER_NAME, true, FileHelper.SEC2_FOLDER,
                mContext.getResources().getDrawable(R.drawable.sec2_folder_closed)));
        sortList(list, mFileComparator);
        mMap.put(mCurrentFolder, list);
        this.mList.addAll(list);
        initiateSec2ItemsFromDb();
        mapMimeType();
    }

    /**
     * Method to add a folder to the current list
     * @param folder the folder to add
     */
    public void addAll(final File folder) {
        if (folder != null) {
            mCurrentFolder = folder.getAbsolutePath();
            if (!mMap.containsKey(mCurrentFolder)) {
                final ArrayList<ListItem> list = new ArrayList<ListItem>();
                final File[] files = folder.listFiles();

                if (files != null) {
                    for (final File file : files) {
                        if (!file.isHidden()) {
                            if (file.isDirectory()) {
                                list.add(new FileItem(file.getName(), true, file, mContext
                                        .getResources().getDrawable(R.drawable.folder)));
                            } else {
                                list.add(new FileItem(file.getName(), false, file, FileHelper
                                        .mapMimeType(file, mContext)));
                            }
                        }
                    }
                }
                sortList(list, mFileComparator);
                mMap.put(mCurrentFolder, list);
                this.mList.addAll(list);
            } else {
                mList.addAll(mMap.get(mCurrentFolder));
                notifyDataSetChanged();
            }
            mapMimeType();
        }
    }

    /**
     * Method to sort the current list
     * @param c the Comparator used to sort the list
     */
    private void sortList(final Comparator<ListItem> c) {
        Collections.sort(mList, c);
        notifyDataSetChanged();
    }

    /**
     * Method to sort the current list
     * @param list the list that will  be sorted
     * @param c the Comparator used to sort the list
     */
    private void sortList(final ArrayList<ListItem> list, final Comparator<ListItem> c) {
        Collections.sort(list, c);
        notifyDataSetChanged();
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        return mList.size();
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public Object getItem(final int position) {
        return mList.get(position);
    }

    /**
     * Method to return the current list
     * @return the list
     */
    public Object getList() {
        return mList;
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(final int position) {
        return mList.get(position).hashCode();
    }

    /*
     * (non-Javadoc)
     * @see android.widget.BaseAdapter#getItemViewType(int)
     */
    @Override
    public int getItemViewType(final int position) {
        if (getItem(position) instanceof FileItem) {
            return VIEW_TYPE_FILE;
        }
        if (getItem(position) instanceof Sec2FileItem) {
            return VIEW_TYPE_SEC2_FILE;
        }
        return -1;
    }

    /*
     * (non-Javadoc)
     * @see android.widget.BaseAdapter#getViewTypeCount()
     */
    @Override
    public int getViewTypeCount() {
        return VIEW_TYPES_COUNT;
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        final ListItem item = mList.get(position);

        final ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();

            convertView = mInflater.inflate(R.layout.item, null);
            holder.image = (ImageView) convertView.findViewById(R.id.image);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(item.title);
        holder.image.setImageDrawable(item.image);

        return convertView;
    }

    /**
     * ViewHolder 
     * 
     * @author hoppe
     *
     */
    private static class ViewHolder {

        ImageView image;
        TextView title;
    }

    /**
     * mapMimeType
     */
    public void mapMimeType() {
        if (mMapMimeTypeTask == null) {
            mMapMimeTypeTask = new MapMimeTypeTask(mContext, this);
            mMapMimeTypeTask.execute();
        } else {
            if (mMapMimeTypeTask.getStatus() == AsyncTask.Status.RUNNING) {
                mMapMimeTypeTask.cancel(true);
            }
            if ((mMapMimeTypeTask.getStatus() != AsyncTask.Status.RUNNING)) {
                mMapMimeTypeTask = new MapMimeTypeTask(mContext, this);
                mMapMimeTypeTask.execute();
            }
        }

    }

    /**
     * MapMimeTypeTask
     * 
     * @author hoppe
     *
     */
    public static class MapMimeTypeTask extends AsyncTask<Object, Void, Void> {

        protected Context context;
        protected Exception exception;
        protected ExplorerAdapter explorerAdapter;

        /**
         * Constructor
         */
        public MapMimeTypeTask(final Context context, final ExplorerAdapter explorerAdapter) {
            this.context = context;
            this.explorerAdapter = explorerAdapter;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(final Void... values) {
            explorerAdapter.notifyDataSetChanged();
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(final Object... params) {

            if (!isCancelled()) {
                try {

                    // HashMap<String, Long> imageList = new HashMap<String, Long>();
                    // if (fca.getCount() > 0) imageList = FileHelper.getImageThumbnailIds(context,
                    // ((FileItem) fca.getItem(0)).file.getParentFile());

                    for (int i = 0; i < explorerAdapter.getCount(); i++) {
                        if (explorerAdapter.getItem(i) instanceof FileItem) {
                            final FileItem item = (FileItem) explorerAdapter.getItem(i);

                            if (!item.isDir && item.mimeType == null) {
                                item.mimeType = FileHelper.getMimeType(item.title
                                        .toLowerCase(Locale.getDefault()));
                                final BitmapDrawable bitmapDrawable = FileHelper.getThumbnail(
                                        context, item.file, item.mimeType);
                                if (bitmapDrawable != null) {
                                    item.image = bitmapDrawable;
                                    publishProgress((Void) null);
                                }
                                // if (item.mimeType.matches(FileHelper.MIME_TYPE_IMAGE)) {
                                // // item.image = FileHelper.getImageThumbnail(context,
                                // imageList.get(item.file.getAbsolutePath()));
                                // item.image = FileHelper.getImageThumbnail(context, item.file);
                                // // item.image = FileHelper.loadBitmapDrawable(context,
                                // item.file);
                                // publishProgress((Void) null);
                                // }
                                // if (item.mimeType.matches(FileHelper.MIME_TYPE_APK)) {
                                // item.image = FileHelper.getApkThumbnail(context, item.file);
                                // publishProgress((Void) null);
                                // }
                                // if (item.mimeType.matches(FileHelper.MIME_TYPE_VIDEO)) {
                                // item.image = FileHelper.getVideoThumbnail(context, item.file);
                                // publishProgress((Void) null);
                                // }

                            }
                        }

                    }

                } catch (final Exception e) {
                    exception = e;
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            if (exception == null) {
                onPostExecuteWithoutException(result);
            } else {
                onPostExecuteWithException(result);
            }

        }

        protected void onPostExecuteWithException(final Void result) {
        }

        protected void onPostExecuteWithoutException(final Void result) {
        }

    }

    /**
     * Method to initiate the sec2 folder.
     */
    private void initiateSec2ItemsFromDb() {
        final FilesDbHandler db = new FilesDbHandler(mContext, false);
        if (db != null) {
            for (final String name : db.getFileNames()) {
                addSec2(name);
            }
            db.close();
        }
    }

}

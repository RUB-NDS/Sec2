package de.adesso.mobile.android.sec2;

import java.io.File;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;
import de.adesso.mobile.android.sec2.activity.Sec2Activity;
import de.adesso.mobile.android.sec2.adapter.FileChooserAdapter;
import de.adesso.mobile.android.sec2.model.FileChooser;
import de.adesso.mobile.android.sec2.model.ListItem;
import de.adesso.mobile.android.sec2.model.SecureFileChooser;
import de.adesso.mobile.android.sec2.service.Service;
import de.adesso.mobile.android.sec2.util.Filehelper;

public class FileChooserActivity extends Sec2Activity {

    private GridView gridView;
    private FileChooserAdapter adapter;
    private File folder;
    private Service.ImageLoadingTask imageLoadingTask;
    private File existFolder = new File(Environment.getExternalStorageDirectory() + File.separator + "Android", "");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.explorer);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);

        initListeners();
        initAdapter(Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    private void initListeners() {
        gridView = (GridView) findViewById(R.id.explorer_grid);
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                ListItem item;
                if (adapter.getItem(position) instanceof FileChooser) {
                    item = (FileChooser) adapter.getItem(position);
                } else if (adapter.getItem(position) instanceof SecureFileChooser) {
                    item = (SecureFileChooser) adapter.getItem(position);
                } else {
                    item = (FileChooser) adapter.getItem(position);
                }
                //                if (!item.isDir) {
                //                    LogHelper.toast(FileChooserActivity.this, "" + Filehelper.getMimeType(item.file));
                //                }
                if (item.isDir) {
                    reInitAdapter(item.file.getAbsolutePath());
                } else {
                    if (Filehelper.getMimeType(item.file) != null) {
                        try {
                            Intent openExternalIntent = new Intent(Intent.ACTION_VIEW);
                            openExternalIntent.setDataAndType(Uri.fromFile(item.file), Filehelper.getMimeType(item.file));
                            startActivity(openExternalIntent);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            Intent openExternalIntent = new Intent(Intent.ACTION_VIEW);
                            openExternalIntent.setDataAndType(Uri.fromFile(item.file), "text/plain");
                            startActivity(openExternalIntent);
                        } catch (Exception e) {}
                    }
                }
            }
        });
        registerForContextMenu(gridView);
    }

    private void initAdapter(String directory) {

        adapter = new FileChooserAdapter(this);

        folder = new File(directory, "");

        ((TextView) findViewById(R.id.explorer_header)).setText(folder.getAbsolutePath());
        //        ((MarqueeViewSingle) findViewById(R.id.explorer_header)).setText1(folder.getAbsolutePath());
        //        ((MarqueeViewSingle) findViewById(R.id.explorer_header)).startMarquee();

        adapter.addAll(folder);

        gridView.setAdapter(adapter);

        adapter.initTask(folder);
    }

    private void reInitAdapter(String directory) {

        folder = new File(directory, "");

        ((TextView) findViewById(R.id.explorer_header)).setText(folder.getAbsolutePath());

        adapter.clear();

        adapter.addAll(folder);

        gridView.setAdapter(adapter);

        adapter.reInitTask(folder);
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!Environment.getExternalStorageDirectory().getAbsolutePath().equalsIgnoreCase(folder.getAbsolutePath())) {
                reInitAdapter(folder.getParent());
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.document_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.select:
                Filehelper.getMimeType(((FileChooser) gridView.getItemAtPosition(menuInfo.position)).file);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void initTask() {
        if (imageLoadingTask == null) {
            imageLoadingTask = new Service.ImageLoadingTask(FileChooserActivity.this, adapter, folder.getAbsolutePath());
            imageLoadingTask.execute();
        } else {
            if ((imageLoadingTask.getStatus() != AsyncTask.Status.RUNNING)) {
                imageLoadingTask = new Service.ImageLoadingTask(FileChooserActivity.this, adapter, folder.getAbsolutePath());
                imageLoadingTask.execute();
            }
        }
    }
}

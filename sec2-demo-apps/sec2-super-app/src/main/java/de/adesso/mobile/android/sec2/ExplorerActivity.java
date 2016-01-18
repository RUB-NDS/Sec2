
package de.adesso.mobile.android.sec2;

import java.io.File;
import java.io.InputStream;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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
import de.adesso.mobile.android.sec2.adapter.ExplorerAdapter;
import de.adesso.mobile.android.sec2.db.FilesDbHandler;
import de.adesso.mobile.android.sec2.dialog.TaskProgressDialog;
import de.adesso.mobile.android.sec2.model.FileItem;
import de.adesso.mobile.android.sec2.model.ListItem;
import de.adesso.mobile.android.sec2.model.Lock;
import de.adesso.mobile.android.sec2.model.Sec2File;
import de.adesso.mobile.android.sec2.model.Sec2FileItem;
import de.adesso.mobile.android.sec2.mwadapter.DeleteRequestProperties;
import de.adesso.mobile.android.sec2.mwadapter.PutRequestProperties;
import de.adesso.mobile.android.sec2.mwadapter.util.MwAdapterPreferenceManager;
import de.adesso.mobile.android.sec2.service.Service;
import de.adesso.mobile.android.sec2.util.CheckedGroupHandler;
import de.adesso.mobile.android.sec2.util.Constants;
import de.adesso.mobile.android.sec2.util.FileDomDocumentCreator;
import de.adesso.mobile.android.sec2.util.FileHelper;

public final class ExplorerActivity extends Sec2Activity {

    private static final int RESULT_ID = 1095;

    private static final int NO_KEY_OR_ALGORITHM = 0;
    private static final int ERROR = 1;
    private static final int DB_ERROR = 2;
    private static final int SAVE_SUCCESS = 3;
    private static final int DELETE_SUCCESS = 4;
    private static final int OVERRIDE_SEC2 = 5;
    private static final int OVERRIDE_DOWNLOAD = 6;
    private static final int FILESIZE_EXCEEDED = 7;
    private static final int ENCRYPT = 8;
    private static final int OPEN_FILE = 9;

    private GridView mGridView;
    private ExplorerAdapter mAdapter;
    private TextView mPath;

    private File mFolder = FileHelper.ROOT_PATH_FOLDER;

    private String mErrorMessage = null;

    private ListItem mListItem = null;

    private FileDomDocumentCreator mFileCreator = null;
    private boolean mOverride = false;
    private boolean mEncrypt = false;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.explorer);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.titlebar);

        initListeners();
    }

    private void updatePath(final String absolutePath) {
        mPath.setText(absolutePath);
    }

    private void initListeners() {
        mGridView = (GridView) findViewById(R.id.explorer_grid);
        mPath = (TextView) findViewById(R.id.explorer_path);

        mAdapter = new ExplorerAdapter(this);
        mAdapter.init();
        updatePath(mFolder.getAbsolutePath());
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> parent,
                    final View view, final int position, final long id) {
                switch (mAdapter.getItemViewType(position)) {
                    case ExplorerAdapter.VIEW_TYPE_FILE:
                        mListItem = (FileItem) mAdapter.getItem(position);
                        if (!mListItem.isDir) {
                            try {
                                final Intent openExternalIntent = new Intent(
                                        Intent.ACTION_VIEW);
                                openExternalIntent.setDataAndType(
                                        Uri.fromFile(mListItem.file),
                                        FileHelper.getMimeType(mListItem.title));
                                startActivity(openExternalIntent);
                            } catch (final ActivityNotFoundException e) {
                                showDialog(OPEN_FILE);
                            }
                        }
                        break;
                    case ExplorerAdapter.VIEW_TYPE_SEC2_FILE:
                        mListItem = (Sec2FileItem) mAdapter.getItem(position);
                        if (!mListItem.isDir) {
                            validateAndDownload(mListItem);
                        }
                        break;
                    default:
                        mListItem = (ListItem) mAdapter.getItem(position);
                        break;
                }
                if (mListItem.isDir) {
                    mFolder = new File(mFolder.getAbsolutePath()
                            + File.separator + mListItem.title);
                    updatePath(mFolder.getAbsolutePath());
                    mAdapter.clear();
                    mAdapter.addAll(mFolder);
                }
            }
        });

        registerForContextMenu(mGridView);
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!Environment.getExternalStorageDirectory().getAbsolutePath()
                    .equalsIgnoreCase(mFolder.getAbsolutePath())) {
                mFolder = new File(mFolder.getParent());
                mAdapter.clear();
                mAdapter.addAll(mFolder);
                updatePath(mFolder.getAbsolutePath());
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.document_context_menu, menu);

        final AdapterView.AdapterContextMenuInfo detailedMenuInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
        final ListItem item = (ListItem) mGridView
                .getItemAtPosition(detailedMenuInfo.position);
        menu.findItem(R.id.document_open).setVisible(
                !(!item.isDir && (item instanceof Sec2FileItem)));
        menu.findItem(R.id.document_delete).setVisible(
                !(item.isDir && (item instanceof Sec2FileItem)));
        menu.findItem(R.id.document_encode).setVisible(
                !item.isDir && (item instanceof FileItem));
        menu.findItem(R.id.document_decode).setVisible(
                !item.isDir && (item instanceof Sec2FileItem));
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {

        final AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();

        switch (item.getItemId()) {
            case R.id.document_open:
                mListItem = ((FileItem) mGridView
                        .getItemAtPosition(menuInfo.position));
                if (mListItem.isDir) {
                    mFolder = new File(mFolder.getAbsolutePath() + File.separator
                            + mListItem.title);
                    updatePath(mFolder.getAbsolutePath());
                    mAdapter.clear();
                    mAdapter.addAll(mFolder);
                } else {
                    try {
                        final Intent openExternalIntent = new Intent(
                                Intent.ACTION_VIEW);
                        openExternalIntent.setDataAndType(
                                Uri.fromFile(mListItem.file),
                                FileHelper.getMimeType(mListItem.title));
                        startActivity(openExternalIntent);
                    } catch (final ActivityNotFoundException e) {
                        showDialog(OPEN_FILE);
                    }
                }
                return true;
            case R.id.document_delete:
                switch (mAdapter.getItemViewType(menuInfo.position)) {
                    case ExplorerAdapter.VIEW_TYPE_FILE:
                        mListItem = ((FileItem) mAdapter.getItem(menuInfo.position));
                        FileHelper.delete(mListItem.file);
                        mAdapter.remove(mListItem);
                        break;
                    case ExplorerAdapter.VIEW_TYPE_SEC2_FILE:
                        mListItem = ((Sec2FileItem) mAdapter.getItem(menuInfo.position));
                        deleteFile(mListItem);
                        break;
                    default:
                        break;
                }
                return true;
            case R.id.document_encode:
                mListItem = ((FileItem) mGridView
                        .getItemAtPosition(menuInfo.position));
                validateAndSave(mListItem);
                return true;
            case R.id.document_decode:
                mListItem = (Sec2FileItem) mAdapter.getItem(menuInfo.position);
                if (!mListItem.isDir) {
                    validateAndDownload(mListItem);
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onPrepareDialog(final int id, final Dialog dialog,
            final Bundle args) {
        switch (id) {
            case FILESIZE_EXCEEDED:
                ((AlertDialog) dialog).setMessage(String.format(
                        getString(R.string.explorer_create_false_filesize),
                        mListItem.title));
                break;
            case OVERRIDE_SEC2:
                ((AlertDialog) dialog).setMessage(String
                        .format(getString(R.string.explorer_override_file),
                                mListItem.title));
                break;
            case SAVE_SUCCESS:
                ((AlertDialog) dialog).setMessage(String.format(
                        getString(R.string.explorer_create_save_success),
                        mListItem.title));
                break;
            case DELETE_SUCCESS:
                ((AlertDialog) dialog).setMessage(String.format(
                        getString(R.string.explorer_create_delete_success),
                        mListItem.title));
                break;
            case OPEN_FILE:
                ((AlertDialog) dialog).setMessage(String.format(
                        getString(R.string.explorer_open_file), mListItem.title));
                break;
            default:
                break;
        }
        super.onPrepareDialog(id, dialog, args);
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        alertDialogBuilder.setTitle(R.string.explorer_create_title);
        switch (id) {
            case SAVE_SUCCESS:
                alertDialogBuilder.setMessage(String.format(
                        getString(R.string.explorer_create_save_success),
                        mListItem.title));
                alertDialogBuilder.setNeutralButton(android.R.string.ok, null);
                break;
            case DELETE_SUCCESS:
                alertDialogBuilder.setMessage(String.format(
                        getString(R.string.explorer_create_delete_success),
                        mListItem.title));
                alertDialogBuilder.setNeutralButton(android.R.string.ok, null);
                break;
            case NO_KEY_OR_ALGORITHM:
                alertDialogBuilder.setMessage(R.string.explorer_create_false_key);
                alertDialogBuilder.setNeutralButton(android.R.string.ok, null);
                break;
            case DB_ERROR:
                alertDialogBuilder.setMessage(R.string.explorer_create_false_db);
                alertDialogBuilder.setNeutralButton(android.R.string.ok, null);
                break;
            case ERROR:
                alertDialogBuilder
                        .setMessage(getString(R.string.explorer_create_false_error)
                                + mErrorMessage);
                alertDialogBuilder.setNeutralButton(android.R.string.ok, null);
                break;
            case FILESIZE_EXCEEDED:
                alertDialogBuilder.setMessage(String.format(
                        getString(R.string.explorer_create_false_filesize),
                        mListItem.title));
                alertDialogBuilder.setNeutralButton(android.R.string.ok, null);
                break;
            case OPEN_FILE:
                alertDialogBuilder.setMessage(String.format(
                        getString(R.string.explorer_open_file), mListItem.title));
                alertDialogBuilder.setNeutralButton(android.R.string.ok, null);
                break;
            case OVERRIDE_SEC2:
                alertDialogBuilder.setMessage(String
                        .format(getString(R.string.explorer_override_file),
                                mListItem.title));
                alertDialogBuilder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int which) {
                                mOverride = true;
                                showDialog(ENCRYPT);
                            }
                        });
                alertDialogBuilder.setNegativeButton(android.R.string.cancel, null);
                break;
            case OVERRIDE_DOWNLOAD:
                alertDialogBuilder.setMessage(String.format(
                        getString(R.string.explorer_override_file_download),
                        mListItem.title));
                alertDialogBuilder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int which) {
                                new GetFileTask(ExplorerActivity.this,
                                        mListItem.title).execute();
                            }
                        });
                alertDialogBuilder.setNegativeButton(android.R.string.cancel, null);
                break;
            case ENCRYPT:
                alertDialogBuilder
                        .setMessage(String.format(
                                getString(R.string.explorer_encrypt_file),
                                mListItem.title));
                alertDialogBuilder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int which) {
                                mEncrypt = true;
                                saveFile((FileItem) mListItem, Lock.LOCKED);
                            }
                        });
                alertDialogBuilder.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int which) {
                                mEncrypt = false;
                                saveFile((FileItem) mListItem, Lock.UNLOCKED);
                            }
                        });
                break;
        }

        return alertDialogBuilder.create();
    }

    /**
     * Tasks for upload and download of files
     */
    private void validateAndSave(final ListItem item) {
        if (FileHelper.isAllowed(item.file)) {
            final FilesDbHandler db = new FilesDbHandler(ExplorerActivity.this,
                    true);
            if (db != null) {
                if (db.fileExists(item.title)) {
                    db.close();
                    showDialog(OVERRIDE_SEC2);
                } else {
                    db.close();
                    showDialog(ENCRYPT);
                }
            }
        } else {
            showDialog(FILESIZE_EXCEEDED);
        }
    }

    private void validateAndDownload(final ListItem item) {
        if (!FileHelper.fileExists(FileHelper.DOWNLOAD_FOLDER_PATH, item.title)) {
            new GetFileTask(this, item.title).execute();
        } else {
            showDialog(OVERRIDE_DOWNLOAD);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode,
            final int resultCode, final Intent data) {
        String xmlDocument = null;
        switch (resultCode) {
            case Activity.RESULT_OK:
                xmlDocument = data.getStringExtra(Constants.INTENT_EXTRA_RESULT);
                if (xmlDocument != null && !xmlDocument.isEmpty()) {
                    saveFile(xmlDocument);
                }
                break;
            case Activity.RESULT_CANCELED:
                mOverride = false;
                break;
        }
    }

    private void saveFile(final FileItem fileItem, final Lock lock) {
        new UploadTask(ExplorerActivity.this, fileItem, lock).execute();
    }

    private void saveFile(final String xmlContent) {
        Log.e(this.getClass().getSimpleName(), "saveFile");
        final MwAdapterPreferenceManager prefManager = new MwAdapterPreferenceManager(
                ExplorerActivity.this);
        final String key = prefManager.getAppAuthKey();
        final String algorithm = prefManager.getAppAuthKeyAlgorithm();
        final String fileName = mFileCreator.getDocumentName();
        String path = null;
        PutRequestProperties requestProperties = null;
        PutRequestToUrlTask task = null;

        if (key != null && algorithm != null) {
            path = prefManager.getCloudPath();
            if (path.isEmpty() || path.endsWith("/")) {
                requestProperties = new PutRequestProperties(
                        prefManager.getCloudHostName(), path + fileName,
                        prefManager.getCloudPort(), xmlContent);
            } else {
                requestProperties = new PutRequestProperties(
                        prefManager.getCloudHostName(), path + "/" + fileName,
                        prefManager.getCloudPort(), xmlContent);
            }
            task = new PutRequestToUrlTask(ExplorerActivity.this, fileName,
                    key, algorithm, getApplication().getPackageName(),
                    prefManager.getMiddlewarePort(), requestProperties);
            task.execute();
        } else {
            showDialog(NO_KEY_OR_ALGORITHM);
        }
    }

    private void deleteFile(final ListItem listItem) {
        final MwAdapterPreferenceManager prefManager = new MwAdapterPreferenceManager(
                ExplorerActivity.this);
        final String key = prefManager.getAppAuthKey();
        final String algorithm = prefManager.getAppAuthKeyAlgorithm();
        DeleteRequestProperties requestProperties = null;
        String path = null;
        final String fileName = listItem.title + ".xml";

        DeleteRequestToUrlTask deleteRequestToUrlTask = null;

        if (key != null && algorithm != null) {
            path = prefManager.getCloudPath();
            if (path.isEmpty() || path.endsWith("/")) {
                requestProperties = new DeleteRequestProperties(
                        prefManager.getCloudHostName(), path + fileName,
                        prefManager.getCloudPort());
            } else {
                requestProperties = new DeleteRequestProperties(
                        prefManager.getCloudHostName(), path + "/" + fileName,
                        prefManager.getCloudPort());
            }
            deleteRequestToUrlTask = new DeleteRequestToUrlTask(
                    ExplorerActivity.this, listItem, key, algorithm,
                    getApplication().getPackageName(),
                    prefManager.getMiddlewarePort(), requestProperties);
            deleteRequestToUrlTask.execute();

        } else {
            showDialog(NO_KEY_OR_ALGORITHM);
        }
    }

    private class UploadTask extends
            Service.AbstractSec2Task<Void, String, String> {

        private final FileItem mFileItem;
        private final Lock mLock;

        protected UploadTask(final Activity activity, final FileItem fileItem,
                final Lock lock) {
            super(activity, "Enkodiere...", true);
            this.mFileItem = fileItem;
            this.mLock = lock;
        }

        @Override
        protected String doInBackground(final Void... params) {
            try {
                final Sec2File sec2File = new Sec2File();
                sec2File.setName(mFileItem.title);
                sec2File.setCreationDate(new GregorianCalendar());
                sec2File.setPlainText(FileHelper.encodeBase64(mFileItem.file));
                sec2File.setLock(mLock);
                switch (mLock) {
                    case UNLOCKED:
                        sec2File.setPartsToEncrypt(new String[] {});
                        break;
                    case LOCKED:
                        sec2File.setPartsToEncrypt(sec2File.getPlainText().clone());
                        break;
                }
                mFileCreator = new FileDomDocumentCreator(sec2File);
                String fileName = sec2File.getName() + ".xml"; // "sec2File_" +
                                                               // subject + "_"
                                                               // +
                                                               // sec2File.getCreationDate().getTimeInMillis()
                                                               // + ".xml";
                fileName = fileName.replaceAll("\\s", "_");
                mFileCreator.setDocumentName(fileName);
                mFileCreator.setFile(sec2File);
                final String xmlContent = mFileCreator
                        .createDomDocument(new CheckedGroupHandler());
                return xmlContent;
            } catch (final Exception e) {
                mException = e;
                return null;
            }
        }

        @Override
        protected void onPostExecuteWithoutException(final String result) {
            super.onPostExecuteWithoutException(result);
            if (result != null && result.length() > 0) {
                if (mEncrypt) {
                    final Intent intent = new Intent();
                    intent.setClass(ExplorerActivity.this,
                            GroupChooserActivity.class);
                    intent.putExtra(Constants.INTENT_EXTRA_DOM, mFileCreator);
                    startActivityForResult(intent, RESULT_ID);
                } else {
                    saveFile(result);
                }
            }
        }

        @Override
        protected void onPostExecuteWithException(final String result) {
            super.onPostExecuteWithException(result);
            showDialog(ERROR);
        }

    }

    private class PutRequestToUrlTask extends Service.PutRequestToUrl {

        private final String fileName;

        public PutRequestToUrlTask(final Activity activity,
                final String fileName, final String key,
                final String algorithm, final String appName, final int port,
                final PutRequestProperties properties) {
            super(activity, key, algorithm, appName, port, properties, activity
                    .getString(R.string.service_file));
            this.fileName = fileName;
        }

        @Override
        protected void onPostExecuteWithoutException(final InputStream result) {
            super.onPostExecuteWithoutException(result);
            if (!mOverride) {
                final String shortFileName = fileName.replace(".xml", "");
                final FilesDbHandler db = new FilesDbHandler(
                        ExplorerActivity.this, true);
                if (db.fileExists(fileName) || db.saveFileName(shortFileName)) {
                    showDialog(SAVE_SUCCESS);
                } else {
                    showDialog(DB_ERROR);
                }
                db.close();
                mAdapter.addSec2(shortFileName);
                mOverride = false;
            } else {
                showDialog(SAVE_SUCCESS);
            }

        }

        @Override
        protected void onPostExecuteWithException(final InputStream result) {
            super.onPostExecuteWithException(result);
            if (mException.getMessage() != null) {
                mErrorMessage = mException.getMessage();
            } else {
                mErrorMessage = "";
            }
            showDialog(ERROR);
        }

    }

    private class GetFileTask extends Service.GetFileTask {

        protected GetFileTask(final Activity activity, final String fileName) {
            super(activity, fileName, new TaskProgressDialog(activity));
        }

        @Override
        protected void onPostExecuteWithoutException(final File result) {
            super.onPostExecuteWithoutException(result);
            if (result != null) {
                mAdapter.addDownload(result);
            }
            showDialog(SAVE_SUCCESS);
        }

        @Override
        protected void onPostExecuteWithException(final File result) {
            super.onPostExecuteWithException(result);
            showDialog(ERROR);
        }
    }

    private class DeleteRequestToUrlTask extends Service.DeleteRequestToUrl {

        private final ListItem listItem;

        protected DeleteRequestToUrlTask(final Activity activity,
                final ListItem listItem, final String key,
                final String algorithm, final String appName, final int port,
                final DeleteRequestProperties properties) {
            super(activity, key, algorithm, appName, port, properties, activity
                    .getString(R.string.service_file));
            this.listItem = listItem;
        }

        @Override
        protected void onPostExecuteWithoutException(final InputStream result) {
            super.onPostExecuteWithoutException(result);
            FilesDbHandler db = null;
            db = new FilesDbHandler(ExplorerActivity.this, true);
            if (db.deleteFileName(listItem.title) > 0) {
                mAdapter.remove(listItem);
                showDialog(DELETE_SUCCESS);
            } else {
                showDialog(DB_ERROR);
            }
            db.close();
        }

        @Override
        protected void onPostExecuteWithException(final InputStream result) {
            super.onPostExecuteWithException(result);
            if (mException.getMessage() != null) {
                mErrorMessage = mException.getMessage();
            } else {
                mErrorMessage = "";
            }
            showDialog(ERROR);
        }

    }

}


package de.adesso.mobile.android.sec2;

import java.io.InputStream;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import de.adesso.mobile.android.sec2.activity.Sec2ListActivity;
import de.adesso.mobile.android.sec2.adapter.NoticeListAdapter;
import de.adesso.mobile.android.sec2.dialog.TaskProgressDialog;
import de.adesso.mobile.android.sec2.mwadapter.DeleteRequestProperties;
import de.adesso.mobile.android.sec2.mwadapter.util.MwAdapterPreferenceManager;
import de.adesso.mobile.android.sec2.service.Service;
import de.adesso.mobile.android.sec2.util.AlertHelper;
import de.adesso.mobile.android.sec2.util.Constants;
import de.adesso.mobile.android.sec2.util.LogHelper;
import de.adesso.mobile.android.sec2.util.NoticeDomDocumentCreator;

/**
 * NoticeActivity
 * 
 * @author bruch
 */
public class NoticeListActivity extends Sec2ListActivity {

    private static final int NO_KEY_OR_ALGORITHM = 0;
    private static final int ERROR = 2;
    private static final int SUCCESS = 3;

    private ListView mListView;
    private NoticeListAdapter mNoticeListAdapter;
    private String mErrorMessage = null;
    private static final int CREATE_NOTE = 1093;
    private static final int VIEW_NOTE = 1092;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.notice);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.titlebar);

        initTitle();
        initListeners();
    }

    private void initTitle() {
        findViewById(R.id.titlebar_add).setVisibility(View.VISIBLE);
        mListView = (ListView) findViewById(android.R.id.list);
    }

    private void initListeners() {
        ((ImageButton) findViewById(R.id.titlebar_add))
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(final View v) {
                        final Intent intent = new Intent();
                        intent.setClass(NoticeListActivity.this,
                                NoticeCreateActivity.class);
                        startActivityForResult(intent, CREATE_NOTE);
                    }
                });

        mNoticeListAdapter = new NoticeListAdapter(this);
        mListView.setAdapter(mNoticeListAdapter);

        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> parent,
                    final View view, final int position, final long id) {
                final Intent noticeCreateIntent = new Intent(
                        NoticeListActivity.this, NoticeCreateActivity.class);
                noticeCreateIntent.putExtra(Constants.INTENT_EXTRA_NOTICE,
                        mNoticeListAdapter.getItem(position));
                startActivityForResult(noticeCreateIntent, VIEW_NOTE);
            }
        });
        registerForContextMenu(mListView);

        new GetNoticeListTask(this).execute();
    }

    @Override
    protected void onActivityResult(final int requestCode,
            final int resultCode, final Intent data) {

        NoticeDomDocumentCreator notice = null;

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CREATE_NOTE:
                    notice = (NoticeDomDocumentCreator) (data
                            .getSerializableExtra(Constants.INTENT_EXTRA_RESULT));
                    if (notice != null) {
                        mNoticeListAdapter.add(notice);
                    }
                    break;
                case VIEW_NOTE:
                    notice = (NoticeDomDocumentCreator) (data
                            .getSerializableExtra(Constants.INTENT_EXTRA_RESULT));
                    if (notice != null) {
                        mNoticeListAdapter.update(notice);
                    }
                    break;
                case PREFERENCES:
                    new GetNoticeListTask(this).execute();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public final void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.notice_context_menu, menu);
    }

    @Override
    public final boolean onContextItemSelected(final MenuItem item) {
        final AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        Intent intent = null;

        switch (item.getItemId()) {
            case R.id.open:
                intent = new Intent(NoticeListActivity.this,
                        NoticeCreateActivity.class);
                intent.putExtra(Constants.INTENT_EXTRA_NOTICE,
                        mNoticeListAdapter.getItem(menuInfo.position));
                startActivityForResult(intent, VIEW_NOTE);
                return true;
            case R.id.delete:
                deleteNotice(menuInfo.position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected final Dialog onCreateDialog(final int id) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        alertDialogBuilder.setTitle(R.string.notice_delete_success_title);

        switch (id) {
            case SUCCESS:
                alertDialogBuilder.setMessage(R.string.notice_delete_success_true);
                break;
            case NO_KEY_OR_ALGORITHM:
                alertDialogBuilder
                        .setMessage(R.string.notice_delete_success_false_key);
                break;
            case ERROR:
                alertDialogBuilder
                        .setMessage(getString(R.string.notice_delete_success_false_error)
                                + mErrorMessage);
                break;
            default:
                break;
        }
        alertDialogBuilder.setNeutralButton(R.string.ok, null);
        alertDialogBuilder.setCancelable(false);

        return alertDialogBuilder.create();
    }

    private void deleteNotice(final int noticePosition) {
        final NoticeDomDocumentCreator noticeDocument = mNoticeListAdapter
                .getItem(noticePosition);
        final MwAdapterPreferenceManager prefManager = new MwAdapterPreferenceManager(
                NoticeListActivity.this);
        final String key = prefManager.getAppAuthKey();
        final String algorithm = prefManager.getAppAuthKeyAlgorithm();
        DeleteRequestProperties requestProperties = null;
        final String noticeName = noticeDocument.getDocumentName();
        String path = null;

        if (key != null && algorithm != null) {
            path = prefManager.getCloudPath();
            if (path.isEmpty() || path.endsWith("/")) {
                requestProperties = new DeleteRequestProperties(
                        prefManager.getCloudHostName(), path + noticeName,
                        prefManager.getCloudPort());
            } else {
                requestProperties = new DeleteRequestProperties(
                        prefManager.getCloudHostName(),
                        path + "/" + noticeName, prefManager.getCloudPort());
            }
            new DeleteRequestToUrlTask(NoticeListActivity.this, noticeName,
                    noticePosition, key, algorithm, getApplication()
                            .getPackageName(), prefManager.getMiddlewarePort(),
                    requestProperties).execute();
        } else {
            showDialog(NO_KEY_OR_ALGORITHM);
        }
    }

    /**
     * 
     * DeleteRequestToUrlTask
     * 
     * @author hoppe
     * 
     */
    private class DeleteRequestToUrlTask extends Service.DeleteRequestToUrl {

        private int noticePosition = -1;

        protected DeleteRequestToUrlTask(final Activity activity,
                final String noticeName, final int noticePosition,
                final String key, final String algorithm, final String appName,
                final int port, final DeleteRequestProperties properties) {
            super(activity, key, algorithm, appName, port, properties, activity
                    .getString(R.string.service_notice));
            this.noticePosition = noticePosition;
        }

        @Override
        protected void onPostExecuteWithoutException(final InputStream result) {
            super.onPostExecuteWithoutException(result);
            mNoticeListAdapter.remove(noticePosition);
            showDialog(SUCCESS);
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
            LogHelper.logE(mErrorMessage);
        }

    }

    /**
     * GetNoticeListTask
     * 
     * @author hoppe
     * 
     */
    private class GetNoticeListTask extends Service.GetNoticeListTask {

        public GetNoticeListTask(final Activity activity) {
            super(activity, new TaskProgressDialog(activity));
        }

        @Override
        protected void onPostExecuteWithoutException(
                final List<NoticeDomDocumentCreator> result) {
            super.onPostExecuteWithoutException(result);
            mNoticeListAdapter.addAll(result);
        }

        @Override
        protected void onPostExecuteWithException(
                final List<NoticeDomDocumentCreator> result) {
            super.onPostExecuteWithException(result);
            mNoticeListAdapter.clear();
            AlertHelper.showAlertDialog(mActivity,
                    mActivity.getString(R.string.error),
                    mException.getMessage());
        }

    }

}

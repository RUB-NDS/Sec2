
package de.adesso.mobile.android.sec2;

import java.io.InputStream;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
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
import de.adesso.mobile.android.sec2.adapter.TaskListAdapter;
import de.adesso.mobile.android.sec2.dialog.TaskProgressDialog;
import de.adesso.mobile.android.sec2.mwadapter.DeleteRequestProperties;
import de.adesso.mobile.android.sec2.mwadapter.util.MwAdapterPreferenceManager;
import de.adesso.mobile.android.sec2.service.Service;
import de.adesso.mobile.android.sec2.util.AlertHelper;
import de.adesso.mobile.android.sec2.util.Constants;
import de.adesso.mobile.android.sec2.util.LogHelper;
import de.adesso.mobile.android.sec2.util.TaskDomDocumentCreator;

/**
 * @author benner/hoppe
 * 
 */
public class TaskListActivity extends Sec2ListActivity {

    private static final String TAG = "TaskListActivity";

    private static final int NO_KEY_OR_ALGORITHM = 0;
    private static final int ERROR = 2;
    private static final int SUCCESS = 3;

    private static final int CREATE_TASK = 1093;
    private static final int VIEW_TASK = 1092;

    private ListView mListView;
    private TaskListAdapter mTaskListAdapter;

    private String mErrorMessage = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.task);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.titlebar);

        initTitle();
        initListeners();
    }

    private void initTitle() {
        findViewById(R.id.titlebar_add).setVisibility(View.VISIBLE);
        mListView = (ListView) findViewById(android.R.id.list);
        mListView.addHeaderView(
                LayoutInflater.from(this).inflate(R.layout.task_item_headline,
                        null), null, false);
    }

    private void initListeners() {
        ((ImageButton) findViewById(R.id.titlebar_add))
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(final View v) {
                        final Intent intent = new Intent();
                        intent.setClass(TaskListActivity.this,
                                TaskCreateActivity.class);
                        startActivityForResult(intent, CREATE_TASK);
                    }
                });

        mTaskListAdapter = new TaskListAdapter(this);
        mListView.setAdapter(mTaskListAdapter);

        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> parent,
                    final View view, final int position, final long id) {
                final Intent eventIntent = new Intent(TaskListActivity.this,
                        TaskCreateActivity.class);
                eventIntent.putExtra(Constants.INTENT_EXTRA_TASK,
                        mTaskListAdapter.getItem(position - 1));
                startActivityForResult(eventIntent, VIEW_TASK);
            }
        });
        registerForContextMenu(mListView);

        new GetTaskListTask(TaskListActivity.this).execute();
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.notice_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        Intent eventIntent = null;

        switch (item.getItemId()) {
            case R.id.open:
                eventIntent = new Intent(TaskListActivity.this,
                        TaskCreateActivity.class);
                eventIntent.putExtra(Constants.INTENT_EXTRA_TASK,
                        mTaskListAdapter.getItem(menuInfo.position - 1));
                startActivityForResult(eventIntent, VIEW_TASK);
                return true;
            case R.id.delete:
                deleteTask(menuInfo.position - 1);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode,
            final int resultCode, final Intent data) {
        LogHelper.logE("requestCode: " + requestCode + " resultCode: "
                + resultCode + " data: " + data);
        TaskDomDocumentCreator task = null;

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CREATE_TASK:
                    task = (TaskDomDocumentCreator) (data
                            .getSerializableExtra(Constants.INTENT_EXTRA_RESULT));
                    if (task != null) {
                        mTaskListAdapter.add(task);
                    }
                    break;
                case VIEW_TASK:
                    task = (TaskDomDocumentCreator) (data
                            .getSerializableExtra(Constants.INTENT_EXTRA_RESULT));
                    if (task != null) {
                        mTaskListAdapter.update(task);
                    }
                    break;
                case PREFERENCES:
                    new GetTaskListTask(TaskListActivity.this).execute();
                    break;
                default:
                    break;
            }
        }
    }

    private void deleteTask(final int taskPosition) {
        final TaskDomDocumentCreator taskDocument = mTaskListAdapter
                .getItem(taskPosition);
        final MwAdapterPreferenceManager prefManager = new MwAdapterPreferenceManager(
                TaskListActivity.this);
        final String key = prefManager.getAppAuthKey();
        final String algorithm = prefManager.getAppAuthKeyAlgorithm();
        DeleteRequestProperties requestProperties = null;
        final String taskName = taskDocument.getDocumentName();
        String path = null;
        DeleteRequestToUrlTask deleteRequestToUrlTask = null;

        if (key != null && algorithm != null) {
            path = prefManager.getCloudPath();
            if (path.isEmpty() || path.endsWith("/")) {
                requestProperties = new DeleteRequestProperties(
                        prefManager.getCloudHostName(), path + taskName,
                        prefManager.getCloudPort());
            } else {
                requestProperties = new DeleteRequestProperties(
                        prefManager.getCloudHostName(), path + "/" + taskName,
                        prefManager.getCloudPort());
            }
            deleteRequestToUrlTask = new DeleteRequestToUrlTask(this, taskName,
                    taskPosition, key, algorithm, getApplication()
                            .getPackageName(), prefManager.getMiddlewarePort(),
                    requestProperties);
            deleteRequestToUrlTask.execute();

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

        private String taskName = "";
        private int taskPosition = -1;

        protected DeleteRequestToUrlTask(final Activity activity,
                final String taskName, final int taskPosition,
                final String key, final String algorithm, final String appName,
                final int port, final DeleteRequestProperties properties) {
            super(activity, key, algorithm, appName, port, properties, activity
                    .getString(R.string.service_task));
            this.taskName = taskName;
            this.taskPosition = taskPosition;
        }

        @Override
        protected void onPostExecuteWithoutException(final InputStream result) {
            super.onPostExecuteWithoutException(result);
            mTaskListAdapter.remove(taskPosition);
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
     * GetTaskListTask
     * 
     * @author hoppe
     */
    private class GetTaskListTask extends Service.GetTaskListTask {

        protected GetTaskListTask(final Activity activity) {
            super(activity, new TaskProgressDialog(activity));
        }

        @Override
        protected void onPostExecuteWithoutException(
                final List<TaskDomDocumentCreator> result) {
            super.onPostExecuteWithoutException(result);
            for (int i = 0; i < result.size(); i++) {
                Log.e(TAG, result.get(i).getTask().toString());
            }
            mTaskListAdapter.addAll(result);
        }

        @Override
        protected void onPostExecuteWithException(
                final List<TaskDomDocumentCreator> result) {
            super.onPostExecuteWithException(result);
            mTaskListAdapter.clear();
            AlertHelper.showAlertDialog(mActivity,
                    mActivity.getString(R.string.error),
                    mException.getMessage());
        }

    }

    @Override
    protected final Dialog onCreateDialog(final int id) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        alertDialogBuilder.setTitle(R.string.notice_delete_success_title);

        switch (id) {
            case SUCCESS:
                alertDialogBuilder.setMessage(R.string.task_delete_success_true);
                break;
            case NO_KEY_OR_ALGORITHM:
                alertDialogBuilder
                        .setMessage(R.string.task_delete_success_false_key);
                break;
            case ERROR:
                alertDialogBuilder
                        .setMessage(getString(R.string.task_delete_success_false_error)
                                + mErrorMessage);
                break;
            default:
                break;
        }
        alertDialogBuilder.setNeutralButton(R.string.ok, null);
        alertDialogBuilder.setCancelable(false);

        return alertDialogBuilder.create();
    }
    
}

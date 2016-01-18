package de.adesso.mobile.android.sec2.mwadapter.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import de.adesso.mobile.android.sec2.mwadapter.MwAdapter;
import de.adesso.mobile.android.sec2.mwadapter.model.Group;
import de.adesso.mobile.android.sec2.mwadapter.tasks.MwAdapterTask;
import de.adesso.mobile.android.sec2.mwadapter.util.GroupHandler;
import de.adesso.mobile.android.sec2.mwadapter.util.MwAdapterPreferenceManager;
import de.adesso.mobile.android.sec2.mwapdater.R;

/**
 * 
 * @author schuessler
 * deprecated: The class shouldn't be used anymore.
 */
@Deprecated
public class GroupInfoActivity extends Activity
{
    private static final int POPUP_NO_KEY_OR_ALGORITHM = 0;
    private static final int POPUP_ERROR = 1;

    private final String errorMessage = null;

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.groupinfo);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.mwadaptertitlebar);
        ((TextView)findViewById(R.id.title_lbl)).setText(R.string.groupinfo_title);

        new InitViewTask().execute();
    }

    @Override
    protected Dialog onCreateDialog(final int id)
    {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(R.string.groupinfo_success_title);

        switch(id)
        {
            case POPUP_NO_KEY_OR_ALGORITHM:
                alertDialogBuilder.setMessage(R.string.groupinfo_success_false_key);
                break;
            case POPUP_ERROR:
                alertDialogBuilder.setMessage(getString(R.string.groupinfo_success_false_error) + errorMessage);
                break;
        }
        alertDialogBuilder.setNeutralButton(R.string.ok, null);
        alertDialogBuilder.setCancelable(false);

        return alertDialogBuilder.create();
    }

    private class OnGroupClickListener implements OnItemClickListener
    {
        private final GroupInfoActivity activity;
        private final GroupHandler groupHandler;

        public OnGroupClickListener(final GroupInfoActivity activity, final GroupHandler groupHandler)
        {
            this.activity = activity;
            this.groupHandler = groupHandler;
        }

        @Override
        public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
        {
            final Intent intent = new Intent(activity, UsersInGroupInfoActivity.class);
            intent.putExtra("groupId", groupHandler.getGroupIds()[position]);
            intent.putExtra("groupName", groupHandler.getGroupNames()[position]);
            startActivity(intent);
        }
    }

    private final class InitViewTask extends MwAdapterTask<Group[]>
    {
        @Override
        protected void onProgressUpdate(final Integer... values)
        {
            super.onProgressUpdate(values);
            showDialog(values[0]);
        }

        @Override
        protected Group[] doInBackground(final Void... params)
        {
            final MwAdapter adapter = MwAdapter.getInstance();
            final MwAdapterPreferenceManager prefManager = new MwAdapterPreferenceManager(getApplicationContext());
            final String key = prefManager.getAppAuthKey();
            final String algorithm = prefManager.getAppAuthKeyAlgorithm();
            try {
                if (key != null && algorithm != null)
                {
                    return adapter.getGroupsForRegisteredUser(key, algorithm, getApplication().getPackageName(), prefManager.getMiddlewarePort());
                }
                else
                {
                    publishProgress(POPUP_NO_KEY_OR_ALGORITHM);
                }
            }
            catch (final Exception e)
            {
                exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecuteWithException(final Group[] result)
        {
            super.onPostExecuteWithException(result);
            showDialog(POPUP_ERROR);
        }

        @Override
        protected void onPostExecuteWithoutException(final Group[] result)
        {
            super.onPostExecuteWithoutException(result);
            final ListView list = (ListView) findViewById(R.id.group_list);
            final GroupHandler groupHandler = new GroupHandler(result);
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(GroupInfoActivity.this, R.layout.list_item, groupHandler.getGroupNames());
            list.setAdapter(adapter);
            list.setOnItemClickListener(new OnGroupClickListener(GroupInfoActivity.this, groupHandler));
        }
    }
}

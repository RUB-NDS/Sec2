package de.adesso.mobile.android.sec2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import de.adesso.mobile.android.sec2.activity.Sec2Activity;
import de.adesso.mobile.android.sec2.mwadapter.model.Group;
import de.adesso.mobile.android.sec2.mwadapter.tasks.GetGroupsForRegisteredUserTask;
import de.adesso.mobile.android.sec2.mwadapter.util.MwAdapterPreferenceManager;
import de.adesso.mobile.android.sec2.util.AbstractDomDocumentCreator;
import de.adesso.mobile.android.sec2.util.CheckedGroupHandler;
import de.adesso.mobile.android.sec2.util.Constants;
import de.adesso.mobile.android.sec2.util.LogHelper;

/**
 * The activity shows a list with groups which can be selected through checkboxes.
 * 
 * @author nike
 */
public class GroupChooserActivity extends Sec2Activity {

    private static final Class<?> c = NoticeCreateActivity.class;
    private static final int NO_KEY_OR_ALGORITHM = 0;
    private static final int ERROR = 1;
    private static final int DB_ERROR = 2;
    private static final int NO_DOM_CREATOR = 3;
    private static final int SUCCESS = 4;

    private String errorMessage = null;
    private ListView list = null;
    private CheckedGroupHandler groupHandler = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.group_chooser);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);

        list = (ListView) findViewById(R.id.group_chooser_list);
        groupHandler = new CheckedGroupHandler(getGroups());
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.check_list_item, groupHandler.getGroupNames());
        list.setAdapter(adapter);

        LogHelper.logV(c, "onCreate");
    }

    private Group[] getGroups() {
        final MwAdapterPreferenceManager prefManager = new MwAdapterPreferenceManager(
                getApplicationContext());
        final String key = prefManager.getAppAuthKey();
        final String algorithm = prefManager.getAppAuthKeyAlgorithm();
        Group[] groups = null;
        GetGroupsForRegisteredUserTask task = null;

        try {
            if (key != null && algorithm != null) {
                task = new GetGroupsForRegisteredUserTask(key, algorithm, getApplication()
                        .getPackageName(), prefManager.getMiddlewarePort());
                task.execute();
                groups = task.get();
                if (task.getException() != null) {
                    throw task.getException();
                }
            } else {
                showDialog(NO_KEY_OR_ALGORITHM);
            }
        } catch (final Exception e) {
            if (e.getMessage() != null) {
                errorMessage = e.getMessage();
            } else {
                errorMessage = "";
            }
            showDialog(ERROR);
            LogHelper.logE(errorMessage);
            setResult(Activity.RESULT_CANCELED);
        }

        return groups;
    }

    public void onCheckboxClicked(final View view) {
        if (view instanceof CheckBox) {
            final int position = list.getPositionForView(view);

            if (position != AdapterView.INVALID_POSITION) {
                groupHandler.setChecked(position, ((CheckBox) view).isChecked());
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(R.string.group_chooser_success_title);

        switch (id) {
            case SUCCESS:
                alertDialogBuilder.setMessage(R.string.group_chooser_success_true);
                break;
            case NO_KEY_OR_ALGORITHM:
                alertDialogBuilder.setMessage(R.string.group_chooser_success_false_key);
                break;
            case DB_ERROR:
                alertDialogBuilder.setMessage(R.string.group_chooser_success_false_db);
                break;
            case ERROR:
                alertDialogBuilder.setMessage(getString(R.string.group_chooser_success_false_error)
                        + errorMessage);
                break;
            case NO_DOM_CREATOR:
                alertDialogBuilder.setMessage(R.string.group_chooser_success_false_dom);
                break;
        }
        alertDialogBuilder.setNeutralButton(R.string.ok, new OnOkClickListener(this));
        alertDialogBuilder.setCancelable(false);

        return alertDialogBuilder.create();
    }

    public void saveNotice(final View view) {
        final AbstractDomDocumentCreator domCreator = (AbstractDomDocumentCreator) (getIntent()
                .getSerializableExtra(Constants.INTENT_EXTRA_DOM));
        Intent result = null;
        boolean showDialog = false;

        try {
            if (domCreator != null) {
                result = new Intent();
                result.putExtra(Constants.INTENT_EXTRA_RESULT,
                        domCreator.createDomDocument(groupHandler));
                setResult(Activity.RESULT_OK, result);
            } else {
                showDialog = true;
                showDialog(NO_DOM_CREATOR);
                setResult(Activity.RESULT_CANCELED);
            }
        } catch (final Exception e) {
            if (e.getMessage() != null) {
                errorMessage = e.getMessage();
            } else {
                errorMessage = "";
            }
            showDialog = true;
            showDialog(ERROR);
            LogHelper.logE(e.getMessage());
            setResult(Activity.RESULT_CANCELED);
        }
        if (!showDialog) {
            finish();
        }
    }

    private class OnOkClickListener implements DialogInterface.OnClickListener {

        private final GroupChooserActivity activity;

        public OnOkClickListener(final GroupChooserActivity activity) {
            this.activity = activity;
        }

        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            if (activity != null) {
                activity.finish();
            }
        }
    }
}

package org.sec2.android.app;

import java.text.MessageFormat;

import org.sec2.android.app.component.CheckedGroupHandler;
import org.sec2.android.util.Constants;
import org.sec2.android.util.Sec2MiddlewarePreferenceManager;
import org.sec2.middleware.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import de.adesso.mobile.android.sec2.mwadapter.logging.LogHelper;
import de.adesso.mobile.android.sec2.mwadapter.model.Group;
import de.adesso.mobile.android.sec2.mwadapter.model.User;
import de.adesso.mobile.android.sec2.mwadapter.tasks.GetGroupTask;
import de.adesso.mobile.android.sec2.mwadapter.tasks.GetGroupsForRegisteredUserTask;
import de.adesso.mobile.android.sec2.mwadapter.tasks.GetGroupsForUserTask;
import de.adesso.mobile.android.sec2.mwadapter.tasks.GetRegisteredUserTask;

/**
 * The activity shows a list with groups which can be selected through
 * checkboxes.
 *
 * @author nike
 */
public class GroupChooserActivity extends Activity
{
    private static final Class<?> CLAZZ = GroupChooserActivity.class;
    private static final int POPUP_NO_KEY = 0;
    private static final int POPUP_NO_ALGORITHM = 1;
    private static final int POPUP_NO_TOKEN = 2;
    private static final int POPUP_REQUEST_ERROR = 3;
    private static final int POPUP_SAVE_ERROR = 4;
    private static final int POPUP_SUCCESS = 5;
    private static final int POPUP_FAIL = 6;

    private String errorMessage = null;
    private ListView list = null;
    private Button saveBtn = null;
    private CheckedGroupHandler groupHandler = null;
    private User user = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        setContentView(R.layout.group_chooser);

        saveBtn = (Button)(findViewById(R.id.group_chooser_save_btn));
        saveBtn.setEnabled(false);

        setTextView();
        setListView();
    }

    private void setTextView()
    {
        final TextView intro =
                (TextView)(findViewById(R.id.group_chooser_intro));

        user = getIntent().getParcelableExtra(Constants.INTENT_EXTRA_USER);
        if (user != null)
        {
            intro.setText(MessageFormat.format(getString(
                    R.string.group_chooser_intro), user.getUserName()));
        }
        else
        {
            intro.setText(R.string.group_chooser_intro_no_user);
            intro.setTextColor(Color.RED);
            LogHelper.logD(CLAZZ, "Extra \"" + Constants.INTENT_EXTRA_USER
                    + "\" war NULL");
        }
    }

    private void setListView()
    {
        Sec2MiddlewarePreferenceManager prefManager = null;
        ArrayAdapter<String> arrayAdapter = null;
        Group[] registeredUserGroups = null;
        Group[] userGroups = null;
        Group[] result = null;
        Group tmpGroup = null;
        User registeredUser = null;
        String key = null;
        String algorithm = null;
        String token = null;
        GetGroupsForRegisteredUserTask ggfruTask = null;
        GetGroupsForUserTask ggfuTask = null;
        GetRegisteredUserTask gruTask = null;
        GetGroupTask ggTask = null;
        int countGroups;
        int port;
        int position = 0;

        list = (ListView)findViewById(R.id.group_chooser_list);
        if (user != null)
        {
            prefManager = new Sec2MiddlewarePreferenceManager(
                    getApplicationContext());
            key = prefManager.getSessionKey();
            if (key != null)
            {
                algorithm = prefManager.getSessionKeyAlgorithm();
                if (algorithm != null)
                {
                    token = prefManager.getSessionToken();
                    if (token != null)
                    {
                        try
                        {
                            port = prefManager.getServerListenPort();
                            ggfruTask = new GetGroupsForRegisteredUserTask(key,
                                    algorithm, token, port);
                            ggfruTask.execute();
                            registeredUserGroups = ggfruTask.get();
                            if(ggfruTask.getException() != null)
                            {
                                throw ggfruTask.getException();
                            }
                            //We don't matter NPE here, because it is catched.
                            if (registeredUserGroups.length > 0)
                            {
                                ggfuTask = new GetGroupsForUserTask(key,
                                        algorithm, token, port,
                                        user.getUserId());
                                ggfuTask.execute();
                                userGroups = ggfuTask.get();
                                if(ggfuTask.getException() != null)
                                {
                                    throw ggfuTask.getException();
                                }
                                countGroups = registeredUserGroups.length;
                                //Remove groups from list of registered user's
                                //group, where the user is already a member,
                                //because these groups should not occur in the
                                //list of possible groups, where the user
                                //can be added.
                                for (int i = 0; i < userGroups.length; i++)
                                {
                                    for (int j = 0;
                                            j < registeredUserGroups.length;
                                            j++)
                                    {
                                        if (registeredUserGroups[j] != null
                                                && userGroups[i].getGroupId()
                                                .equals(registeredUserGroups[j]
                                                        .getGroupId()))
                                        {
                                            registeredUserGroups[j] = null;
                                            countGroups--;
                                            break;
                                        }
                                    }
                                }
                                //Furthermore remove groups from list of
                                //registered user's group, where the
                                //registered user isn't the group-owner.
                                if (countGroups > 0)
                                {
                                    gruTask = new GetRegisteredUserTask(key,
                                            algorithm, token, port);
                                    gruTask.execute();
                                    registeredUser = gruTask.get();
                                    if(gruTask.getException() != null)
                                    {
                                        throw gruTask.getException();
                                    }
                                    for (int i = 0;
                                            i < registeredUserGroups.length;
                                            i++)
                                    {
                                        if (registeredUserGroups[i] != null)
                                        {
                                            //If group-owner isn't set, try to
                                            //fetch him
                                            if (!registeredUserGroups[i]
                                                    .isGroupOwnerSet())
                                            {
                                                ggTask = new GetGroupTask(key,
                                                        algorithm, token, port,
                                                        registeredUserGroups[i]
                                                                .getGroupId());
                                                ggTask.execute();
                                                tmpGroup = ggTask.get();
                                                if(ggTask.getException()
                                                        != null)
                                                {
                                                    throw ggTask
                                                    .getException();
                                                }
                                                //If tmpGroup != null, update
                                                //entry in
                                                //registeredUserGroups-array
                                                if (tmpGroup != null)
                                                {
                                                    registeredUserGroups[i] =
                                                            tmpGroup;
                                                }
                                            }
                                            //Now check, who's the group-owner
                                            //and set array-entry to NULL, if
                                            //IDs of group-owner and registered
                                            //user aren't equal
                                            if (!registeredUserGroups[i]
                                                    .isGroupOwnerSet()
                                                    || registeredUserGroups[i]
                                                            .isGroupOwnerSet()
                                                            && !registeredUserGroups[i]
                                                                    .getGroupOwner()
                                                                    .getUserId()
                                                                    .equals(registeredUser.getUserId()))
                                            {
                                                registeredUserGroups[i] = null;
                                                countGroups--;
                                            }
                                        }
                                    }
                                }
                                if (countGroups > 0)
                                {
                                    result = new Group[countGroups];
                                    //Copy all remaining groups from
                                    //registeredUserGroups to the result array
                                    for (int i = 0;
                                            i < registeredUserGroups.length;
                                            i++)
                                    {
                                        if (registeredUserGroups[i] != null)
                                        {
                                            result[position] =
                                                    registeredUserGroups[i];
                                            position++;
                                        }
                                    }
                                    groupHandler =
                                            new CheckedGroupHandler(result);
                                    arrayAdapter =
                                            new ArrayAdapter<String>(this,
                                                    R.layout.check_list_item,
                                                    groupHandler.getGroupNames());
                                    list.setAdapter(arrayAdapter);
                                }
                            }
                        }
                        catch (final Exception e)
                        {
                            if (e.getMessage() != null)
                            {
                                errorMessage = e.getMessage();
                            }
                            else
                            {
                                errorMessage = "";
                            }
                            setResult(Activity.RESULT_CANCELED);
                            showDialog(POPUP_REQUEST_ERROR);
                            LogHelper.logE(CLAZZ, errorMessage);
                        }
                    }
                    else
                    {
                        setResult(Activity.RESULT_CANCELED);
                        showDialog(POPUP_NO_TOKEN);
                        LogHelper.logE(CLAZZ, "Kein Sitzungstoken in den "
                                + "Preferences gefunden!");
                    }
                }
                else
                {
                    setResult(Activity.RESULT_CANCELED);
                    showDialog(POPUP_NO_ALGORITHM);
                    LogHelper.logE(CLAZZ, "Kein Sitzungsalgorithmus in den "
                            + "Preferences gefunden!");
                }
            }
            else
            {
                setResult(Activity.RESULT_CANCELED);
                showDialog(POPUP_NO_KEY);
                LogHelper.logE(CLAZZ, "Kein Sitzungsschlüssel in den "
                        + "Preferences gefunden!");
            }
        }
    }

    /**
     * Handles a click on a checkbox. It marks the corresponding group as
     * checked, if the group wasn't checked before, or as unchecked otherwise.
     * Furthermore it enables the save-button, if at least one group is marked
     * as checked, or disables the button otherwise.
     *
     * @param view - The view calling this method
     */
    public void onCheckboxClicked(final View view)
    {
        final int position = list.getPositionForView(view);

        if (groupHandler != null)
        {
            if (position != AdapterView.INVALID_POSITION)
            {
                groupHandler.setChecked(position,
                        ((CheckBox)view).isChecked());
                if (groupHandler.getNumberOfCheckedGroups() > 0)
                {
                    saveBtn.setEnabled(true);
                }
                else
                {
                    saveBtn.setEnabled(false);
                }
            }
            else
            {
                LogHelper.logE(CLAZZ, "Postion " + position
                        + " war nicht gültig!");
            }
        }
        else
        {
            LogHelper.logE(CLAZZ, "Variable \"groupHandler\" war NULL!");
        }
    }

    @Override
    protected Dialog onCreateDialog(final int id)
    {
        final AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(R.string.group_chooser_popup_title);

        switch(id)
        {
            case POPUP_SUCCESS:
                alertDialogBuilder.setMessage(R.string.group_chooser_success);
                break;
            case POPUP_NO_KEY:
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.group_chooser_request_error_pref),
                        getString(R.string.group_chooser_key_error_msg)));
                break;
            case POPUP_NO_ALGORITHM:
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.group_chooser_request_error_pref),
                        getString(
                                R.string.group_chooser_algorithm_error_msg)));
                break;
            case POPUP_REQUEST_ERROR:
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.group_chooser_request_error_pref),
                        errorMessage));
                break;
            case POPUP_NO_TOKEN:
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.group_chooser_request_error_pref),
                        getString(R.string.group_chooser_token_error_msg)));
                break;
            case POPUP_SAVE_ERROR:
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.group_chooser_save_error_pref),
                        errorMessage));
                break;
            case POPUP_FAIL:
                alertDialogBuilder.setMessage(R.string.group_chooser_fail);
        }
        alertDialogBuilder.setNeutralButton(R.string.ok,
                new OnOkClickListener());
        alertDialogBuilder.setCancelable(false);

        return alertDialogBuilder.create();
    }

    /**
     * Is called, when the cancel-button is pressed, and finishs this activity.
     *
     * @param view - The view
     */
    public void cancel(final View view)
    {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    /**
     * Is called, when the save-button is pressed. The method adds the user to
     * the checked groups.
     *
     * @param view - The view
     */
    public void save(final View view)
    {
        String[] allGroupIds = null;
        String[] checkedGroupIds = null;
        ServiceMethodProvider service = null;
        int i = 0, j = 0;

        if (groupHandler != null)
        {
            if (user != null)
            {
                //Create array with all checked group-IDs
                checkedGroupIds =
                        new String[groupHandler.getNumberOfCheckedGroups()];
                allGroupIds = groupHandler.getGroupIds();
                while (i < checkedGroupIds.length && j < allGroupIds.length)
                {
                    if (groupHandler.isChecked(j))
                    {
                        checkedGroupIds[i] = allGroupIds[j];
                        i++;
                    }
                    j++;
                }
                service = new ServiceMethodProvider();
                try
                {
                    if (service.addUserToGroups(user.getUserId(),
                            checkedGroupIds))
                    {
                        showDialog(POPUP_SUCCESS);
                    }
                    else
                    {
                        showDialog(POPUP_FAIL);
                    }
                    setResult(Activity.RESULT_OK);
                }
                catch (final Exception e)
                {
                    if (e.getMessage() != null)
                    {
                        errorMessage = e.getMessage();
                    }
                    else
                    {
                        errorMessage = "";
                    }
                    setResult(Activity.RESULT_CANCELED);
                    showDialog(POPUP_SAVE_ERROR);
                    LogHelper.logE(errorMessage);
                }
            }
            else
            {
                LogHelper.logE(CLAZZ, "Variable \"user\" war NULL!");
                errorMessage = "";
                setResult(Activity.RESULT_CANCELED);
                showDialog(POPUP_SAVE_ERROR);
            }
        }
        else
        {
            LogHelper.logE(CLAZZ, "Variable \"groupHandler\" war NULL!");
            errorMessage = "";
            setResult(Activity.RESULT_CANCELED);
            showDialog(POPUP_SAVE_ERROR);
        }
    }

    private class OnOkClickListener implements DialogInterface.OnClickListener
    {
        @Override
        public void onClick(final DialogInterface dialog, final int which)
        {
            GroupChooserActivity.this.finish();
        }
    }
}

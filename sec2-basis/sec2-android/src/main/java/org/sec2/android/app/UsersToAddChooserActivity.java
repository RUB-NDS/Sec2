package org.sec2.android.app;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.sec2.android.app.component.CheckedUserHandler;
import org.sec2.android.util.Constants;
import org.sec2.android.util.Sec2MiddlewarePreferenceManager;
import org.sec2.middleware.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import de.adesso.mobile.android.sec2.mwadapter.logging.LogHelper;
import de.adesso.mobile.android.sec2.mwadapter.model.Group;
import de.adesso.mobile.android.sec2.mwadapter.model.User;
import de.adesso.mobile.android.sec2.mwadapter.tasks.GetAllUsersTask;
import de.adesso.mobile.android.sec2.mwadapter.util.EmailToUserNameMapper;

/**
 * The activity shows a list with users who can be selected through checkboxes.
 * When clicking the save-button, the selected users will be added to the
 * group, which was passed together with the calling intent.
 *
 * @author schuessler
 */
public class UsersToAddChooserActivity extends AbstractUsersChooserActivity
{
    private Group group = null;
    private String errorMessage = null;

    private static final Class<?> CLAZZ = UsersToAddChooserActivity.class;
    private static final int POPUP_NO_KEY = 0;
    private static final int POPUP_NO_ALGORITHM = 1;
    private static final int POPUP_NO_TOKEN = 2;
    private static final int POPUP_REQUEST_ERROR = 3;
    private static final int POPUP_SAVE_ERROR = 4;
    private static final int POPUP_SUCCESS = 5;
    private static final int POPUP_FAIL = 6;

    @Override
    protected void initTextView()
    {
        final TextView intro = getTextView();

        group = getIntent().getParcelableExtra(Constants.INTENT_EXTRA_GROUP);
        if (group != null)
        {
            intro.setText(MessageFormat.format(getString(
                    R.string.uta_chooser_intro), group.getGroupName()));
        }
        else
        {
            intro.setText(R.string.uta_chooser_intro_no_group);
            intro.setTextColor(Color.RED);
            LogHelper.logD(CLAZZ, "Extra \"" + Constants.INTENT_EXTRA_GROUP
                    + "\" war NULL");
        }
    }

    @Override
    protected CheckedUserHandler initListView()
    {
        Sec2MiddlewarePreferenceManager prefManager = null;
        ArrayAdapter<String> arrayAdapter = null;
        User[] allUsers = null;
        String[] memberIds = null;
        String key = null;
        String algorithm = null;
        String token = null;
        ArrayList<User> usersToDisplay = null;
        final ListView list = getListView();
        CheckedUserHandler userHandler = null;
        GetAllUsersTask gauTask = null;
        int port;
        int j = 0;
        boolean add = true;

        if (group != null)
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
                            usersToDisplay = new ArrayList<User>();
                            port = prefManager.getServerListenPort();
                            gauTask = new GetAllUsersTask(key, algorithm,
                                    token, port);
                            gauTask.execute();
                            allUsers = gauTask.get();
                            if (gauTask.getException() != null)
                            {
                                throw gauTask.getException();
                            }
                            //We don't matter NPE here, because it is catched.
                            if (allUsers.length > 0)
                            {
                                //Add only those users to the list, who aren't
                                //already a member of the group. At the same
                                //time, try to determine their names from
                                //Android Contacs
                                memberIds = getIntent().getStringArrayExtra(
                                        Constants.INTENT_EXTRA_USER_IDS);
                                for (int i = 0; i < allUsers.length; i++)
                                {
                                    add = true;
                                    if (memberIds != null)
                                    {
                                        j = 0;
                                        while (j < memberIds.length && add)
                                        {
                                            if (!allUsers[i].getUserId()
                                                    .equals(memberIds[j]))
                                            {
                                                j++;
                                            }
                                            else
                                            {
                                                add = false;
                                            }
                                        }
                                    }
                                    if (add)
                                    {
                                        EmailToUserNameMapper.emailToUserName(
                                                this, allUsers[i]);
                                        if (allUsers[i].getUserName() == null)
                                        {
                                            allUsers[i].setUserName(getString(R.string.uta_chooser_username_not_found));
                                        }
                                        usersToDisplay.add(allUsers[i]);
                                    }
                                }
                            }
                            userHandler = new CheckedUserHandler(
                                    usersToDisplay.toArray(
                                            new User[usersToDisplay.size()]));
                            arrayAdapter = new ArrayAdapter<String>(this,
                                    R.layout.check_list_item,
                                    userHandler.getUserNames());
                            list.setAdapter(arrayAdapter);
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

        return userHandler;
    }

    @Override
    protected Dialog onCreateDialog(final int id)
    {
        final AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(R.string.uta_chooser_popup_title);

        switch(id)
        {
            case POPUP_SUCCESS:
                alertDialogBuilder.setMessage(R.string.uta_chooser_success);
                break;
            case POPUP_NO_KEY:
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.uta_chooser_request_error_pref),
                        getString(R.string.uta_chooser_key_error_msg)));
                break;
            case POPUP_NO_ALGORITHM:
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.uta_chooser_request_error_pref),
                        getString(R.string.uta_chooser_algorithm_error_msg)));
                break;
            case POPUP_REQUEST_ERROR:
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.uta_chooser_request_error_pref),
                        errorMessage));
                break;
            case POPUP_NO_TOKEN:
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.uta_chooser_request_error_pref),
                        getString(R.string.uta_chooser_token_error_msg)));
                break;
            case POPUP_SAVE_ERROR:
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.uta_chooser_save_error_pref), errorMessage));
                break;
            case POPUP_FAIL:
                alertDialogBuilder.setMessage(R.string.uta_chooser_fail);
        }
        alertDialogBuilder.setNeutralButton(R.string.ok,
                new OnOkClickListener());
        alertDialogBuilder.setCancelable(false);

        return alertDialogBuilder.create();
    }

    /**
     * Is called, when the save-button is pressed. The method adds the checked
     * user to the group.
     *
     * @param view - The view
     */
    @Override
    public void save(final View view)
    {
        String[] allUserIds = null;
        String[] checkedUserIds = null;
        ServiceMethodProvider service = null;
        final CheckedUserHandler userHandler = getUserHandler();
        int i = 0, j = 0;

        if (userHandler != null)
        {
            if (group != null)
            {
                //Create array with all checked user-IDs
                checkedUserIds =
                        new String[userHandler.getNumberOfCheckedUsers()];
                allUserIds = userHandler.getUserIds();
                while (i < checkedUserIds.length && j < allUserIds.length)
                {
                    if (userHandler.isChecked(j))
                    {
                        checkedUserIds[i] = allUserIds[j];
                        i++;
                    }
                    j++;
                }
                service = new ServiceMethodProvider();
                try
                {
                    if (service.addUsersToGroup(checkedUserIds,
                            group.getGroupId()))
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
                LogHelper.logE(CLAZZ, "Variable \"group\" war NULL!");
                errorMessage = "";
                setResult(Activity.RESULT_CANCELED);
                showDialog(POPUP_SAVE_ERROR);
            }
        }
        else
        {
            LogHelper.logE(CLAZZ, "Variable \"userHandler\" war NULL!");
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
            UsersToAddChooserActivity.this.finish();
        }
    }
}

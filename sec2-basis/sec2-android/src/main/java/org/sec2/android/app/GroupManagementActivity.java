package org.sec2.android.app;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.sec2.android.util.Constants;
import org.sec2.android.util.Sec2MiddlewarePreferenceManager;
import org.sec2.middleware.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import de.adesso.mobile.android.sec2.mwadapter.gui.UsersInGroupInfoActivity;
import de.adesso.mobile.android.sec2.mwadapter.logging.LogHelper;
import de.adesso.mobile.android.sec2.mwadapter.model.Group;
import de.adesso.mobile.android.sec2.mwadapter.model.User;
import de.adesso.mobile.android.sec2.mwadapter.tasks.GetRegisteredUserTask;

/**
 * This activity represents the group-management of the administration app of
 * the Sec2-middleware. The activity displays informations about the specific
 * group, which was passed together with the intent, and a list of all users,
 * who are a member. The activity extends the UsersInGroupInfoActivity from the
 * MwAdapter-library.
 *
 * @author nike
 */
public class GroupManagementActivity extends UsersInGroupInfoActivity
{
    private static final String SAVED_GROUP = "group";
    private static final String SAVED_REG_USER = "user";
    private static final int POPUP_GROUP_MANAGEMENT_ADD_ERROR = 100;
    private static final int POPUP_GROUP_MANAGEMENT_REMOVE_ERROR_GROUP = 101;
    private static final int POPUP_GROUP_MANAGEMENT_REMOVE_ERROR_USER = 102;
    private static final int POPUP_GROUP_MANAGEMENT_REMOVE_ERROR_OWNER = 103;
    private static final int POPUP_GROUP_MANAGEMENT_REMOVE_ERROR_GENERAL = 104;
    private static final int POPUP_GROUP_MANAGEMENT_REMOVE_CONFIRM = 105;
    private static final int POPUP_GROUP_MANAGEMENT_REMOVE_SUCCESS = 106;
    private static final int POPUP_GROUP_MANAGEMENT_ERROR = 107;
    private static final int INTENT_FOR_RESULT = 3000;
    private static final Class<?> CLAZZ = UserManagementActivity.class;

    private Group group = null;
    private User userToDelete = null;
    private User registeredUser = null;
    private String errorMessage = "";
    private OnItemLongClickListener usersListLongListener = null;
    private OnItemClickListener usersListListener = null;
    private boolean buttonsInflated = false;

    @Override
    protected void doOnCreateMisc(final Bundle savedInstanceState)
    {
        if (savedInstanceState == null)
        {
            group = getIntent().getParcelableExtra(
                    Constants.INTENT_EXTRA_GROUP);
        }
        else
        {
            group = savedInstanceState.getParcelable(SAVED_GROUP);
            registeredUser = savedInstanceState.getParcelable(SAVED_REG_USER);
        }

        //Initialise users-list
        refreshActivity();
    }

    /**
     * Save the group- and registered-user-object, if this activity is
     * unexpectedly killed.
     *
     * @param outState - The Bundle where to save the group- and the
     *  registered-user-object.
     */
    @Override
    protected void onSaveInstanceState(final Bundle outState)
    {
        if (group != null)
        {
            outState.putParcelable(SAVED_GROUP, group);
        }
        if (registeredUser != null)
        {
            outState.putParcelable(SAVED_REG_USER, registeredUser);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected Dialog onCreateDialog(final int id)
    {
        switch(id)
        {
            case POPUP_GROUP_MANAGEMENT_ADD_ERROR:
            case POPUP_GROUP_MANAGEMENT_REMOVE_ERROR_GROUP:
            case POPUP_GROUP_MANAGEMENT_REMOVE_ERROR_USER:
            case POPUP_GROUP_MANAGEMENT_REMOVE_ERROR_OWNER:
            case POPUP_GROUP_MANAGEMENT_REMOVE_ERROR_GENERAL:
            case POPUP_GROUP_MANAGEMENT_REMOVE_SUCCESS:
            case POPUP_GROUP_MANAGEMENT_ERROR:
                return createInfoDialog(id);
            case POPUP_GROUP_MANAGEMENT_REMOVE_CONFIRM:
                return createConfirmDialog(id);
            default:
                return super.onCreateDialog(id);
        }
    }

    /**
     * Reload group's user-list if a user was successfully added to the group
     * or removed from the group.
     *
     * @param requestCode - The request-code
     * @param resultCode - The result-code
     * @param data - The data of the intent
     */
    @Override
    protected void onActivityResult(final int requestCode,
            final int resultCode, final Intent data)
    {
        if (group != null && resultCode == Activity.RESULT_OK)
        {
            refreshActivity();
        }
    }

    /**
     * Calls to UsersToAddChooserActivity, so the users, who should be added to
     * the group, can be chosen.
     *
     * @param view - The view
     */
    public void add(final View view)
    {
        Intent intent = null;

        if (group != null)
        {
            intent = new Intent(this, UsersToAddChooserActivity.class);
            intent.putExtra(Constants.INTENT_EXTRA_GROUP, group);
            intent.putExtra(Constants.INTENT_EXTRA_USER_IDS,
                    getUserIdsInList());
            startActivityForResult(intent, INTENT_FOR_RESULT);
        }
        else
        {
            showDialog(POPUP_GROUP_MANAGEMENT_ADD_ERROR);
            LogHelper.logE(CLAZZ, "Kein Group-Objekt im Intent gefunden!");
        }
    }

    /**
     * Calls to UsersToRemoveChooserActivity, so the users, who should be
     * removed from the group, can be chosen.
     *
     * @param view - The view
     */
    public void remove(final View view)
    {
        ArrayList<User> users = null;
        User user = null;
        Intent intent = null;
        int count = -1;

        if (group != null)
        {
            count = getCount();
            //If getCount > 0 and users.size > 0, show the chooser-activity,
            //otherwise do simply nothing.
            if (count > 0)
            {
                users = new ArrayList<User>();
                for (int i = 0; i < count; i++)
                {
                    user = getUserByListPosition(i);
                    //Only show user in the chooser-activity, if user != null
                    //and is not the group-owner
                    if (user != null && (!group.isGroupOwnerSet()
                            || group.isGroupOwnerSet() && !user.getUserId()
                            .equals(group.getGroupOwner().getUserId())))
                    {
                        users.add(user);
                    }
                }
                if (users.size() > 0)
                {
                    intent = new Intent(this,
                            UsersToRemoveChooserActivity.class);
                    intent.putExtra(Constants.INTENT_EXTRA_GROUP, group);
                    intent.putParcelableArrayListExtra(
                            Constants.INTENT_EXTRA_USERS, users);
                    startActivityForResult(intent, INTENT_FOR_RESULT);
                }
            }
        }
        else
        {
            showDialog(POPUP_GROUP_MANAGEMENT_REMOVE_ERROR_GROUP);
            LogHelper.logE(CLAZZ, "Kein Group-Objekt im Intent gefunden!");
        }
    }

    private Dialog createInfoDialog(final int id)
    {
        final AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(this);

        if (id == POPUP_GROUP_MANAGEMENT_ADD_ERROR)
        {
            alertDialogBuilder.setTitle(R.string.group_popup_add_title);
        }
        else if (id == POPUP_GROUP_MANAGEMENT_ERROR)
        {
            alertDialogBuilder.setTitle(R.string.group_popup_error_title);
        }
        else
        {
            alertDialogBuilder.setTitle(R.string.group_popup_remove_title);
        }
        switch(id)
        {
            case POPUP_GROUP_MANAGEMENT_ADD_ERROR:
            case POPUP_GROUP_MANAGEMENT_REMOVE_ERROR_GROUP:
                alertDialogBuilder.setMessage(
                        R.string.group_popup_group_error);
                alertDialogBuilder.setNeutralButton(R.string.ok, null);
                break;
            case POPUP_GROUP_MANAGEMENT_REMOVE_ERROR_USER:
                alertDialogBuilder.setMessage(R.string.group_popup_user_error);
                alertDialogBuilder.setNeutralButton(R.string.ok, null);
                break;
            case POPUP_GROUP_MANAGEMENT_REMOVE_ERROR_OWNER:
                alertDialogBuilder.setMessage(
                        R.string.group_popup_owner_error);
                alertDialogBuilder.setNeutralButton(R.string.ok, null);
                break;
            case POPUP_GROUP_MANAGEMENT_ERROR:
                alertDialogBuilder.setMessage(errorMessage);
                alertDialogBuilder.setNeutralButton(R.string.ok, null);
                break;
            case POPUP_GROUP_MANAGEMENT_REMOVE_ERROR_GENERAL:
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.group_popup_general_error), errorMessage));
                alertDialogBuilder.setNeutralButton(R.string.ok,
                        new RemoveDialogClickListener(id));
                break;
            case POPUP_GROUP_MANAGEMENT_REMOVE_SUCCESS:
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.group_popup_success),
                        userToDelete.getUserName(),
                        group.getGroupName()));
                alertDialogBuilder.setNeutralButton(R.string.ok,
                        new RemoveDialogClickListener(id));
                break;
        }
        alertDialogBuilder.setCancelable(false);

        return alertDialogBuilder.create();
    }

    private Dialog createConfirmDialog(final int id)
    {
        final AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(this);

        if (userToDelete == null)
        {
            LogHelper.logD(CLAZZ, "Variable \"userToDelete\" war NULL!");
            return createInfoDialog(POPUP_GROUP_MANAGEMENT_REMOVE_ERROR_USER);
        }
        if (group == null)
        {
            LogHelper.logD(CLAZZ, "Variable \"group\" war NULL!");
            return createInfoDialog(POPUP_GROUP_MANAGEMENT_REMOVE_ERROR_GROUP);
        }

        alertDialogBuilder.setTitle(R.string.group_popup_confirm_title);
        alertDialogBuilder.setMessage(MessageFormat.format(getString(
                R.string.group_popup_confirm_remove),
                userToDelete.getUserName(),
                group.getGroupName()));
        alertDialogBuilder.setPositiveButton(R.string.yes,
                new OnYesRemoveClickListener());
        alertDialogBuilder.setNegativeButton(R.string.no,
                new RemoveDialogClickListener(id));

        return alertDialogBuilder.create();
    }

    private void refreshActivity()
    {
        final Sec2MiddlewarePreferenceManager prefManager =
                new Sec2MiddlewarePreferenceManager(getApplicationContext());
        final ListView usersList = getUsersListView();
        final String sessionKey = prefManager.getSessionKey();
        final String algorithm = prefManager.getSessionKeyAlgorithm();
        final String sessionToken = prefManager.getSessionToken();
        final int port = prefManager.getServerListenPort();
        View buttons = null;
        GetRegisteredUserTask gruTask = null;
        boolean enableUserRemoval = false;

        //Get group information. Case group == NULL is handled in method
        //requestGroupInfos()
        group = requestGroupInfos(group, sessionKey, algorithm, sessionToken,
                port);

        //Add listeners to the users' ListView-object
        if (usersListListener == null)
        {
            usersListListener = new UsersListOnItemClickListener();
        }
        usersList.setOnItemClickListener(usersListListener);

        if (group.isGroupOwnerSet())
        {
            //If registeredUser == null, fetch him from the Sec2-middleware
            if (registeredUser == null)
            {
                try
                {
                    gruTask = new GetRegisteredUserTask(sessionKey, algorithm,
                            sessionToken, port);
                    gruTask.execute();
                    registeredUser = gruTask.get();
                    if(gruTask.getException() != null)
                    {
                        throw gruTask.getException();
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
                    showDialog(POPUP_GROUP_MANAGEMENT_ERROR);
                    LogHelper.logE(CLAZZ, "Es ist ein Fehler aufgetreten: "
                            + errorMessage);
                }
            }
            //If registered is group-owner, enable possibility to remove users
            //from group
            if (group.getGroupOwner().getUserId().equals(
                    registeredUser.getUserId()))
            {
                enableUserRemoval = true;
            }
        }

        //If enableUserRemoval, add buttons, if not added yet, and
        //long-click-listener to the users-list
        if (enableUserRemoval)
        {
            //Add long-click-listener to users-list
            if (usersListLongListener == null)
            {
                usersListLongListener = new UsersListOnItemLongClickListener();
            }
            usersList.setOnItemLongClickListener(usersListLongListener);
            //Add buttons, if not added yet
            if (!buttonsInflated)
            {
                getLayoutInflater().inflate(R.layout.add_remove_btn,
                        getMembersTableRow());
                buttonsInflated = true;
            }
        }
        //Else remove buttons, if already inflated
        else if (buttonsInflated)
        {
            buttons = findViewById(R.id.add_remove_btn);
            getMembersTableRow().removeView(buttons);
        }
    }

    private final class UsersListOnItemLongClickListener
    implements OnItemLongClickListener
    {
        @Override
        public boolean onItemLongClick(final AdapterView<?> parent,
                final View view, final int position, final long id)
        {
            userToDelete = getUserByListPosition(position);

            if (group != null)
            {
                if (userToDelete != null)
                {
                    if (group.isGroupOwnerSet() && group.getGroupOwner()
                            .getUserId().equals(userToDelete.getUserId()))
                    {
                        showDialog(POPUP_GROUP_MANAGEMENT_REMOVE_ERROR_OWNER);
                    }
                    else
                    {
                        showDialog(POPUP_GROUP_MANAGEMENT_REMOVE_CONFIRM);
                    }
                }
                else
                {
                    showDialog(POPUP_GROUP_MANAGEMENT_REMOVE_ERROR_USER);
                    LogHelper.logD(CLAZZ, MessageFormat.format(
                            "Es konnte keine Benutzer-ID für Position {0} "
                                    + "gefunden werden", position));
                }
            }
            else
            {
                showDialog(POPUP_GROUP_MANAGEMENT_REMOVE_ERROR_GROUP);
                LogHelper.logD(CLAZZ, "Variable \"group\" war NULL!");
            }
            return true;
        }
    }

    private final class OnYesRemoveClickListener
    implements DialogInterface.OnClickListener
    {
        /*
         * Handles the onClick-event on the YES-button. Beside others it
         * removes the dialog POPUP_GROUP_MANAGEMENT_REMOVE_CONFIRM, so that it
         * updates the user and the group the next time this dialog is
         * displayed.
         */
        @Override
        public void onClick(final DialogInterface dialog, final int which)
        {
            ServiceMethodProvider service = null;

            if (group != null)
            {
                if (userToDelete != null)
                {
                    service = new ServiceMethodProvider();
                    try
                    {
                        if (service.removeUsersFromGroup(new String[]{
                                userToDelete.getUserId()}, group.getGroupId()))
                        {
                            showDialog(POPUP_GROUP_MANAGEMENT_REMOVE_SUCCESS);
                        }
                        else
                        {
                            errorMessage = MessageFormat.format(getString(
                                    R.string.group_popup_general_error_info),
                                    userToDelete.getUserName(),
                                    group.getGroupName());
                            showDialog(POPUP_GROUP_MANAGEMENT_REMOVE_ERROR_GENERAL);
                        }
                        //Reload group-infos
                        refreshActivity();
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
                        showDialog(
                                POPUP_GROUP_MANAGEMENT_REMOVE_ERROR_GENERAL);
                        LogHelper.logE(CLAZZ, "Es ist ein Fehler aufgetreten: "
                                + errorMessage);
                    }
                    finally
                    {
                        removeDialog(POPUP_GROUP_MANAGEMENT_REMOVE_CONFIRM);
                    }
                }
                else
                {
                    LogHelper.logE(CLAZZ,
                            "Variable \"userToDelete\" ist NULL!");
                    removeDialog(POPUP_GROUP_MANAGEMENT_REMOVE_CONFIRM);
                    showDialog(POPUP_GROUP_MANAGEMENT_REMOVE_ERROR_USER);
                }
            }
            else
            {
                LogHelper.logE(CLAZZ, "Variable \"group\" ist NULL!");
                removeDialog(POPUP_GROUP_MANAGEMENT_REMOVE_CONFIRM);
                showDialog(POPUP_GROUP_MANAGEMENT_REMOVE_ERROR_GROUP);
            }
        }
    }

    private final class RemoveDialogClickListener
    implements DialogInterface.OnClickListener
    {
        private int dialogId = -1;

        private RemoveDialogClickListener(final int dialogId)
        {
            this.dialogId = dialogId;
        }

        /*
         * Handles the onClick-event on the button. It removes the dialog with
         * the dialogID, so that it updates the variable text the next time
         * this dialog is displayed.
         */
        @Override
        public void onClick(final DialogInterface dialog, final int which)
        {
            removeDialog(dialogId);
        }
    }

    private final class UsersListOnItemClickListener
    implements OnItemClickListener
    {
        @Override
        public void onItemClick(final AdapterView<?> parent, final View view,
                final int position, final long id)
        {
            Intent intent = null;
            final User user = getUserByListPosition(position);

            if (user != null)
            {
                intent = new Intent(GroupManagementActivity.this,
                        UserManagementActivity.class);
                intent.putExtra(Constants.INTENT_EXTRA_USER, user);
                startActivity(intent);
            }
            else
            {
                errorMessage = getString(R.string.group_popup_user_error);
                showDialog(POPUP_GROUP_MANAGEMENT_ERROR);
                LogHelper.logD(MessageFormat.format("Es konnte keine "
                        + "Benutzer-ID für Position {0} gefunden werden",
                        position));
            }
        }
    }
}

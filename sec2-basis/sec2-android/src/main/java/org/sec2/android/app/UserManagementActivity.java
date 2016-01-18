package org.sec2.android.app;

import java.text.MessageFormat;

import org.sec2.android.util.Constants;
import org.sec2.android.util.Sec2MiddlewarePreferenceManager;
import org.sec2.middleware.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.adesso.mobile.android.sec2.mwadapter.gui.UserInfoActivity;
import de.adesso.mobile.android.sec2.mwadapter.logging.LogHelper;
import de.adesso.mobile.android.sec2.mwadapter.model.Group;
import de.adesso.mobile.android.sec2.mwadapter.model.User;

/**
 * This activity represents the user-management of the administration app of
 * the Sec2-middleware. The activity displays informations about the specific
 * user, who was passed together with the intent, and a list of all groups,
 * where he is a member. The activity extends the UserInfoActivity from the
 * MwAdapter-library.
 *
 * @author nike
 */
public class UserManagementActivity extends UserInfoActivity
{
    private String errorMessage = "";
    private User user = null;
    private Group group = null;
    private OnItemClickListener groupsListListener = null;
    private int groupsListId = -1;

    private static final int POPUP_USER_MANAGEMENT_ADD_ERROR = 100;
    private static final int POPUP_USER_MANAGEMENT_REMOVE_ERROR_USER = 101;
    private static final int POPUP_USER_MANAGEMENT_REMOVE_ERROR_GROUP = 102;
    private static final int POPUP_USER_MANAGEMENT_REMOVE_ERROR_GENERAL = 103;
    private static final int POPUP_USER_MANAGEMENT_REMOVE = 104;
    private static final int POPUP_USER_MANAGEMENT_REMOVE_SUCCESS = 105;
    private static final int POPUP_USER_MANAGEMENT_REMOVE_FAIL = 106;
    private static final int POPUP_USER_MANAGEMENT_REMOVE_ALL = 107;
    private static final int POPUP_USER_MANAGEMENT_REMOVE_ALL_SUCCESS = 108;
    private static final int POPUP_USER_MANAGEMENT_REMOVE_ALL_FAIL = 109;
    private static final int POPUP_USER_MANAGEMENT_ERROR = 110;
    private static final int INTENT_FOR_RESULT = 2000;
    private static final String SAVED_USER = "user";
    private static final Class<?> CLAZZ = UserManagementActivity.class;

    @Override
    protected void doOnCreateLayout(final Bundle savedInstanceState)
    {
        super.doOnCreateLayout(savedInstanceState);
        //Add a button to the table-row
        getLayoutInflater().inflate(R.layout.add_btn, getGroupTableRow());
    }

    @Override
    protected void doOnCreateMisc(final Bundle savedInstanceState)
    {
        final ListView groupsList = getGroupsListView();

        if (savedInstanceState == null)
        {
            user = getIntent().getParcelableExtra(Constants.INTENT_EXTRA_USER);
        }
        else
        {
            user = savedInstanceState.getParcelable(SAVED_USER);
        }

        groupsListListener = new GroupsListOnItemClickListener();
        groupsListId = groupsList.getId();

        refreshActivity(); //Initialise groups-list

        //Register groups-list for context-menu
        registerForContextMenu(groupsList);
    }

    @Override
    protected Dialog onCreateDialog(final int id)
    {
        switch(id)
        {
            case POPUP_USER_MANAGEMENT_ADD_ERROR:
            case POPUP_USER_MANAGEMENT_REMOVE_ERROR_USER:
            case POPUP_USER_MANAGEMENT_REMOVE_ERROR_GROUP:
            case POPUP_USER_MANAGEMENT_REMOVE_ERROR_GENERAL:
            case POPUP_USER_MANAGEMENT_REMOVE_SUCCESS:
            case POPUP_USER_MANAGEMENT_REMOVE_FAIL:
            case POPUP_USER_MANAGEMENT_REMOVE_ALL_SUCCESS:
            case POPUP_USER_MANAGEMENT_REMOVE_ALL_FAIL:
            case POPUP_USER_MANAGEMENT_ERROR:
                return createInfoDialog(id);
            case POPUP_USER_MANAGEMENT_REMOVE:
            case POPUP_USER_MANAGEMENT_REMOVE_ALL:
                return createConfirmDialog(id);
            default:
                return super.onCreateDialog(id);
        }
    }

    /**
     * Save the user-object, if this activity is unexpectedly killed.
     *
     * @param outState - The bundle where to save the user
     */
    @Override
    protected void onSaveInstanceState(final Bundle outState)
    {
        if (user != null)
        {
            outState.putParcelable(SAVED_USER, user);
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * Reload user's group-list if user was successfully added to groups.
     *
     * @param requestCode - The request-code
     * @param resultCode - The result-code
     * @param data - Containing the result's data.
     */
    @Override
    protected void onActivityResult(final int requestCode,
            final int resultCode, final Intent data)
    {
        //Refresh activity
        if (user != null && resultCode == Activity.RESULT_OK)
        {
            refreshActivity();
        }
    }

    private Dialog createConfirmDialog(final int id)
    {
        AlertDialog.Builder alertDialogBuilder = null;

        if (user == null)
        {
            LogHelper.logW(CLAZZ, "Variable \"user\" ist NULL!");
            return createInfoDialog(POPUP_USER_MANAGEMENT_REMOVE_ERROR_USER);
        }

        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.um_popup_confirm_title);
        switch(id)
        {
            case POPUP_USER_MANAGEMENT_REMOVE:
                if (group == null)
                {
                    LogHelper.logW(CLAZZ, "Variable \"group\" ist NULL!");
                    return createInfoDialog(
                            POPUP_USER_MANAGEMENT_REMOVE_ERROR_GROUP);
                }
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.um_popup_confirm_remove),
                        user.getUserName(), group.getGroupName()));
                alertDialogBuilder.setPositiveButton(R.string.yes,
                        new OnYesRemoveClickListener());
                break;
            case POPUP_USER_MANAGEMENT_REMOVE_ALL:
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.um_popup_confirm_remove_all),
                        user.getUserName()));
                alertDialogBuilder.setPositiveButton(R.string.yes,
                        new OnYesRemoveAllClickListener());
                break;
        }
        alertDialogBuilder.setNegativeButton(R.string.no, null);

        return alertDialogBuilder.create();
    }

    private Dialog createInfoDialog(final int id)
    {
        final AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(this);

        switch(id)
        {
            case POPUP_USER_MANAGEMENT_ADD_ERROR:
                alertDialogBuilder.setTitle(R.string.um_popup_add_error_title);
                alertDialogBuilder.setMessage(R.string.um_popup_user_error);
                alertDialogBuilder.setNeutralButton(R.string.ok, null);
                break;
            case POPUP_USER_MANAGEMENT_REMOVE_ERROR_USER:
                alertDialogBuilder.setTitle(
                        R.string.um_popup_remove_error_title);
                alertDialogBuilder.setMessage(R.string.um_popup_user_error);
                alertDialogBuilder.setNeutralButton(R.string.ok, null);
                break;
            case POPUP_USER_MANAGEMENT_REMOVE_ERROR_GROUP:
                alertDialogBuilder.setTitle(
                        R.string.um_popup_remove_error_title);
                alertDialogBuilder.setMessage(R.string.um_popup_group_error);
                alertDialogBuilder.setNeutralButton(R.string.ok, null);
                break;
            case POPUP_USER_MANAGEMENT_REMOVE_ERROR_GENERAL:
                alertDialogBuilder.setTitle(
                        R.string.um_popup_remove_error_title);
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.um_popup_remove_error_general),
                        errorMessage));
                alertDialogBuilder.setNeutralButton(R.string.ok,
                        new OnOkClickListener(id));
                break;
            case POPUP_USER_MANAGEMENT_REMOVE_SUCCESS:
                alertDialogBuilder.setTitle(
                        R.string.um_popup_remove_success_title);
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.um_popup_remove_success), user.getUserName(),
                        group.getGroupName()));
                alertDialogBuilder.setNeutralButton(R.string.ok,
                        new OnOkClickListener(id));
                break;
            case POPUP_USER_MANAGEMENT_REMOVE_ALL_SUCCESS:
                alertDialogBuilder.setTitle(
                        R.string.um_popup_remove_success_title);
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.um_popup_remove_all_success),
                        user.getUserName()));
                alertDialogBuilder.setNeutralButton(R.string.ok,
                        new OnOkClickListener(id));
                break;
            case POPUP_USER_MANAGEMENT_ERROR:
                alertDialogBuilder.setTitle(R.string.um_popup_error_title);
                alertDialogBuilder.setMessage(R.string.um_popup_userid_error);
                alertDialogBuilder.setNeutralButton(R.string.ok, null);
                break;
            case POPUP_USER_MANAGEMENT_REMOVE_FAIL:
                alertDialogBuilder.setTitle(
                        R.string.um_popup_remove_error_title);
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.um_popup_remove_fail), user.getUserName(),
                        group.getGroupName()));
                alertDialogBuilder.setNeutralButton(R.string.ok,
                        new OnOkClickListener(id));
                break;
            case POPUP_USER_MANAGEMENT_REMOVE_ALL_FAIL:
                alertDialogBuilder.setTitle(
                        R.string.um_popup_remove_error_title);
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.um_popup_remove_all_fail),
                        user.getUserName()));
                alertDialogBuilder.setNeutralButton(R.string.ok,
                        new OnOkClickListener(id));
        }

        alertDialogBuilder.setCancelable(false);

        return alertDialogBuilder.create();
    }

    private void refreshActivity()
    {
        final Sec2MiddlewarePreferenceManager prefManager =
                new Sec2MiddlewarePreferenceManager(getApplicationContext());
        final ListView groupsList = getGroupsListView();

        //Case user == NULL is handled in method requestUserInfos()
        requestUserInfos(user, prefManager.getSessionKey(),
                prefManager.getSessionKeyAlgorithm(),
                prefManager.getSessionToken(),
                prefManager.getServerListenPort());
        //Set listener again, because it is changed in method requestUserInfos
        if (groupsListListener == null)
        {
            groupsListListener = new GroupsListOnItemClickListener();
        }
        groupsList.setOnItemClickListener(groupsListListener);
    }

    /**
     * Calls to GroupChooserActivity, so the groups can be chosen, where the
     * user should be added to.
     *
     * @param view - The view
     */
    public void add(final View view)
    {
        Intent intent = null;

        if (user != null)
        {
            intent = new Intent(this, GroupChooserActivity.class);
            intent.putExtra(Constants.INTENT_EXTRA_USER, user);
            startActivityForResult(intent, INTENT_FOR_RESULT);
        }
        else
        {
            showDialog(POPUP_USER_MANAGEMENT_ADD_ERROR);
            LogHelper.logE(CLAZZ, "Kein User-Objekt im Intent gefunden!");
        }
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenuInfo menuInfo)
    {
        if (groupsListId == v.getId())
        {
            getMenuInflater().inflate(R.menu.usermanagement_context, menu);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item)
    {
        AdapterContextMenuInfo menuInfo = null;

        if (!(item.getMenuInfo() instanceof AdapterContextMenuInfo))
        {
            LogHelper.logW(CLAZZ, "MenuInfo hatte Klasse "
                    + item.getMenuInfo().getClass().getName() + " statt "
                    + AdapterContextMenuInfo.class.getName());
            return super.onContextItemSelected(item);
        }

        menuInfo = (AdapterContextMenuInfo)(item.getMenuInfo());
        switch(item.getItemId())
        {
            case R.id.um_context_remove:
                group = getGroupByListPosition(menuInfo.position);
                showDialog(POPUP_USER_MANAGEMENT_REMOVE);
                return true;
            case R.id.um_context_remove_all:
                showDialog(POPUP_USER_MANAGEMENT_REMOVE_ALL);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private final class OnYesRemoveClickListener
    implements DialogInterface.OnClickListener
    {
        /*
         * Handles the onClick-event on the YES-button, if the user should only
         * be removed from ONE group. It removes beside others the dialog
         * POPUP_USER_MANAGEMENT_REMOVE, so that it updates the user and the
         * group the next time this dialog is displayed.
         */
        @Override
        public void onClick(final DialogInterface dialog, final int which)
        {
            ServiceMethodProvider service = null;

            if (group != null)
            {
                if (user != null)
                {
                    service = new ServiceMethodProvider();
                    try
                    {
                        if (service.removeUserFromGroups(user.getUserId(),
                                new String[]{group.getGroupId()}))
                        {
                            showDialog(POPUP_USER_MANAGEMENT_REMOVE_SUCCESS);
                        }
                        else
                        {
                            showDialog(POPUP_USER_MANAGEMENT_REMOVE_FAIL);
                        }
                        //Reload user's group-list
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
                        showDialog(POPUP_USER_MANAGEMENT_REMOVE_ERROR_GENERAL);
                        LogHelper.logE(CLAZZ, "Es ist ein Fehler aufgetreten: "
                                + errorMessage);
                    }
                    finally
                    {
                        removeDialog(POPUP_USER_MANAGEMENT_REMOVE);
                    }
                }
                else
                {
                    LogHelper.logE(CLAZZ, "Variable \"user\" ist NULL!");
                    removeDialog(POPUP_USER_MANAGEMENT_REMOVE);
                    showDialog(POPUP_USER_MANAGEMENT_REMOVE_ERROR_USER);
                }
            }
            else
            {
                LogHelper.logE(CLAZZ, "Variable \"group\" ist NULL!");
                removeDialog(POPUP_USER_MANAGEMENT_REMOVE);
                showDialog(POPUP_USER_MANAGEMENT_REMOVE_ERROR_GROUP);
            }
        }
    }

    private final class OnYesRemoveAllClickListener
    implements DialogInterface.OnClickListener
    {
        /*
         * Handles the onClick-event on the YES-button, if the user should be
         * removed from ALL groups.
         */
        @Override
        public void onClick(final DialogInterface dialog, final int which)
        {
            ServiceMethodProvider service = null;

            if (user != null)
            {
                service = new ServiceMethodProvider();
                try
                {
                    if (service.removeUserFromGroups(user.getUserId(),
                            getGroupIdsInList()))
                    {
                        showDialog(POPUP_USER_MANAGEMENT_REMOVE_ALL_SUCCESS);
                    }
                    else
                    {
                        showDialog(POPUP_USER_MANAGEMENT_REMOVE_ALL_FAIL);
                    }
                    //Reload user's group-list.
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
                    showDialog(POPUP_USER_MANAGEMENT_REMOVE_ERROR_GENERAL);
                    LogHelper.logE(CLAZZ, "Es ist ein Fehler aufgetreten: "
                            + errorMessage);
                }
            }
            else
            {
                LogHelper.logE(CLAZZ, "Variable \"user\" ist NULL!");
                showDialog(POPUP_USER_MANAGEMENT_REMOVE_ERROR_USER);
            }
        }
    }

    private final class OnOkClickListener
    implements DialogInterface.OnClickListener
    {
        private final int dialogId;

        private OnOkClickListener(final int dialogId)
        {
            this.dialogId = dialogId;
        }

        /*
         * Handles the onClick-event on the OK-button. It removes the dialog
         * POPUP_USER_MANAGEMENT_REMOVE_SUCCESS, so that it updates the user
         * and the group the next time this dialog is displayed.
         */
        @Override
        public void onClick(final DialogInterface dialog, final int which)
        {
            removeDialog(dialogId);
        }
    }

    private final class GroupsListOnItemClickListener
    implements OnItemClickListener
    {
        @Override
        public void onItemClick(final AdapterView<?> parent, final View view,
                final int position, final long id)
        {
            Intent intent = null;
            final Group group = getGroupByListPosition(position);

            if (group != null)
            {
                intent = new Intent(UserManagementActivity.this,
                        GroupManagementActivity.class);
                intent.putExtra(Constants.INTENT_EXTRA_GROUP, group);
                startActivity(intent);
            }
            else
            {
                showDialog(POPUP_USER_MANAGEMENT_ERROR);
                LogHelper.logD(MessageFormat.format("Es konnte keine "
                        + "Benutzer-ID für Position {0} gefunden werden",
                        position));
            }
        }
    }
}

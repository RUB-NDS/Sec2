package org.sec2.android.app;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;

import org.sec2.android.app.component.CountedGroupArrayAdapter;
import org.sec2.android.model.CountedGroup;
import org.sec2.android.util.Constants;
import org.sec2.android.util.Sec2MiddlewarePreferenceManager;
import org.sec2.middleware.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.adesso.mobile.android.sec2.mwadapter.MwAdapter;
import de.adesso.mobile.android.sec2.mwadapter.logging.LogHelper;
import de.adesso.mobile.android.sec2.mwadapter.model.Group;
import de.adesso.mobile.android.sec2.mwadapter.model.User;
import de.adesso.mobile.android.sec2.mwadapter.tasks.GetGroupTask;
import de.adesso.mobile.android.sec2.mwadapter.tasks.GetUsersInGroupTask;
import de.adesso.mobile.android.sec2.mwadapter.tasks.MwAdapterTask;
import de.adesso.mobile.android.sec2.mwadapter.util.EmailToUserNameMapper;
import de.adesso.mobile.android.sec2.mwadapter.util.UserHandler;

/**
 * This activity is the entry activity to the group-management of the
 * administration app of the Sec2-middleware. The activity displays a list of
 * all groups, where user, who is registered at the Sec2-middleware, is the
 * group-owner.
 *
 * @author schuessler
 */
public class GroupManagementEntryActivity extends Activity
{
    private static final Class<?> CLAZZ = GroupManagementEntryActivity.class;
    private static final int POPUP_ERROR = 0;
    private static final int POPUP_NO_KEY = 1;
    private static final int POPUP_NO_ALGORITHM = 2;
    private static final int POPUP_NO_TOKEN = 3;
    private static final int POPUP_NO_GROUP = 4;
    private static final int POPUP_CONFIRM_REMOVE = 5;
    private static final int POPUP_REMOVE_SUCCESS = 6;
    private static final int POPUP_REMOVE_FAIL = 7;
    private static final int INTENT_FOR_RESULT = 1000;
    private static final String SAVED_LOGIN_PW = "loginPw";
    private static final String SAVED_TASK = "createNewGroup";

    private String errorMessage = "";
    private String loginPw = null;
    private CountedGroupArrayAdapter arrayAdapter = null;
    private User registeredUser = null;
    private int adapterPosition = -1;
    private int groupsListId = -1;
    private boolean createNewGroup = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.groups_overview);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.groups_overview_titlebar);

        if (savedInstanceState == null)
        {
            loginPw = getIntent().getStringExtra(
                    Constants.INTENT_EXTRA_LOGIN_PW);
        }
        else
        {
            loginPw = savedInstanceState.getString(SAVED_LOGIN_PW);
            createNewGroup = savedInstanceState.getBoolean(SAVED_TASK);
        }

        arrayAdapter = new CountedGroupArrayAdapter(this, getGroups());

        final ListView list =
                (ListView)findViewById(R.id.groups_overview_list);
        groupsListId = list.getId();
        list.setAdapter(arrayAdapter);
        list.setOnItemClickListener(new OnGroupClickListener());

        registerForContextMenu(list); //Register groups-list for context-menu
    }

    /**
     * Save the loginPw, if this activity is unexpectedly killed.
     *
     * @param outState - The Bundle where to save the loginPw and the flag,
     *  whether a new group was created or not.
     */
    @Override
    protected void onSaveInstanceState(final Bundle outState)
    {
        outState.putString(SAVED_LOGIN_PW, loginPw);
        outState.putBoolean(SAVED_TASK, createNewGroup);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(final int requestCode,
            final int resultCode, final Intent data)
    {
        CountedGroup group = null;

        if (resultCode == RESULT_OK && data != null)
        {
            group = data.getParcelableExtra(Constants.INTENT_EXTRA_RESULT);
            if (group != null)
            {
                arrayAdapter.add(group);
            }
            else
            {
                LogHelper.logW(CLAZZ, "Gruppe war nicht gesetzt, bzw. NULL!");
            }
        }
    }

    /**
     * Updates the groups-list.
     */
    @Override
    protected void onRestart()
    {
        final int count = arrayAdapter.getCount();
        ServiceMethodProvider service = null;
        CountedGroup group = null;

        super.onRestart();
        //If a new group was created, updating groups-list is not needed
        if (!createNewGroup)
        {
            if (count > 0)
            {
                service = new ServiceMethodProvider();
                for (int i = 0; i < count; i++)
                {
                    group = arrayAdapter.getItem(i);
                    if (group != null)
                    {
                        try
                        {
                            group.setMemberCount(service.getMemberCount(
                                    group.getGroupId()));
                            arrayAdapter.update(group, i);
                        }
                        catch (final RemoteException re)
                        {
                            if (re.getMessage() != null)
                            {
                                errorMessage = re.getMessage();
                            }
                            else
                            {
                                errorMessage = "";
                            }
                            LogHelper.logW(CLAZZ, re.getMessage());
                        }
                    }
                }
            }
        }
        else
        {
            createNewGroup = false;
        }
    }

    /**
     * Returns to the ServicePreferenceActivity, if the BACK-button was
     * pressed.
     */
    @Override
    public void onBackPressed()
    {
        final Intent intent =
                new Intent(this, ServicePreferenceActivity.class);

        intent.putExtra(Constants.INTENT_EXTRA_LOGIN_PW, loginPw);
        startActivity(intent);
        super.onBackPressed();
    }

    /**
     * Calls to UsersToAddChooserActivity, so the users, who should be added to
     * the group, can be chosen.
     *
     * @param view - The view
     */
    public void add(final View view)
    {
        final Intent intent = new Intent(this, GroupCreateActivity.class);

        createNewGroup = true;
        intent.putExtra(Constants.INTENT_EXTRA_USER, registeredUser);
        startActivityForResult(intent, INTENT_FOR_RESULT);
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenuInfo menuInfo)
    {
        if (groupsListId == v.getId())
        {
            getMenuInflater().inflate(
                    R.menu.groupmanagemententry_context, menu);
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
            case R.id.gm_context_add_user:
                showUserChooserDialog(arrayAdapter.getItem(menuInfo.position));
                return true;
            case R.id.gm_context_delete_group:
                adapterPosition = menuInfo.position;
                showDialog(POPUP_CONFIRM_REMOVE);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private Dialog createConfirmDialog(final int dialogId)
    {
        AlertDialog.Builder alertDialogBuilder = null;
        Group group = null;

        if (adapterPosition >= 0 && adapterPosition < arrayAdapter.getCount())
        {
            group = arrayAdapter.getItem(adapterPosition);
            if (group != null)
            {
                alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(R.string.gm_popup_confirm_title);
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.gm_popup_confirm_remove),
                        group.getGroupName()));
                alertDialogBuilder.setPositiveButton(R.string.yes,
                        new OnYesRemoveClickListener(dialogId));
                alertDialogBuilder.setNegativeButton(R.string.no,
                        new OnOkClickListener(dialogId));

                return alertDialogBuilder.create();
            }
            else
            {
                LogHelper.logE(CLAZZ, "Variable \"group\" war NULL!");
                return createInfoDialog(POPUP_NO_GROUP);
            }
        }
        else
        {
            LogHelper.logE(CLAZZ, "Variable \"adapterPosition\" ist kein "
                    + "gültiger Index: " + adapterPosition);
            return createInfoDialog(POPUP_NO_GROUP);
        }
    }

    private Dialog createInfoDialog(final int dialogId)
    {
        final AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(R.string.gm_popup_title);

        switch(dialogId)
        {
            case POPUP_ERROR:
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.gm_popup_request_error_pref), errorMessage));
                break;
            case POPUP_REMOVE_SUCCESS:
                alertDialogBuilder.setMessage(
                        R.string.gm_popup_remove_success);
                break;
            case POPUP_NO_KEY:
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.gm_popup_request_error_pref),
                        getString(R.string.gm_popup_key_error_msg)));
                break;
            case POPUP_NO_ALGORITHM:
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.gm_popup_request_error_pref),
                        getString(R.string.gm_popup_algorithm_error_msg)));
                break;
            case POPUP_NO_TOKEN:
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.gm_popup_request_error_pref),
                        getString(R.string.gm_popup_token_error_msg)));
                break;
            case POPUP_NO_GROUP:
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.gm_popup_request_error_pref),
                        getString(R.string.gm_popup_group_error_msg)));
                break;
            case POPUP_REMOVE_FAIL:
                alertDialogBuilder.setMessage(R.string.gm_popup_remove_fail);
        }

        alertDialogBuilder.setNeutralButton(R.string.ok,
                new OnOkClickListener(dialogId));
        alertDialogBuilder.setCancelable(false);

        return alertDialogBuilder.create();
    }

    private void showUserChooserDialog(final Group group)
    {
        Intent intent = null;
        final Sec2MiddlewarePreferenceManager prefManager =
                new Sec2MiddlewarePreferenceManager(getApplicationContext());
        final String key = prefManager.getSessionKey();
        String algorithm = null;
        String token = null;
        UserHandler userHandler = null;
        GetUsersInGroupTask guigTask = null;
        User[] users = null;
        final int port = prefManager.getServerListenPort();

        //First fetch the IDs of the group-members
        if (group != null)
        {
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
                            //Then start the intent
                            guigTask = new GetUsersInGroupTask(key, algorithm,
                                    token, port, group.getGroupId());
                            guigTask.execute();
                            users = guigTask.get();
                            if(guigTask.getException() != null)
                            {
                                throw guigTask.getException();
                            }
                            userHandler = new UserHandler(users);
                            intent = new Intent(this,
                                    UsersToAddChooserActivity.class);
                            intent.putExtra(Constants.INTENT_EXTRA_GROUP,
                                    group);
                            intent.putExtra(Constants.INTENT_EXTRA_USER_IDS,
                                    userHandler.getUserIds());
                            startActivity(intent);
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
                            showDialog(POPUP_ERROR);
                            LogHelper.logE(CLAZZ, e.getMessage());
                        }
                    }
                    else
                    {
                        showDialog(POPUP_NO_TOKEN);
                        LogHelper.logE(CLAZZ, "Kein Sitzungstoken in den "
                                + "Preferences gefunden!");
                    }
                }
                else
                {
                    showDialog(POPUP_NO_ALGORITHM);
                    LogHelper.logE(CLAZZ, "Kein Sitzungsalgorithmus in den "
                            + "Preferences gefunden!");
                }
            }
            else
            {
                showDialog(POPUP_NO_KEY);
                LogHelper.logE(CLAZZ, "Kein Sitzungsschlüssel in den "
                        + "Preferences gefunden!");
            }
        }
        else
        {
            showDialog(POPUP_NO_GROUP);
            LogHelper.logE(CLAZZ, "Variable \"group\" war NULL!");
        }
    }

    private ArrayList<CountedGroup> getGroups()
    {
        final Sec2MiddlewarePreferenceManager prefManager =
                new Sec2MiddlewarePreferenceManager(getApplicationContext());
        final String key = prefManager.getSessionKey();
        ServiceMethodProvider service = null;
        String algorithm = null;
        String token = null;
        Group[] groups = null;
        final LinkedList<CountedGroup> regUserGroups =
                new LinkedList<CountedGroup>();
        Group tmpGroup = null;
        GetRegisteredUserAndGroupsTask gruagTask = null;
        GetGroupTask ggTask = null;
        ResultContainer taskResult = null;
        final int port = prefManager.getServerListenPort();
        int membersCount;


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
                        gruagTask = new GetRegisteredUserAndGroupsTask(key,
                                algorithm, token, port);
                        gruagTask.execute();
                        taskResult = gruagTask.get();
                        if(gruagTask.getException() != null)
                        {
                            throw gruagTask.getException();
                        }
                        registeredUser = taskResult.getUser();
                        EmailToUserNameMapper.emailToUserName(
                                this, registeredUser);
                        groups = taskResult.getGroups();
                        service = new ServiceMethodProvider();
                        for (int i = 0; i < groups.length; i++)
                        {
                            //Try to fetch group-owner
                            if (!groups[i].isGroupOwnerSet())
                            {
                                ggTask = new GetGroupTask(key, algorithm,
                                        token, port, groups[i].getGroupId());
                                ggTask.execute();
                                tmpGroup = ggTask.get();
                                if(ggTask.getException() != null)
                                {
                                    throw ggTask.getException();
                                }
                                //Update group, if tmpGroup != null
                                if (tmpGroup != null)
                                {
                                    groups[i] = tmpGroup;
                                }
                            }
                            //If registered user owns group, fetch count of
                            //group-members and add group to result-list
                            if (groups[i].isGroupOwnerSet()
                                    && registeredUser.getUserId().equals(
                                            groups[i].getGroupOwner()
                                            .getUserId()))
                            {
                                membersCount = service.getMemberCount(
                                        groups[i].getGroupId());
                                if (membersCount < 0)
                                {
                                    LogHelper.logW(CLAZZ, MessageFormat.format(
                                            "\"membersCount\" war kleiner "
                                                    + "0: {0}", membersCount));
                                }
                                regUserGroups.add(new CountedGroup(
                                        groups[i].getGroupId(),
                                        groups[i].getGroupName(),
                                        registeredUser, membersCount));
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
                        showDialog(POPUP_ERROR);
                        LogHelper.logE(CLAZZ, e.getMessage());
                    }
                }
                else
                {
                    showDialog(POPUP_NO_TOKEN);
                    LogHelper.logE(CLAZZ, "Kein Sitzungstoken in den "
                            + "Preferences gefunden!");
                }
            }
            else
            {
                showDialog(POPUP_NO_ALGORITHM);
                LogHelper.logE(CLAZZ, "Kein Sitzungsalgorithmus in den "
                        + "Preferences gefunden!");
            }
        }
        else
        {
            showDialog(POPUP_NO_KEY);
            LogHelper.logE(CLAZZ, "Kein Sitzungsschlüssel in den Preferences"
                    + " gefunden!");
        }

        return new ArrayList<CountedGroup>(regUserGroups);
    }

    @Override
    protected Dialog onCreateDialog(final int id)
    {
        if (id == POPUP_CONFIRM_REMOVE)
        {
            return createConfirmDialog(id);
        }
        else
        {
            return createInfoDialog(id);
        }
    }

    private final class OnGroupClickListener implements OnItemClickListener
    {
        @Override
        public void onItemClick(final AdapterView<?> parent, final View view,
                final int position, final long id)
        {
            final Intent intent = new Intent(GroupManagementEntryActivity.this,
                    GroupManagementActivity.class);
            intent.putExtra(Constants.INTENT_EXTRA_GROUP,
                    (CountedGroup)(parent.getItemAtPosition(position)));
            startActivity(intent);
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

    private final class OnYesRemoveClickListener
    implements DialogInterface.OnClickListener
    {
        private final int dialogId;

        public OnYesRemoveClickListener(final int dialogId)
        {
            this.dialogId = dialogId;
        }

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
            Group group = null;

            if (adapterPosition >= 0
                    && adapterPosition < arrayAdapter.getCount())
            {
                group = arrayAdapter.getItem(adapterPosition);
                if (group != null)
                {
                    service = new ServiceMethodProvider();
                    try
                    {
                        if (service.deleteGroup(group.getGroupId()))
                        {
                            arrayAdapter.remove(adapterPosition);
                            showDialog(POPUP_REMOVE_SUCCESS);
                        }
                        else
                        {
                            showDialog(POPUP_REMOVE_FAIL);
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
                        showDialog(POPUP_ERROR);
                        LogHelper.logE(CLAZZ, "Es ist ein Fehler aufgetreten: "
                                + errorMessage);
                    }
                }
                else
                {
                    LogHelper.logE(CLAZZ, "Variable \"group\" ist NULL!");
                    showDialog(POPUP_NO_GROUP);
                }
            }
            else
            {
                LogHelper.logE(CLAZZ, "Variable \"adapterPosition\" ist kein "
                        + "gültiger Index: " + adapterPosition);
                showDialog(POPUP_NO_GROUP);
            }

            removeDialog(dialogId);
        }
    }

    private final class ResultContainer
    {
        private final User user;
        private final Group[] groups;

        public ResultContainer(final User user, final Group[] groups)
        {
            this.user = user;
            this.groups = groups;
        }

        public User getUser()
        {
            return user;
        }

        public Group[] getGroups()
        {
            return groups;
        }
    }

    private final class GetRegisteredUserAndGroupsTask
    extends MwAdapterTask<ResultContainer>
    {
        private final String key;
        private final String algorithm;
        private final String appName;
        private final int port;

        public GetRegisteredUserAndGroupsTask(final String key,
                final String algorithm, final String appName, final int port)
        {
            this.key = key;
            this.algorithm = algorithm;
            this.appName = appName;
            this.port = port;
        }

        @Override
        protected ResultContainer doInBackground(final Void... params)
        {
            final MwAdapter mwAdapter = MwAdapter.getInstance();
            ResultContainer result = null;
            User user = null;
            Group[] groups = null;

            try
            {
                user = mwAdapter.getRegisteredUser(key, algorithm, appName, port);
                groups = mwAdapter.getGroupsForRegisteredUser(key, algorithm, appName, port);
                result = new ResultContainer(user, groups);
            }
            catch (final Exception e)
            {
                exception = e;
            }

            return result;
        }
    }
}

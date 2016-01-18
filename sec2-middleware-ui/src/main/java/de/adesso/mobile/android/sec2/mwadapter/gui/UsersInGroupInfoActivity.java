package de.adesso.mobile.android.sec2.mwadapter.gui;

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
import android.widget.TableRow;
import android.widget.TextView;
import de.adesso.mobile.android.sec2.mwadapter.logging.LogHelper;
import de.adesso.mobile.android.sec2.mwadapter.model.Group;
import de.adesso.mobile.android.sec2.mwadapter.model.User;
import de.adesso.mobile.android.sec2.mwadapter.tasks.GetGroupTask;
import de.adesso.mobile.android.sec2.mwadapter.tasks.GetUsersInGroupTask;
import de.adesso.mobile.android.sec2.mwadapter.util.EmailToUserNameMapper;
import de.adesso.mobile.android.sec2.mwadapter.util.MwAdapterPreferenceManager;
import de.adesso.mobile.android.sec2.mwadapter.util.UserHandler;
import de.adesso.mobile.android.sec2.mwapdater.R;

/**
 * This activity displays informations about the group, which was passed together with the
 * intent calling this activity, and a list of all users, who are member in this group.
 * 
 * @author nike
 *
 */
public class UsersInGroupInfoActivity extends BaseMwAdapterActivity
{
    private static final int POPUP_NO_KEY_OR_ALGORITHM = 0;
    private static final int POPUP_NO_GROUP_FOUND = 1;
    private static final int POPUP_ERROR = 2;

    private String errorMessage = null;
    private UserHandler userHandler = null;

    @Override
    protected void doOnCreateLayout(final Bundle savedInstanceState)
    {
        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.usersingroupinfo);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.mwadaptertitlebar);
    }

    @Override
    protected void doOnCreateMisc(final Bundle savedInstanceState)
    {
        final Group group = getIntent().getParcelableExtra("group");
        final MwAdapterPreferenceManager prefManager = new MwAdapterPreferenceManager(getApplicationContext());

        //Get group information
        requestGroupInfos(group, prefManager.getAppAuthKey(), prefManager.getAppAuthKeyAlgorithm(),
                getApplication().getPackageName(), prefManager.getMiddlewarePort());
    }

    /**
     * This method requests information about the passed group. Information contain
     * the name of the group-owner and a list of all users, who are member in this group.
     * 
     * @param group - The group-object, whose information are to be shown
     * @param key - The app authentication key
     * @param algorithm - The algorithm of the app authentication key
     * @param appName - The app's name the app authentication key is bound to at the Sec2-middleware
     * @param port - The port, where the Sec2-middleware is listening on
     * 
     * @return Returns an updated Group-object
     */
    protected Group requestGroupInfos(Group group, final String key, final String algorithm, final String appName,
            final int port)
    {
        User[] users = null;
        User owner = null;
        Group tmpGroup = null;
        ListView list = null;
        TextView title = null;
        final TextView ownerView = (TextView)findViewById(R.id.usersingroupinfo_owner_value);;
        String ownerName = "";
        String groupName = null;
        GetGroupTask ggTask = null;
        GetUsersInGroupTask guigTask = null;

        try
        {
            if(group != null)
            {
                //Display title
                title = (TextView)findViewById(R.id.title_lbl);
                groupName = group.getGroupName();
                if(groupName == null) groupName = "";
                title.setText(getString(R.string.usersingroupinfo_title) + " \"" + groupName + "\"");
                //If group-owner is set, get his name
                if(group.isGroupOwnerSet()) ownerName = group.getGroupOwner().getUserName();
                else
                {
                    //Else try to fetch the group-owner from the Sec2-middleware
                    if(key != null && algorithm != null)
                    {
                        ggTask = new GetGroupTask(key, algorithm, appName, port, group.getGroupId());
                        ggTask.execute();
                        tmpGroup = ggTask.get();
                        if(ggTask.getException() != null) throw ggTask.getException();
                        if(tmpGroup != null)
                        {
                            //Update Group-Object
                            group = tmpGroup;
                            //If we have now a group-owner, get his name
                            if(group.isGroupOwnerSet())
                            {
                                owner = group.getGroupOwner();
                                EmailToUserNameMapper.emailToUserName(this, owner);
                                if(owner.getUserName() != null) ownerName = owner.getUserName();
                                else ownerName = getString(R.string.username_not_found);
                            }
                        }
                    }
                }
                //Display as owner's name what we have found
                ownerView.setText(ownerName);
                //Get the users, who are member in this group
                if(key != null && algorithm != null)
                {
                    guigTask = new GetUsersInGroupTask(key, algorithm, appName, port, group.getGroupId());
                    guigTask.execute();
                    users = guigTask.get();
                    if(guigTask.getException() != null) throw guigTask.getException();
                    if(users != null)
                    {
                        for(int i = 0; i < users.length; i++)
                        {
                            EmailToUserNameMapper.emailToUserName(this, users[i]);
                            if(users[i].getUserName() == null) users[i].setUserName(getString(R.string.username_not_found));
                        }
                        userHandler = new UserHandler(users);
                        list = (ListView)findViewById(R.id.usersingroup_list);
                        list.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, userHandler.getUserNames()));
                        list.setOnItemClickListener(new OnUserClickListener());
                    }
                }
                else showDialog(POPUP_NO_KEY_OR_ALGORITHM);
            }
            else showDialog(POPUP_NO_GROUP_FOUND);
        }
        catch(final Exception e)
        {
            if(e.getMessage() != null) errorMessage = e.getMessage();
            else errorMessage = "";
            showDialog(POPUP_ERROR);
            LogHelper.logE(e.getMessage());
        }

        return group;
    }

    @Override
    protected Dialog onCreateDialog(final int id)
    {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(R.string.usersingroupinfo_success_title);

        switch(id)
        {
            case POPUP_NO_KEY_OR_ALGORITHM:
                alertDialogBuilder.setMessage(R.string.usersingroupinfo_success_false_key);
                break;
            case POPUP_NO_GROUP_FOUND:
                alertDialogBuilder.setMessage(R.string.usersingroupinfo_success_false_group);
                break;
            case POPUP_ERROR:
                alertDialogBuilder.setMessage(getString(R.string.usersingroupinfo_success_false_error) + errorMessage);
                break;
        }
        alertDialogBuilder.setNeutralButton(R.string.ok, null);
        alertDialogBuilder.setCancelable(false);

        return alertDialogBuilder.create();
    }

    /**
     * Returns the TableRow-object, which contains the TextView-objects displaying the group-owner's name.
     * This method can be used, if one wants to add more elements/views to the row or modify its appearance.
     * 
     * @return The TableRow-object for the group-owner's name.
     */
    protected TableRow getGroupOwnerTableRow()
    {
        return (TableRow)(findViewById(R.id.owner_row));
    }

    /**
     * Returns the TableRow-object, which contains the TextView displaying R.string.usersingroupinfo_members.
     * This method can be used, if one wants to add more elements/views to the row or modify its appearance.
     * 
     * @return The TableRow-object for the users-memberships.
     */
    protected TableRow getMembersTableRow()
    {
        return (TableRow)(findViewById(R.id.members_row));
    }

    /**
     * This method returns an string-array with all IDs of the users of the list displaying all users, who are a member in the group.
     * If the list isn't initialised yet or if the list doesn't contain any IDs, an empty array is returned.
     * 
     * @return A string-array with all IDs of the users of the list. Returns an empty array, if the list isn't initialised yet or if doesn't
     * contain any IDs.
     */
    protected String[] getUserIdsInList()
    {
        if(userHandler != null) return userHandler.getUserIds();
        else return new String[0];
    }

    /**
     * This method returns the User-object at the given position of the list displaying all users, who are a member of this group.
     * If the list isn't initialised yet or if the given position is out of bound (position < 0 || position > list-size), NULL will be returned.
     * 
     * @param position - The position in the list of the group to be returned
     * 
     * @return The user at the given position of the list. Returns NULL, if the list isn't initialised yet or if the given position is
     *  out of bound.
     */
    protected User getUserByListPosition(final int position)
    {
        if(userHandler != null) return userHandler.getUserAtIndex(position);
        else return null;
    }

    /**
     * This method returns the number of users in the members-list. If the list isn't initialised yet, -1 will be returned.
     * 
     * @return The number of users in the members-list. Returns -1, if the list isn't initialised yet.
     */
    protected int getCount()
    {
        if(userHandler != null) return userHandler.getCount();
        else return -1;
    }

    /**
     * This method returns the ListView-object displaying all users, which are a member of the group.
     * 
     * @return The ListView-object of the list displaying all users, which are a member of the group.
     */
    protected ListView getUsersListView()
    {
        return (ListView)(findViewById(R.id.usersingroup_list));
    }

    private final class OnUserClickListener implements OnItemClickListener
    {
        @Override
        public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
        {
            final Intent intent = new Intent(UsersInGroupInfoActivity.this, UserInfoActivity.class);
            intent.putExtra("userId", userHandler.getUserAtIndex(position));
            startActivity(intent);
        }
    }
}
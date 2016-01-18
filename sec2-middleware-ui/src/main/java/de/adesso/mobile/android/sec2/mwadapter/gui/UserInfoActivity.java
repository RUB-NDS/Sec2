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
import de.adesso.mobile.android.sec2.mwadapter.tasks.GetGroupsForUserTask;
import de.adesso.mobile.android.sec2.mwadapter.util.GroupHandler;
import de.adesso.mobile.android.sec2.mwadapter.util.MwAdapterPreferenceManager;
import de.adesso.mobile.android.sec2.mwapdater.R;

/**
 * This activity displays informations about the user, which was passed together with the
 * intent calling this activity, and a list of all groups, where the user is a member.
 * 
 * @author nike
 *
 */
public class UserInfoActivity extends BaseMwAdapterActivity
{
    private static final int POPUP_NO_USER_FOUND = 0;
    private static final int POPUP_NO_KEY_OR_ALGORITHM = 1;
    private static final int POPUP_ERROR = 2;

    private String errorMessage = null;
    private GroupHandler groupHandler = null;

    @Override
    protected void doOnCreateLayout(final Bundle savedInstanceState)
    {
        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.userinfo);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.mwadaptertitlebar);
    }

    @Override
    protected void doOnCreateMisc(final Bundle savedInstanceState)
    {
        final User user = getIntent().getParcelableExtra("userId");
        final MwAdapterPreferenceManager prefManager = new MwAdapterPreferenceManager(getApplicationContext());

        requestUserInfos(user, prefManager.getAppAuthKey(), prefManager.getAppAuthKeyAlgorithm(),
                getApplication().getPackageName(), prefManager.getMiddlewarePort());
    }

    /**
     * This method requests list of all groups, where the user is a member, from the Sec2-middleware.
     * 
     * @param user - The user-object, whose information are to be shown
     * @param key - The app authentication key
     * @param algorithm - The algorithm of the app authentication key
     * @param appName - The app's name the app authentication key is bound to at the Sec2-middleware
     * @param port - The port, where the Sec2-middleware is listening on
     */
    protected void requestUserInfos(final User user, final String key, final String algorithm, final String appName, final int port)
    {
        ArrayAdapter<String> arrayAdapter = null;
        TextView label = null;
        TextView title = null;
        ListView list = null;
        GetGroupsForUserTask task = null;

        try
        {
            if(key != null && algorithm != null)
            {
                if(user != null)
                {
                    //Set the username
                    label = (TextView)findViewById(R.id.userinfo_username_value);
                    title = (TextView)findViewById(R.id.title_lbl);
                    if(user.getUserName() == null)
                    {
                        label.setText(R.string.username_not_found);
                        title.setText(getString(R.string.usersinfo_title) + " für \"" + user.getUserId() + "\"");
                    }
                    else
                    {
                        label.setText(user.getUserName());
                        if(user.getUserName().isEmpty())
                            title.setText(getString(R.string.usersinfo_title) + " für \"" + user.getUserId() + "\"");
                        else
                            title.setText(getString(R.string.usersinfo_title) + " für \"" + user.getUserName() + "\"");
                    }
                    //Set the useremail
                    label = (TextView)findViewById(R.id.userinfo_useremail_value);
                    if(user.getUserEmail() == null || user.getUserEmail().isEmpty()) label.setText("");
                    else label.setText(user.getUserEmail());
                    //Show the groups where the user is member
                    task = new GetGroupsForUserTask(key, algorithm, appName, port, user.getUserId());
                    task.execute();
                    groupHandler = new GroupHandler(task.get());
                    if(task.getException() == null)
                    {
                        list = (ListView)findViewById(R.id.userinfo_grouplist);
                        arrayAdapter = new ArrayAdapter<String>(this, R.layout.list_item, groupHandler.getGroupNames());
                        list.setAdapter(arrayAdapter);
                        list.setOnItemClickListener(new OnGroupClickListener());
                    }
                    else throw task.getException();
                }
                else showDialog(POPUP_NO_USER_FOUND);
            }
            else showDialog(POPUP_NO_KEY_OR_ALGORITHM);
        }
        catch(final Exception e)
        {
            if(e.getMessage() != null) errorMessage = e.getMessage();
            else errorMessage = "";
            showDialog(POPUP_ERROR);
            LogHelper.logE(e.getMessage());
        }
    }

    @Override
    protected Dialog onCreateDialog(final int id)
    {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(R.string.userinfo_success_title);

        switch(id)
        {
            case POPUP_NO_USER_FOUND:
                alertDialogBuilder.setMessage(R.string.userinfo_success_false_user);
                break;
            case POPUP_NO_KEY_OR_ALGORITHM:
                alertDialogBuilder.setMessage(R.string.userinfo_success_false_key);
                break;
            case POPUP_ERROR:
                alertDialogBuilder.setMessage(getString(R.string.userinfo_success_false_error) + errorMessage);
                break;
        }
        alertDialogBuilder.setNeutralButton(R.string.ok, null);
        alertDialogBuilder.setCancelable(false);

        return alertDialogBuilder.create();
    }

    /**
     * Returns the TableRow-object, which contains the TextView displaying R.string.userinfo_groupmemberships.
     * This method can be used, if one wants to add more elements/views to the row or modify its appearance.
     * 
     * @return The TableRow-object for the group-memberships.
     */
    protected TableRow getGroupTableRow()
    {
        return (TableRow)(findViewById(R.id.group_row));
    }

    /**
     * Returns the TableRow-object, which contains the TextView-objects displaying user's email-address.
     * This method can be used, if one wants to add more elements/views to the row or modify its appearance.
     * 
     * @return The TableRow-object for the user's email-address.
     */
    protected TableRow getEmailTableRow()
    {
        return (TableRow)(findViewById(R.id.email_row));
    }

    /**
     * Returns the TableRow-object, which contains the TextView-objects displaying user's name.
     * This method can be used, if one wants to add more elements/views to the row or modify its appearance.
     * 
     * @return The TableRow-object for the user's name.
     */
    protected TableRow getNameTableRow()
    {
        return (TableRow)(findViewById(R.id.name_row));
    }

    /**
     * This method returns the ListView-object displaying all groups, where the user is a member.
     * 
     * @return The view-ID of the list displaying all groups, where the user is a member.
     */
    protected ListView getGroupsListView()
    {
        return (ListView)(findViewById(R.id.userinfo_grouplist));
    }

    /**
     * This method returns the Group-object at the given position of the list displaying all groups, where the user is a member.
     * If the list isn't initialised yet or if the given position is out of bound (position < 0 || position > list-size), NULL will be returned.
     * 
     * @param position - The position in the list of the group to be returned
     * 
     * @return The group at the given position of the list. Returns NULL, if the list isn't initialised yet or if the given position is
     *  out of bound.
     */
    protected Group getGroupByListPosition(final int position)
    {
        if(groupHandler != null) return groupHandler.getGroupAtIndex(position);
        else return null;
    }

    /**
     * This method returns a string-array with all IDs of the groups of the list displaying all groups, where the user is a member.
     * If the list isn't initialised yet or if the list doesn't contain any IDs, an empty array is returned.
     * 
     * @return A string-array with all IDs of the groups of the list. Returns an empty array, if the list isn't initialised yet or if doesn't
     * contain any IDs.
     */
    protected String[] getGroupIdsInList()
    {
        if(groupHandler != null) return groupHandler.getGroupIds();
        else return new String[0];
    }

    private final class OnGroupClickListener implements OnItemClickListener
    {
        @Override
        public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
        {
            Intent intent = null;
            Group group = null;

            if(groupHandler != null)
            {
                group = groupHandler.getGroupAtIndex(position);
                if(group != null)
                {
                    intent = new Intent(UserInfoActivity.this, UsersInGroupInfoActivity.class);
                    intent.putExtra("group", group);
                    startActivity(intent);
                }
                else
                {
                    LogHelper.logW(UserInfoActivity.class, "Variable \"group\" war NULL!");
                    errorMessage = "";
                    showDialog(POPUP_ERROR);
                }
            }
            else
            {
                LogHelper.logW(UserInfoActivity.class, "Variable \"groupHandler\" war NULL!");
                errorMessage = "";
                showDialog(POPUP_ERROR);
            }
        }
    }
}
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
import android.widget.TextView;
import de.adesso.mobile.android.sec2.mwadapter.MwAdapter;
import de.adesso.mobile.android.sec2.mwadapter.logging.LogHelper;
import de.adesso.mobile.android.sec2.mwadapter.model.User;
import de.adesso.mobile.android.sec2.mwadapter.tasks.MwAdapterTask;
import de.adesso.mobile.android.sec2.mwadapter.util.EmailToUserNameMapper;
import de.adesso.mobile.android.sec2.mwadapter.util.MwAdapterPreferenceManager;
import de.adesso.mobile.android.sec2.mwadapter.util.UserHandler;
import de.adesso.mobile.android.sec2.mwapdater.R;

/**
 * This activity displays informations about the user, who is registered at the Sec2-middleware, and a list of
 * all users, who are known to the registered user.
 * 
 * @author nike
 *
 */
public class UsersInfoActivity extends BaseMwAdapterActivity
{
    private static final int POPUP_NO_KEY_OR_ALGORITHM = 0;
    private static final int POPUP_ERROR = 1;
    private static final int POPUP_NO_REGISTERED_USER_FOUND = 2;

    private String errorMessage = null;
    private UserHandler userHandler = null;

    @Override
    protected void doOnCreateLayout(final Bundle savedInstanceState)
    {
        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.usersinfo);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.mwadaptertitlebar);
        ((TextView)findViewById(R.id.title_lbl)).setText(R.string.usersinfo_title);
    }

    @Override
    protected void doOnCreateMisc(final Bundle savedInstanceState)
    {
        final MwAdapterPreferenceManager prefManager = new MwAdapterPreferenceManager(getApplicationContext());
        requestUserInfos(prefManager.getAppAuthKey(), prefManager.getAppAuthKeyAlgorithm(),
                getApplication().getPackageName(), prefManager.getMiddlewarePort());
    }

    /**
     * This method requests from the Sec2-middleware informations about the user, who is registered
     * at the Sec2-middleware, and the list of all users known to the registered user.
     * 
     * @param key - The app authentication key
     * @param algorithm - The algorithm of the app authentication key
     * @param appName - The app's name the app authentication key is bound to at the Sec2-middleware
     * @param port - The port, where the Sec2-middleware is listening on
     */
    protected void requestUserInfos(final String key, final String algorithm, final String appName, final int port)
    {
        ArrayAdapter<String> arrayAdapter = null;
        User user = null;
        TextView label = null;
        ListView list = null;
        RequestUserInfosTask task = null;
        ResultContainer result = null;
        User[] users = null;

        try
        {
            if(key != null && algorithm != null)
            {
                task = new RequestUserInfosTask(key, algorithm, appName, port);
                task.execute();
                result = task.get();
                user = result.getRegisteredUser();
                if(user != null)
                {
                    EmailToUserNameMapper.emailToUserName(this, user);
                    //Set the username
                    label = (TextView)findViewById(R.id.usersinfo_username_value);
                    if(user.getUserName() == null)
                        user.setUserName(getString(R.string.username_not_found));
                    label.setText(user.getUserName());
                    //Set the useremail
                    label = (TextView)findViewById(R.id.usersinfo_useremail_value);
                    if(user.getUserEmail() == null || user.getUserEmail().isEmpty()) label.setText("");
                    else label.setText(user.getUserEmail());
                }
                else showDialog(POPUP_NO_REGISTERED_USER_FOUND);
                //Show all available users
                users = result.getUsers();
                if(users != null)
                {
                    for(int i = 0; i < users.length; i++)
                    {
                        EmailToUserNameMapper.emailToUserName(this, users[i]);
                        if(users[i].getUserName() == null)
                            users[i].setUserName(getString(R.string.username_not_found));
                    }
                }
                userHandler = new UserHandler(users);
                list = (ListView)findViewById(R.id.usersinfo_alluserslist);
                arrayAdapter = new ArrayAdapter<String>(this, R.layout.list_item, userHandler.getUserNames());
                list.setAdapter(arrayAdapter);
                list.setOnItemClickListener(new OnUserClickListener());
            }
            else showDialog(POPUP_NO_KEY_OR_ALGORITHM);
        }
        catch(final Exception e)
        {
            if(e.getMessage() != null) errorMessage = e.getMessage();
            else errorMessage = "";
            showDialog(POPUP_ERROR);
            LogHelper.logE(errorMessage);
        }
    }

    @Override
    protected Dialog onCreateDialog(final int id)
    {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(R.string.usersinfo_success_title);

        switch(id)
        {
            case POPUP_NO_KEY_OR_ALGORITHM:
                alertDialogBuilder.setMessage(R.string.usersinfo_success_false_key);
                break;
            case POPUP_ERROR:
                alertDialogBuilder.setMessage(getString(R.string.usersinfo_success_false_error) + errorMessage);
                break;
            case POPUP_NO_REGISTERED_USER_FOUND:
                alertDialogBuilder.setMessage(R.string.usersinfo_success_false_no_registered_user);
        }
        alertDialogBuilder.setNeutralButton(R.string.ok, null);
        alertDialogBuilder.setCancelable(false);

        return alertDialogBuilder.create();
    }

    /**
     * This method returns the ListView-object displaying all users, which are known to the registered user.
     * 
     * @return The view-ID of the list displaying all users, which are known to the registered user.
     */
    protected ListView getUsersListView()
    {
        return (ListView)(findViewById(R.id.usersinfo_alluserslist));
    }

    /**
     * This method returns the User-object at the given position of the list displaying all users, which are known to the registered user.
     * If the list isn't initialised yet or if the given position is out of bound (position < 0 || position > list-size), NULL will be returned.
     * 
     * @param position - The position in the list of the user to be returned
     * 
     * @return The user at the given position of the list. Returns NULL, if the list isn't initialised yet or if the given position is
     *  out of bound.
     */
    protected User getUserByListPosition(final int position)
    {
        if(userHandler != null) return userHandler.getUserAtIndex(position);
        else return null;
    }

    private final class RequestUserInfosTask extends MwAdapterTask<ResultContainer>
    {

        private final String key;
        private final String algorithm;
        private final String appName;
        private final int port;

        public RequestUserInfosTask(final String key, final String algorithm, final String appName, final int port)
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
            User user = null;
            User[] users = null;

            try
            {
                user = mwAdapter.getRegisteredUser(key, algorithm, appName, port);
                users = mwAdapter.getAllUsers(key, algorithm, appName, port);
            }
            catch (final Exception e)
            {
                exception = e;
            }

            return new ResultContainer(user, users);
        }

        @Override
        protected void onPostExecuteWithException(final ResultContainer result)
        {
            super.onPostExecuteWithException(result);
            showDialog(POPUP_ERROR);
        }
    }

    private final class ResultContainer
    {
        private User registeredUser = null;
        private User[] users = null;

        public ResultContainer(final User registeredUser, final User[] users)
        {
            this.registeredUser = registeredUser;
            this.users = users;
        }

        public User getRegisteredUser()
        {
            return registeredUser;
        }

        public User[] getUsers()
        {
            return users;
        }
    }

    private final class OnUserClickListener implements OnItemClickListener
    {
        @Override
        public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
        {
            Intent intent = null;
            User user = null;

            if(userHandler != null)
            {
                user = userHandler.getUserAtIndex(position);
                if(user != null)
                {
                    intent = new Intent(UsersInfoActivity.this, UserInfoActivity.class);
                    intent.putExtra("userId", user);
                    startActivity(intent);
                }
                else
                {
                    LogHelper.logW(UsersInfoActivity.class, "Variable \"user\" war NULL!");
                    errorMessage = "";
                    showDialog(POPUP_ERROR);
                }
            }
            else
            {
                LogHelper.logW(UsersInfoActivity.class, "Variable \"userHandler\" war NULL!");
                errorMessage = "";
                showDialog(POPUP_ERROR);
            }
        }
    }
}
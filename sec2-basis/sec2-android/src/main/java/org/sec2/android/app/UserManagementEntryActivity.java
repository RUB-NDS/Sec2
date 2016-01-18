package org.sec2.android.app;

import java.text.MessageFormat;

import org.sec2.android.util.Constants;
import org.sec2.android.util.Sec2MiddlewarePreferenceManager;
import org.sec2.middleware.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import de.adesso.mobile.android.sec2.mwadapter.gui.UsersInfoActivity;
import de.adesso.mobile.android.sec2.mwadapter.logging.LogHelper;
import de.adesso.mobile.android.sec2.mwadapter.model.User;

/**
 * This activity is the entry activity to the user-management of the
 * administration-app of the Sec2-middleware. The activity displays
 * informations about the user, who is registered at the Sec2-middleware, and a
 * list of all users, who are known to the registered user. It extends the
 * UsersInfoActivity from the MwAdapter-library.
 *
 * @author nike
 */
public class UserManagementEntryActivity extends UsersInfoActivity
{
    private static final int POPUP_USER_MANAGEMENT_ERROR = 100;
    private static final String SAVED_USER = "userId";

    private String loginPw = null;
    private String errorMessage = null;
    private User user = null;

    @Override
    protected void doOnCreateMisc(final Bundle savedInstanceState)
    {
        final Sec2MiddlewarePreferenceManager prefManager =
                new Sec2MiddlewarePreferenceManager(getApplicationContext());
        final ListView usersList = getUsersListView();

        if (savedInstanceState == null)
        {
            loginPw = getIntent().getStringExtra(
                    Constants.INTENT_EXTRA_LOGIN_PW);
        }
        else
        {
            loginPw = savedInstanceState.getString(
                    Constants.INTENT_EXTRA_LOGIN_PW);
            user = savedInstanceState.getParcelable(SAVED_USER);
        }
        requestUserInfos(prefManager.getSessionKey(),
                prefManager.getSessionKeyAlgorithm(),
                prefManager.getSessionToken(),
                prefManager.getServerListenPort());
        if (usersList != null)
        {
            usersList.setOnItemLongClickListener(
                    new UsersListOnItemLongClickListener());
            usersList.setOnItemClickListener(
                    new UsersListOnItemClickListener());
        }
    }

    @Override
    protected Dialog onCreateDialog(final int id)
    {
        final AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(this);
        boolean passToSuperClass = false;

        alertDialogBuilder.setTitle(R.string.usermanagement_popup_title);
        switch(id)
        {
            case POPUP_USER_MANAGEMENT_ERROR:
                alertDialogBuilder.setMessage(errorMessage);
                break;
            default:
                passToSuperClass = true;
        }
        if (!passToSuperClass)
        {
            alertDialogBuilder.setNeutralButton(R.string.ok, null);
            alertDialogBuilder.setCancelable(false);
            return alertDialogBuilder.create();
        }
        else
        {
            return super.onCreateDialog(id);
        }
    }

    /**
     * This method overwrites the method BaseMwAdapterActivity.isPushToStack(),
     * because this activity should not be part of the
     * anti-activity-flooding-mechanism. Activity-flooding is not a threat
     * here, because the activity can't be called in a cyclic way.
     *
     * @return Always FALSE.
     */
    @Override
    protected boolean isPushToStack()
    {
        return false;
    }

    /**
     * Save the loginPw, if this activity is unexpectedly killed.
     *
     * @param outState - Bundle, where the loginPw and the user are saved
     */
    @Override
    protected void onSaveInstanceState(final Bundle outState)
    {
        outState.putString(Constants.INTENT_EXTRA_LOGIN_PW, loginPw);
        if (user != null)
        {
            outState.putParcelable(SAVED_USER, user);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed()
    {
        final Intent intent =
                new Intent(this, ServicePreferenceActivity.class);

        intent.putExtra(Constants.INTENT_EXTRA_LOGIN_PW, loginPw);
        startActivity(intent);
        super.onBackPressed();
    }

    private final class UsersListOnItemLongClickListener
    implements OnItemLongClickListener
    {
        @Override
        public boolean onItemLongClick(final AdapterView<?> parent,
                final View view, final int position, final long id)
        {
            Intent intent = null;
            User user = getUserByListPosition(position);

            if (user != null)
            {
                intent = new Intent(UserManagementEntryActivity.this,
                        GroupChooserActivity.class);
                intent.putExtra(Constants.INTENT_EXTRA_USER, user);
                startActivity(intent);
            }
            else
            {
                errorMessage =
                        getString(R.string.usermanagement_popup_userid_error);
                showDialog(POPUP_USER_MANAGEMENT_ERROR);
                LogHelper.logD(MessageFormat.format(getString(
                        R.string.usermanagement_userid_error), position));
            }
            return true;
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
            User user = getUserByListPosition(position);

            if (user != null)
            {
                intent = new Intent(UserManagementEntryActivity.this,
                        UserManagementActivity.class);
                intent.putExtra(Constants.INTENT_EXTRA_USER, user);
                startActivity(intent);
            }
            else
            {
                errorMessage =
                        getString(R.string.usermanagement_popup_userid_error);
                showDialog(POPUP_USER_MANAGEMENT_ERROR);
                LogHelper.logD(MessageFormat.format(getString(
                        R.string.usermanagement_userid_error), position));
            }
        }
    }
}

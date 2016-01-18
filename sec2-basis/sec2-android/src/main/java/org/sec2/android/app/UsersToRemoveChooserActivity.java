package org.sec2.android.app;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.sec2.android.app.component.CheckedUserHandler;
import org.sec2.android.util.Constants;
import org.sec2.middleware.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import de.adesso.mobile.android.sec2.mwadapter.logging.LogHelper;
import de.adesso.mobile.android.sec2.mwadapter.model.Group;
import de.adesso.mobile.android.sec2.mwadapter.model.User;

/**
 * The activity shows a list with users who can be selected through checkboxes.
 * When clicking the save-button, the selected users will be removed from the
 * group, which was passed together with the calling intent.
 *
 * @author schuessler
 */
public class UsersToRemoveChooserActivity extends AbstractUsersChooserActivity
{
    private Group group = null;
    private String errorMessage = null;

    private static final Class<?> CLAZZ = UsersToRemoveChooserActivity.class;
    private static final int POPUP_REMOVE_ERROR = 0;
    private static final int POPUP_SUCCESS = 1;
    private static final int POPUP_FAIL = 2;

    @Override
    protected void initTextView()
    {
        final TextView intro = getTextView();

        group = getIntent().getParcelableExtra(Constants.INTENT_EXTRA_GROUP);
        if (group != null)
        {
            intro.setText(MessageFormat.format(getString(
                    R.string.utr_chooser_intro), group.getGroupName()));
        }
        else
        {
            intro.setText(R.string.utr_chooser_intro_no_group);
            intro.setTextColor(Color.RED);
            LogHelper.logD(CLAZZ, "Extra \"" + Constants.INTENT_EXTRA_GROUP
                    + "\" war NULL");
        }
    }

    @Override
    protected CheckedUserHandler initListView()
    {
        final ArrayList<User> users = getIntent().getParcelableArrayListExtra(
                Constants.INTENT_EXTRA_USERS);
        final CheckedUserHandler userHandler =
                new CheckedUserHandler(users != null ? users.toArray(
                        new User[users.size()]) : null);
        final ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<String>(this, R.layout.check_list_item,
                        userHandler.getUserNames());

        getListView().setAdapter(arrayAdapter);

        return userHandler;
    }

    @Override
    protected Dialog onCreateDialog(final int id)
    {
        final AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(R.string.utr_chooser_popup_title);

        switch(id)
        {
            case POPUP_SUCCESS:
                alertDialogBuilder.setMessage(R.string.utr_chooser_success);
                break;
            case POPUP_REMOVE_ERROR:
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.utr_chooser_remove_error_pref),
                        errorMessage));
                break;
            case POPUP_FAIL:
                alertDialogBuilder.setMessage(R.string.utr_chooser_fail);
        }
        alertDialogBuilder.setNeutralButton(R.string.ok,
                new OnOkClickListener());
        alertDialogBuilder.setCancelable(false);

        return alertDialogBuilder.create();
    }

    /**
     * Is called, when the save-button is pressed. The method removes the
     * checked users from the group.
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
                    if (service.removeUsersFromGroup(checkedUserIds,
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
                    showDialog(POPUP_REMOVE_ERROR);
                    LogHelper.logE(errorMessage);
                }
            }
            else
            {
                LogHelper.logE(CLAZZ, "Variable \"group\" war NULL!");
                errorMessage = "";
                setResult(Activity.RESULT_CANCELED);
                showDialog(POPUP_REMOVE_ERROR);
            }
        }
        else
        {
            LogHelper.logE(CLAZZ, "Variable \"userHandler\" war NULL!");
            errorMessage = "";
            setResult(Activity.RESULT_CANCELED);
            showDialog(POPUP_REMOVE_ERROR);
        }
    }

    private class OnOkClickListener implements DialogInterface.OnClickListener
    {
        @Override
        public void onClick(final DialogInterface dialog, final int which)
        {
            UsersToRemoveChooserActivity.this.finish();
        }
    }
}

package org.sec2.android.app;

import java.text.MessageFormat;

import org.sec2.android.model.CountedGroup;
import org.sec2.android.util.Constants;
import org.sec2.middleware.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import de.adesso.mobile.android.sec2.mwadapter.logging.LogHelper;
import de.adesso.mobile.android.sec2.mwadapter.model.User;

/**
 * This class shows an EditView, where the user can enter a group-name for a
 * new group. The group is created, if the user clicks on the save-button.
 *
 * @author nike
 */
public class GroupCreateActivity extends Activity
{
    private static final int POPUP_ERROR = 0;
    private static final int POPUP_SUCCESS = 1;
    private static final Class<?> CLAZZ = GroupCreateActivity.class;
    private static final String REG_USER = "registeredUser";

    private Button saveBtn = null;
    private EditText groupName = null;
    private String errorMessage = "";
    private User registeredUser = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        setContentView(R.layout.group_create);

        saveBtn = (Button)(findViewById(R.id.group_create_save_btn));
        saveBtn.setEnabled(false);

        groupName = (EditText)(findViewById(R.id.group_create_name));
        groupName.addTextChangedListener(new TextListener());

        if (savedInstanceState == null)
        {
            registeredUser = getIntent().getParcelableExtra(
                    Constants.INTENT_EXTRA_USER);
        }
        else
        {
            registeredUser = savedInstanceState.getParcelable(REG_USER);
        }
    }

    /**
     * Saves the registered-user-object, if this activity is unexpectedly
     * killed.
     *
     * @param outState - Bundle were to save the registered-user-object
     */
    @Override
    protected void onSaveInstanceState(final Bundle outState)
    {
        outState.putParcelable(REG_USER, registeredUser);
        super.onSaveInstanceState(outState);
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
        String groupId = null;
        ServiceMethodProvider service = new ServiceMethodProvider();
        CountedGroup group = new CountedGroup();
        Intent result = null;

        group.setGroupName(groupName.getText().toString());
        group.setGroupOwner(registeredUser);
        //Registered user is the 1st member of this group
        group.setMemberCount(1);
        try
        {
            groupId = service.createNewGroup(group.getGroupName());
            if (groupId != null)
            {
                group.setGroupId(groupId);
                result = new Intent();
                result.putExtra(Constants.INTENT_EXTRA_RESULT, group);
                setResult(Activity.RESULT_OK, result);
                showDialog(POPUP_SUCCESS);
            }
            else
            {
                errorMessage =
                        getString(R.string.group_create_popup_error_id_msg);
                setResult(Activity.RESULT_CANCELED);
                showDialog(POPUP_ERROR);
                LogHelper.logE(CLAZZ, "Zurückgelieferte Gruppen-ID war NULL!");
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
            showDialog(POPUP_ERROR);
            LogHelper.logE(errorMessage);
        }
    }

    @Override
    protected Dialog onCreateDialog(final int id)
    {
        final AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(R.string.group_create_popup_title);

        switch(id)
        {
            case POPUP_SUCCESS:
                alertDialogBuilder.setMessage(
                        R.string.group_create_popup_success);
                alertDialogBuilder.setNeutralButton(
                        R.string.ok, new OnOkClickListener());
                break;
            case POPUP_ERROR:
                alertDialogBuilder.setMessage(MessageFormat.format(
                        getString(R.string.group_create_popup_error_pref),
                        errorMessage));
                alertDialogBuilder.setNeutralButton(R.string.ok, null);
        }
        alertDialogBuilder.setCancelable(false);

        return alertDialogBuilder.create();
    }

    private class TextListener implements TextWatcher
    {
        @Override
        public void beforeTextChanged(final CharSequence s, final int start,
                final int count, final int after)
        {
            //Nothing to do here
        }

        @Override
        public void onTextChanged(final CharSequence s, final int start,
                final int before, final int count)
        {
            //Nothing to do here
        }

        @Override
        public void afterTextChanged(final Editable s)
        {
            if (s == null || s.length() == 0)
            {
                saveBtn.setEnabled(false);
            }
            else
            {
                saveBtn.setEnabled(true);
            }
        }
    }

    private class OnOkClickListener implements DialogInterface.OnClickListener
    {
        @Override
        public void onClick(final DialogInterface dialog, final int which)
        {
            GroupCreateActivity.this.finish();
        }
    }
}

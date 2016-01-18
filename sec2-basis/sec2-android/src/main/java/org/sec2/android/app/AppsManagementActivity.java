package org.sec2.android.app;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import de.adesso.mobile.android.sec2.mwadapter.logging.LogHelper;

/**
 * This activity represents the app-management of the administration app of the
 * Sec2-middleware. The activity displays a list of all apps, which are
 * registered at the middleware.
 *
 * @author schuessler
 */
public class AppsManagementActivity extends Activity
{
    private static final String SAVED_LOGIN_PW = "loginPw";
    private static final Class<?> CLAZZ = AppsManagementActivity.class;
    private static final int POPUP_ERROR = 0;
    private static final int POPUP_CONFIRM = 1;
    private static final int POPUP_SUCCESS = 2;
    private static final int POPUP_FAIL = 3;

    private String loginPw = null;
    private String errorMessage = "";
    private String appId = null;
    private ArrayAdapter<String> arrayAdapter = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.apps_overview);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.apps_overview_titlebar);

        if (savedInstanceState == null)
        {
            loginPw = getIntent().getStringExtra(
                    Constants.INTENT_EXTRA_LOGIN_PW);
        }
        else
        {
            loginPw = savedInstanceState.getString(SAVED_LOGIN_PW);
        }

        arrayAdapter = new ArrayAdapter<String>(this,
                R.layout.simple_list_item, getAppIds());

        final ListView list = (ListView)findViewById(R.id.apps_overview_list);
        list.setAdapter(arrayAdapter);
        list.setOnItemLongClickListener(new AppsListOnItemLongClickListener());
    }

    /**
     * Save the loginPw, if this activity is unexpectedly killed.
     *
     * @param outState - The bundle where to save the loginPw
     */
    @Override
    protected void onSaveInstanceState(final Bundle outState)
    {
        outState.putString(SAVED_LOGIN_PW, loginPw);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected Dialog onCreateDialog(final int id)
    {
        if (id == POPUP_CONFIRM)
        {
            return createConfirmDialog(id);
        }
        else
        {
            return createInfoDialog(id);
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

    private Dialog createConfirmDialog(final int dialogId)
    {
        final AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(
                R.string.app_management_popup_confirm_title);
        alertDialogBuilder.setMessage(MessageFormat.format(
                getString(R.string.app_management_popup_confirm), appId));
        alertDialogBuilder.setPositiveButton(R.string.yes,
                new OnYesRemoveClickListener(dialogId));
        alertDialogBuilder.setNegativeButton(R.string.no,
                new OnOkClickListener(dialogId));

        return alertDialogBuilder.create();
    }

    private Dialog createInfoDialog(final int dialogId)
    {
        final AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(R.string.app_management_popup_title);

        switch(dialogId)
        {
            case POPUP_ERROR:
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.app_management_popup_request_error_pref),
                        errorMessage));
                break;
            case POPUP_SUCCESS:
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.app_management_popup_success), appId));
                break;
            case POPUP_FAIL:
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.app_management_popup_fail), appId));
        }

        alertDialogBuilder.setNeutralButton(R.string.ok,
                new OnOkClickListener(dialogId));
        alertDialogBuilder.setCancelable(false);

        return alertDialogBuilder.create();
    }

    private List<String> getAppIds()
    {
        final ServiceMethodProvider service = new ServiceMethodProvider();
        Sec2MiddlewarePreferenceManager prefManager =
                new Sec2MiddlewarePreferenceManager(getApplicationContext());
        ArrayList<String> appIds = new ArrayList<String>();

        try
        {
            //We return a list here, because arrayAdapter can't be modified, if
            //an array is returned
            appIds.addAll(Arrays.asList(service.getRegisteredAppIds(
                    prefManager.getSessionToken())));
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

        return appIds;
    }

    private final class OnOkClickListener implements
    DialogInterface.OnClickListener
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

    private final class AppsListOnItemLongClickListener implements
    OnItemLongClickListener
    {
        @Override
        public boolean onItemLongClick(final AdapterView<?> parent,
                final View view, final int position, final long id)
        {
            appId = parent.getItemAtPosition(position).toString();
            showDialog(POPUP_CONFIRM);
            return true;
        }
    }

    private final class OnYesRemoveClickListener implements
    DialogInterface.OnClickListener
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
            ServiceMethodProvider service = new ServiceMethodProvider();

            try
            {
                if (service.unregisterApp(appId))
                {
                    arrayAdapter.remove(appId);
                    showDialog(POPUP_SUCCESS);
                }
                else
                {
                    showDialog(POPUP_FAIL);
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

            removeDialog(dialogId);
        }
    }
}

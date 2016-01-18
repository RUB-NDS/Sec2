package de.adesso.mobile.android.sec2.mwadapter.gui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import de.adesso.mobile.android.sec2.mwadapter.MwAdapter;
import de.adesso.mobile.android.sec2.mwadapter.crypto.AppAuthKey;
import de.adesso.mobile.android.sec2.mwadapter.gui.components.HostNameValidator;
import de.adesso.mobile.android.sec2.mwadapter.gui.components.PathValidator;
import de.adesso.mobile.android.sec2.mwadapter.gui.components.PortRangeValidator;
import de.adesso.mobile.android.sec2.mwadapter.gui.components.ValidatedEditTextPreference;
import de.adesso.mobile.android.sec2.mwadapter.logging.LogHelper;
import de.adesso.mobile.android.sec2.mwadapter.util.MwAdapterPreferenceManager;
import de.adesso.mobile.android.sec2.mwadapter.util.PreferenceKeys;
import de.adesso.mobile.android.sec2.mwapdater.R;

/**
 * This activity shows a preference screen where the user can set the
 * preferences for the connection to the Sec2-middleware-instance and to the
 * cloud-storage-service.
 * 
 * @author nike
 */
public class MwAdapterPreferenceActivity extends PreferenceActivity
{
    private static final int POPUP_SUCCESS = 0;
    private static final int POPUP_NO_SUCCESS_ERROR = 1;
    private static final int POPUP_NO_SUCCESS_NO_KEY = 2;
    private static final int POPUP_NO_SUCCESS_NO_COMMIT = 3;

    private String errorMessage = null;
    private MwAdapterPreferenceManager preferenceManager = null;

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        preferenceManager = new MwAdapterPreferenceManager(getApplicationContext());

        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        setContentView(R.layout.preference);
        addPreferencesFromResource(R.xml.preference_ressource);

        final PortRangeValidator portRangeValidator = new PortRangeValidator();
        ValidatedEditTextPreference editText =
                (ValidatedEditTextPreference)(getPreferenceScreen().findPreference(PreferenceKeys.KEY_MIDDLEWARE_PORT));
        editText.setValidator(portRangeValidator);
        editText = (ValidatedEditTextPreference)(getPreferenceScreen().findPreference(PreferenceKeys.KEY_CLOUD_HOST));
        editText.setValidator(new HostNameValidator());
        editText = (ValidatedEditTextPreference)(getPreferenceScreen().findPreference(PreferenceKeys.KEY_CLOUD_PORT));
        editText.setValidator(portRangeValidator);
        editText = (ValidatedEditTextPreference)(getPreferenceScreen().findPreference(PreferenceKeys.KEY_CLOUD_LOCATION));
        editText.setValidator(new PathValidator());

        final TextView status = (TextView)findViewById(R.id.register_status_flag_lbl);
        final String key = preferenceManager.getAppAuthKey();
        if(key != null && !key.isEmpty())
        {
            status.setTextColor(Color.GRAY);
            status.setText(R.string.status_registered);
        }
        else
        {
            status.setTextColor(Color.RED);
            status.setText(R.string.status_not_registered);
        }
    }

    /**
     * This method is executed when the "register"-button is pressed. It starts
     * the registration-process of this app with the Sec2-middleware.
     * 
     * @param view - The view
     */
    public void register(final View view)
    {
        new RegisterTask().execute();
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            final Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected Dialog onCreateDialog(final int id)
    {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(R.string.register_success_title);
        switch(id)
        {
            case POPUP_SUCCESS:
                alertDialogBuilder.setMessage(R.string.register_success_true);
                break;
            case POPUP_NO_SUCCESS_ERROR:
                alertDialogBuilder.setMessage(getString(R.string.register_success_false_error) + errorMessage);
                break;
            case POPUP_NO_SUCCESS_NO_KEY:
                alertDialogBuilder.setMessage(R.string.register_success_false_key);
                break;
            case POPUP_NO_SUCCESS_NO_COMMIT: alertDialogBuilder.setMessage(R.string.register_success_false_commit);
        }
        alertDialogBuilder.setNeutralButton(R.string.ok, null);
        alertDialogBuilder.setCancelable(false);

        return alertDialogBuilder.create();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        finish();
    }

    private final class RegisterTask extends AsyncTask<Void, Void, Object>
    {
        @Override
        protected Object doInBackground(final Void... params)
        {
            final MwAdapter adapter = MwAdapter.getInstance();

            try
            {
                return adapter.register(getApplication().getPackageName(), preferenceManager.getMiddlewarePort());
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
                LogHelper.logE(e.getMessage());

                return e;
            }
        }

        @Override
        protected void onPostExecute(final Object result)
        {
            TextView status = null;

            if (result instanceof Exception)
            {
                showDialog(POPUP_NO_SUCCESS_ERROR);
            }
            else if (result != null && result instanceof AppAuthKey)
            {
                if (preferenceManager.saveAppAuthKey((AppAuthKey) result))
                {
                    status = (TextView) findViewById(R.id.register_status_flag_lbl);
                    status.setTextColor(Color.GRAY);
                    status.setText(MwAdapterPreferenceActivity.this.getText(R.string.status_registered));
                    showDialog(POPUP_SUCCESS);
                }
                else
                {
                    showDialog(POPUP_NO_SUCCESS_NO_COMMIT);
                }
            }
            else
            {
                showDialog(POPUP_NO_SUCCESS_NO_KEY);
            }
        };
    }
}

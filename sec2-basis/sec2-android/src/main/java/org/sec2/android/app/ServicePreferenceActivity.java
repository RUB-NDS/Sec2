package org.sec2.android.app;

import org.sec2.android.ISec2MiddlewareService;
import org.sec2.android.IServiceConnectionListener;
import org.sec2.android.Sec2MiddlewareServiceImpl;
import org.sec2.android.Sec2MiddlewareServiceImpl.Sec2MiddlewareServiceConnection;
import org.sec2.android.app.component.IPasswordChangedListener;
import org.sec2.android.app.component.LoginPasswordEditTextPreference;
import org.sec2.android.model.SessionKey;
import org.sec2.android.util.Constants;
import org.sec2.android.util.PreferenceKeys;
import org.sec2.android.util.Sec2MiddlewarePreferenceManager;
import org.sec2.middleware.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import de.adesso.mobile.android.sec2.mwadapter.gui.components.PortRangeValidator;
import de.adesso.mobile.android.sec2.mwadapter.gui.components.ValidatedEditTextPreference;
import de.adesso.mobile.android.sec2.mwadapter.logging.LogHelper;

/**
 * This activity displays the preferences-screen. On this screen the user can
 * control the Sec2-middleware. Furthermore, the user can navigate to the
 * screens for group-management, for user-management and for app-management.
 *
 * @author nike
 */
public class ServicePreferenceActivity extends PreferenceActivity
implements IServiceConnectionListener, IPasswordChangedListener
{
    private static final int POPUP_BIND_ERROR = 0;
    private static final int POPUP_SERVICE_COM_ERROR = 1;
    private static final int POPUP_SERVER_START_ERROR = 2;
    private static final int POPUP_SERVER_STOP_ERROR = 3;
    private static final int POPUP_USER_MANAGEMENT_ERROR = 4;
    private static final int POPUP_GROUP_MANAGEMENT_ERROR = 5;
    private static final Class<?> CLAZZ = ServicePreferenceActivity.class;

    private String errorMessage = "";
    private String statusMessage = null;
    private String statusStarted = null;
    private String statusStopped = null;
    private String statusUnknown = null;
    private String loginPw = null;
    private Sec2MiddlewareServiceConnection connection = null;
    private Button buttonStart = null;
    private Button buttonStop = null;
    private TextView statusView = null;
    private boolean sessionKeyFetched = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        setContentView(R.layout.service_preference);
        addPreferencesFromResource(R.xml.service_preference_res);

        buttonStart = (Button)findViewById(R.id.start_btn);
        buttonStop = (Button)findViewById(R.id.stop_btn);
        statusMessage = getString(R.string.service_conf_status) + " ";
        statusStarted = getString(R.string.service_conf_status_started);
        statusStopped = getString(R.string.service_conf_status_stopped);
        statusUnknown = getString(R.string.service_conf_status_unknown);
        loginPw = getIntent().getStringExtra(Constants.INTENT_EXTRA_LOGIN_PW);
        final LoginPasswordEditTextPreference lpetp =
                (LoginPasswordEditTextPreference)(getPreferenceScreen()
                        .findPreference(PreferenceKeys.KEY_LOGIN_PW));
        lpetp.setLoginPassword(loginPw);
        lpetp.setPasswordChangedListener(this);
        final ValidatedEditTextPreference vetp =
                (ValidatedEditTextPreference)(getPreferenceScreen()
                        .findPreference(PreferenceKeys.KEY_SERVER_PORT));
        vetp.setValidator(new PortRangeValidator());
        statusView = (TextView)findViewById(R.id.status_info_lbl);
        //Set listeners for some preferences
        Preference preference = findPreference("link_user_management");
        preference.setOnPreferenceClickListener(
                new OnUserManagementPreferenceClickListener());
        preference = findPreference("link_group_management");
        preference.setOnPreferenceClickListener(
                new OnGroupManagementPreferenceClickListener());
        preference = findPreference("link_app_management");
        preference.setOnPreferenceClickListener(
                new OnAppManagementPreferenceClickListener());

        connectToService();
    }

    @Override
    public void onPasswordChanged(final String newPassword)
    {
        final ISec2MiddlewareService service = connection.getService();

        loginPw = newPassword;
        try
        {
            if (service != null)
            {
                if (service.isRunning())
                {
                    service.useDbKey(newPassword);
                }
            }
            else
            {
                showDialog(POPUP_SERVICE_COM_ERROR);
            }
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
            showDialog(POPUP_SERVICE_COM_ERROR);
            LogHelper.logE(CLAZZ, re.getMessage());
        }
    }

    @Override
    public void onServiceConnected()
    {
        final ISec2MiddlewareService service = connection.getService();

        try
        {
            if (service != null)
            {
                ServiceMethodProvider.setService(service);
                if (service.isRunning())
                {
                    statusView.setText(statusMessage + statusStarted);
                    buttonStart.setEnabled(false);
                    buttonStop.setEnabled(true);
                }
                else
                {
                    statusView.setText(statusMessage + statusStopped);
                    buttonStart.setEnabled(true);
                    buttonStop.setEnabled(false);
                }
            }
            else
            {
                showDialog(POPUP_SERVICE_COM_ERROR);
            }
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
            showDialog(POPUP_SERVICE_COM_ERROR);
            LogHelper.logE(CLAZZ, re.getMessage());
        }
    }

    @Override
    public void onServiceDisconnected()
    {
        //Nothing to do here
    }

    /**
     * This method is called when the start-button, defined in
     * service_preference.xml, was pressed. The method starts then the
     * Sec2-middleware-service.
     *
     * @param view - The view
     */
    public void startServer(final View view)
    {
        final ISec2MiddlewareService service = connection.getService();

        try
        {
            if (service != null)
            {
                service.startServer(loginPw);
                if (service.isRunning())
                {
                    statusView.setText(statusMessage + statusStarted);
                    buttonStart.setEnabled(false);
                    buttonStop.setEnabled(true);
                }
                else
                {
                    showDialog(POPUP_SERVER_START_ERROR);
                }
            }
            else
            {
                showDialog(POPUP_SERVICE_COM_ERROR);
            }
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
            showDialog(POPUP_SERVER_START_ERROR);
            LogHelper.logE(CLAZZ, re.getMessage());
        }
    }

    /**
     * This method is called when the stop-button, defined in
     * service_preference.xml, was pressed. The method stops then the
     * Sec2-middleware-service.
     *
     * @param view - The view
     */
    public void stopServer(final View view)
    {
        final ISec2MiddlewareService service = connection.getService();

        try
        {
            if (service != null)
            {
                service.stopServer();
                if (!service.isRunning())
                {
                    statusView.setText(statusMessage + statusStopped);
                    buttonStart.setEnabled(true);
                    buttonStop.setEnabled(false);
                }
                else
                {
                    showDialog(POPUP_SERVER_STOP_ERROR);
                }
            }
            else
            {
                showDialog(POPUP_SERVICE_COM_ERROR);
            }
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
            showDialog(POPUP_SERVER_STOP_ERROR);
            LogHelper.logE(CLAZZ, re.getMessage());
        }
    }

    @Override
    protected Dialog onCreateDialog(final int id)
    {
        final AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(R.string.service_conf_popup_title);
        switch(id)
        {
            case POPUP_BIND_ERROR:
                alertDialogBuilder.setMessage(
                        R.string.service_conf_popup_bind_error);
                break;
            case POPUP_SERVICE_COM_ERROR:
                alertDialogBuilder.setMessage(getString(
                        R.string.service_conf_popup_service_com_error) + " "
                        + errorMessage);
                break;
            case POPUP_SERVER_START_ERROR:
                alertDialogBuilder.setMessage(getString(
                        R.string.service_conf_popup_server_start_error) + " "
                        + errorMessage);
                break;
            case POPUP_SERVER_STOP_ERROR:
                alertDialogBuilder.setMessage(getString(
                        R.string.service_conf_popup_server_stop_error) + " "
                        + errorMessage);
                break;
            case POPUP_USER_MANAGEMENT_ERROR:
                alertDialogBuilder.setMessage(getString(
                        R.string.service_conf_popup_user_management_error)
                        + " " + errorMessage);
                break;
            case POPUP_GROUP_MANAGEMENT_ERROR:
                alertDialogBuilder.setMessage(getString(
                        R.string.service_conf_popup_group_management_error) + " "
                        + errorMessage);
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

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        disconnectFromService();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState)
    {
        disconnectFromService();
    }

    private void connectToService()
    {
        final Intent intent =
                new Intent(Sec2MiddlewareServiceImpl.class.getName());

        intent.putExtra(Constants.INTENT_EXTRA_DB_KEY, loginPw);
        connection = new Sec2MiddlewareServiceConnection(this);
        if (!bindService(intent, connection, Context.BIND_AUTO_CREATE))
        {
            statusView.setText(statusMessage + statusUnknown);
            buttonStart.setEnabled(true);
            buttonStop.setEnabled(true);
            showDialog(POPUP_BIND_ERROR);
        }
    }

    private void disconnectFromService()
    {
        final ISec2MiddlewareService service = connection.getService();
        Intent intent = null;

        try
        {
            if (service != null)
            {
                intent = new Intent(Sec2MiddlewareServiceImpl.class.getName());
                //If service is running, then keep it running
                if (service.isRunning())
                {
                    intent.putExtra(Constants.INTENT_EXTRA_DB_KEY, loginPw);
                    startService(intent);
                }
                //Else stop the service
                else
                {
                    stopService(intent);
                }
            }
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
            showDialog(POPUP_SERVICE_COM_ERROR);
            LogHelper.logE(CLAZZ, re.getMessage());
        }
        try
        {
            unbindService(connection);
        }
        catch (final IllegalArgumentException iae)
        {
            LogHelper.logD(CLAZZ, iae.getMessage());
        }
    }

    private final class OnUserManagementPreferenceClickListener
    implements OnPreferenceClickListener
    {
        @Override
        public boolean onPreferenceClick(final Preference preference)
        {
            final ISec2MiddlewareService service = connection.getService();
            final Sec2MiddlewarePreferenceManager prefManager =
                    new Sec2MiddlewarePreferenceManager(
                            getApplicationContext());
            SessionKey sessionKey = null;
            Intent intent = null;
            boolean sessionSuccessfulFetched = true;

            try
            {
                if (service != null && service.isRunning())
                {
                    //If no session key was fetched before, fetch a new session
                    //key from the Sec2-middleware-server
                    if (!sessionKeyFetched)
                    {
                        sessionKey = service.getSessionKey(
                                prefManager.getSessionToken());
                        if (prefManager.saveSessionKey(sessionKey))
                        {
                            sessionKeyFetched = true;
                        }
                        else
                        {
                            sessionSuccessfulFetched = false;
                            errorMessage = getString(R.string.service_conf_popup_user_management_error_sk);
                            showDialog(POPUP_USER_MANAGEMENT_ERROR);
                        }
                    }
                    if (sessionSuccessfulFetched)
                    {
                        intent = new Intent(ServicePreferenceActivity.this,
                                UserManagementEntryActivity.class);
                        intent.putExtra(Constants.INTENT_EXTRA_LOGIN_PW,
                                loginPw);
                        startActivity(intent);
                    }
                }
                else
                {
                    errorMessage = getString(
                            R.string
                            .service_conf_popup_user_management_error_server);
                    showDialog(POPUP_USER_MANAGEMENT_ERROR);
                }
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
                showDialog(POPUP_USER_MANAGEMENT_ERROR);
                LogHelper.logE(CLAZZ, re.getMessage());
            }

            return true;
        }
    }

    private final class OnGroupManagementPreferenceClickListener
    implements OnPreferenceClickListener
    {
        @Override
        public boolean onPreferenceClick(final Preference preference)
        {
            final ISec2MiddlewareService service = connection.getService();
            final Sec2MiddlewarePreferenceManager prefManager =
                    new Sec2MiddlewarePreferenceManager(
                            getApplicationContext());
            SessionKey sessionKey = null;
            Intent intent = null;
            boolean sessionSuccessfulFetched = true;

            try
            {
                if (service != null && service.isRunning())
                {
                    //If no session key was fetched before, fetch a new session
                    //key from the Sec2-middleware-server
                    if (!sessionKeyFetched)
                    {
                        sessionKey = service.getSessionKey(
                                prefManager.getSessionToken());
                        if (prefManager.saveSessionKey(sessionKey))
                        {
                            sessionKeyFetched = true;
                        }
                        else
                        {
                            sessionSuccessfulFetched = false;
                            errorMessage = getString(R.string.service_conf_popup_group_management_error_sk);
                            showDialog(POPUP_GROUP_MANAGEMENT_ERROR);
                        }
                    }
                    if (sessionSuccessfulFetched)
                    {
                        intent = new Intent(ServicePreferenceActivity.this,
                                GroupManagementEntryActivity.class);
                        intent.putExtra(Constants.INTENT_EXTRA_LOGIN_PW,
                                loginPw);
                        startActivity(intent);
                    }
                }
                else
                {
                    errorMessage = getString(
                            R.string
                            .service_conf_popup_group_management_error_server);
                    showDialog(POPUP_GROUP_MANAGEMENT_ERROR);
                }
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
                showDialog(POPUP_GROUP_MANAGEMENT_ERROR);
                LogHelper.logE(CLAZZ, re.getMessage());
            }

            return true;
        }
    }

    private final class OnAppManagementPreferenceClickListener
    implements OnPreferenceClickListener
    {
        @Override
        public boolean onPreferenceClick(final Preference preference)
        {
            final Intent intent = new Intent(ServicePreferenceActivity.this,
                    AppsManagementActivity.class);

            intent.putExtra(Constants.INTENT_EXTRA_LOGIN_PW, loginPw);
            startActivity(intent);

            return true;
        }
    }
}

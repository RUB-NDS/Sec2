package de.adesso.mobile.android.sec2;

import java.text.MessageFormat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import de.adesso.mobile.android.sec2.activity.Sec2Activity;
import de.adesso.mobile.android.sec2.app.Sec2Application;
import de.adesso.mobile.android.sec2.util.Constants;
import de.adesso.mobile.android.sec2.util.CryptoUtils;
import de.adesso.mobile.android.sec2.util.LogHelper;

/**
 * LoginActivity
 * @author nike
 */
public class LoginActivity extends Sec2Activity {

    private static final Class<?> c = LoginActivity.class;
    private static final int PW_VALIDATION_ERROR = 0;
    private static final int PW_WRONG_ERROR = 1;

    private String errorMessage = null;
    @SuppressWarnings ("unused")
    private Sec2Application app;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        setContentView(R.layout.login);

        app = ((Sec2Application) getApplication());

    }

    public void login(final View v) {
        LogHelper.logV(c, "login()");

        final EditText editText = (EditText) findViewById(R.id.txtPin);
        final String typedPw = editText.getText().toString();

        try {
            if (CryptoUtils.checkLoginPw(
                    typedPw,
                    PreferenceManager.getDefaultSharedPreferences(this).getString(
                            Constants.PREF_KEY_LOGIN, ""))) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                showDialog(PW_WRONG_ERROR);
            }
        } catch (final Exception e) {
            if (e.getMessage() != null) {
                errorMessage = e.getMessage();
            } else {
                errorMessage = "";
            }
            showDialog(PW_VALIDATION_ERROR);
            LogHelper.logE(e.getMessage());
        }
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(R.string.login_pw_validation_error_title);

        switch (id) {
            case PW_VALIDATION_ERROR:
                alertDialogBuilder.setMessage(MessageFormat.format(
                        getString(R.string.login_pw_validation_error), errorMessage));
                break;
            case PW_WRONG_ERROR:
                alertDialogBuilder.setMessage(R.string.login_pw_wrong_error);
        }
        alertDialogBuilder.setNeutralButton(R.string.ok, null);
        alertDialogBuilder.setCancelable(false);

        return alertDialogBuilder.create();
    }
}

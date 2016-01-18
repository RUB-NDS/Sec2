package org.sec2.android.app;

import java.text.MessageFormat;

import org.sec2.android.util.Constants;
import org.sec2.android.util.CryptoUtils;
import org.sec2.android.util.Sec2MiddlewarePreferenceManager;
import org.sec2.middleware.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import de.adesso.mobile.android.sec2.mwadapter.logging.LogHelper;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Security;
import org.sec2.securityprovider.mobileclient.MobileClientProvider;
import org.sec2.securityprovider.serviceparameter.TokenType;
import org.sec2.token.TokenConstants;
import org.spongycastle.jce.provider.BouncyCastleProvider;

/**
 * This activity shows the login-screen of the app.
 *
 * @author schuessler
 */
public class LoginActivity extends Activity {

    private static final Class<?> CLAZZ = LoginActivity.class;
    private static final int POPUP_PW_VALIDATION_ERROR = 0;
    private static final int POPUP_PW_WRONG_ERROR = 1;
    private String errorMessage = null;
   
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        setContentView(R.layout.login);

        LogHelper.logV(CLAZZ, "onCreate");
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    /**
     * This method is called, when the login-button is pressed.
     *
     * TODO change this to pin entry???
     *
     * @param v - The view
     */
    public void login(final View v) {
        LogHelper.logV(CLAZZ, "login()");
        final EditText editText = (EditText) findViewById(R.id.password);
        final String typedPw = editText.getText().toString();
        final Sec2MiddlewarePreferenceManager prefManager =
                new Sec2MiddlewarePreferenceManager(this);
        final CryptoUtils cryptoUtils = new CryptoUtils();
        Intent intent = null;

        try {
            // TODO handle MobileClient initialization here
            try {
                Security.insertProviderAt(MobileClientProvider.getInstance(), 1);
            } catch (IllegalStateException e) {
//                MobileClientProvider.setType(TokenType.SOFTWARE_TEST_TOKEN_USER_1);
                Security.insertProviderAt(MobileClientProvider.getInstance(TokenConstants.DEFAULT_PIN), 1);
            }
            Security.addProvider(new BouncyCastleProvider()); //for AES-GCM

//            // TODO zusaetzliche pin checks
//            byte[] pin = typedPw.getBytes();
//            for (int i = 0; i < pin.length; i++) {
//                pin[i] -= 0x30;
//            }
//
//            MobileClientProvider.getInstance(pin);



            if (cryptoUtils.checkLoginPw(
                    typedPw, prefManager.getHashedLoginPassword())) {
                intent = new Intent(this, ServicePreferenceActivity.class);
                intent.putExtra(Constants.INTENT_EXTRA_LOGIN_PW, typedPw);

                startActivity(intent);
                //Activity will be finished now via the onStop()-method
            } else {
                showDialog(POPUP_PW_WRONG_ERROR);
            }
        } catch (final Exception e) {
            if (e.getMessage() != null) {
                errorMessage = e.getMessage();
            } else {
                errorMessage = "";
            }
            showDialog(POPUP_PW_VALIDATION_ERROR);
            LogHelper.logE(e.getMessage());
        }
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        final AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(R.string.login_pw_validation_error_title);

        switch (id) {
            case POPUP_PW_VALIDATION_ERROR:
                alertDialogBuilder.setMessage(MessageFormat.format(getString(
                        R.string.login_pw_validation_error), errorMessage));
                break;
            case POPUP_PW_WRONG_ERROR:
                alertDialogBuilder.setMessage(R.string.login_pw_wrong_error);
        }
        alertDialogBuilder.setNeutralButton(R.string.ok, null);
        alertDialogBuilder.setCancelable(false);

        return alertDialogBuilder.create();
    }
}

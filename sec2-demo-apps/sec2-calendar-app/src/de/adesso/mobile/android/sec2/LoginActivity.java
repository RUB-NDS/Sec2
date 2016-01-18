package de.adesso.mobile.android.sec2;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import de.adesso.mobile.android.sec2.activity.Sec2Activity;
import de.adesso.mobile.android.sec2.app.Sec2Application;
import de.adesso.mobile.android.sec2.util.LogHelper;

/**
 * LoginActivity
 * @author hoppe
 */
public class LoginActivity extends Sec2Activity {

    private static final Class<?> c = LoginActivity.class;

    @SuppressWarnings ("unused")
    private Sec2Application app;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        setContentView(R.layout.login);

        app = ((Sec2Application) getApplication());

        LogHelper.logV(c, "onCreate");

    }

    public void login(View v) {
        LogHelper.logV(c, "login()");

        Intent intent = new Intent();
        intent.setClass(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }

}

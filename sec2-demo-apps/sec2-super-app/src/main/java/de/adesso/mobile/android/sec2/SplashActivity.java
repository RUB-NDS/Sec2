package de.adesso.mobile.android.sec2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import de.adesso.mobile.android.sec2.app.Sec2Application;

/**
 * SplashActivity
 * @author hoppe
 */
public class SplashActivity extends Activity {

    private static final int STOPSPLASH = 0;
    private static final int WAIT = 1000;

    @SuppressWarnings ("unused")
    private Sec2Application mApp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        setContentView(R.layout.splash);
        mApp = (Sec2Application) getApplication();
        Message msg = new Message();
        msg.what = STOPSPLASH;
        mHandler.sendMessageDelayed(msg, WAIT);
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == STOPSPLASH) {
                Intent intent = new Intent();
                intent.setClass(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        }
    };
}

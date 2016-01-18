package de.adesso.mobile.android.sec2.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import de.adesso.mobile.android.sec2.R;

/**
 * WaitingDialog
 * @author mschmitz
 */
public class WaitingDialog extends Dialog {

    private final OnCancelListener listener;
    private TextView tvMessage;
    private String tvMessageBackup;

    /**
     * Constructor
     */
    public WaitingDialog(final Context context, final OnCancelListener listener) {
        super(context);
        this.listener = listener;
    }

    /**
     * onCreate
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        getContext().setTheme(R.style.WaitingDialogTheme);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.dialog_waiting);
        initAnimations();

        tvMessage = (TextView) findViewById(R.id.wd_message);
        if (tvMessageBackup != null) {
            setMessage(tvMessageBackup);
        }
    }

    /**
     * setMessage
     */
    public void setMessage(final String msg) {
        if (tvMessage != null) {
            tvMessage.setText(msg);
        } else {
            tvMessageBackup = msg;
        }
    }

    /**
     * getMessage
     */
    public String getMessage() {
        if (tvMessage != null) {
            return tvMessage.getText().toString();
        } else {
            return tvMessageBackup;
        }

    }

    /**
     * initAnimations3
     */
    private void initAnimations() {
        final Animation outer = AnimationUtils.loadAnimation(getContext(), R.anim.outer);
        final Animation inner = AnimationUtils.loadAnimation(getContext(), R.anim.inner);
        findViewById(R.id.wd_outer).startAnimation(outer);
        findViewById(R.id.wd_inner).startAnimation(inner);
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            // fire our own listener
            if (listener != null) {
                listener.onCancel(this);
            }

            // hide the dialog
            this.hide();
            this.dismiss();

            // we handled this event
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * OnCancelListener
     * @author mschmitz
     */
    public interface OnCancelListener {

        void onCancel(WaitingDialog dialog);
    }

}

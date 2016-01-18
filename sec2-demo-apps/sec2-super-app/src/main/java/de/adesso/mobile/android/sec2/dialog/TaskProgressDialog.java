
package de.adesso.mobile.android.sec2.dialog;

import android.content.Context;
import de.adesso.mobile.android.sec2.util.IDialog;

/**
 * Utility class to create update and dismiss a dialog while loading data from exist database
 * 
 * @author hoppe
 *
 */
public final class TaskProgressDialog implements IDialog {

    private final Context mContext;
    private WaitingDialog mDialog;

    /**
     * Contructor to create an object of TaskProgressDialog
     * @param context Context in which a Waiting Dialog will be created
     */
    public TaskProgressDialog(final Context context) {
        mContext = context;
    }

    /*
     * (non-Javadoc)
     * @see de.adesso.mobile.android.sec2.util.IDialog#start(java.lang.String)
     */
    @Override
    public void start(final String message) {
        mDialog = new WaitingDialog(mContext, null);
        mDialog.setMessage(message);
        mDialog.show();
    }

    /*
     * (non-Javadoc)
     * @see de.adesso.mobile.android.sec2.util.IDialog#update(java.lang.String)
     */
    @Override
    public void update(final String message) {
        mDialog.setMessage(message);
    }

    /*
     * (non-Javadoc)
     * @see de.adesso.mobile.android.sec2.util.IDialog#stop()
     */
    @Override
    public void stop() {
        mDialog.hide();
        mDialog.dismiss();
    }

}

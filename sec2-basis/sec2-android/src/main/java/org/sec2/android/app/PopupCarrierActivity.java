package org.sec2.android.app;

import java.text.MessageFormat;

import org.sec2.android.servers.rest.PopupResultWrapper;
import org.sec2.android.util.Constants;
import org.sec2.android.util.LockObjectHandler;
import org.sec2.middleware.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

/**
 * This activity acts as a "carrier"-activity to show a popup, initiated by a
 * service. This is necessary because otherwise it isn't possible to show a
 * popup.
 *
 * @author schuessler
 */
public class PopupCarrierActivity extends Activity
{
    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        showDialog(0);
    }

    @Override
    protected Dialog onCreateDialog(final int id)
    {
        Intent callingIntent = getIntent();
        String lockObjectId = callingIntent.getStringExtra(
                Constants.INTENT_EXTRA_LOCK_OBJ_ID);
        final AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(R.string.popup_carrier_popup_title);
        alertDialogBuilder.setMessage(MessageFormat.format(getString(
                R.string.popup_carrier_popup_text),
                callingIntent.getStringExtra(
                        Constants.INTENT_EXTRA_APP_NAME)));
        alertDialogBuilder.setPositiveButton(R.string.yes,
                new OnYesOrNoClickListener(this, lockObjectId, true));
        alertDialogBuilder.setNegativeButton(R.string.no,
                new OnYesOrNoClickListener(this, lockObjectId, false));
        alertDialogBuilder.setCancelable(false);

        return alertDialogBuilder.create();
    }

    private final class OnYesOrNoClickListener
    implements DialogInterface.OnClickListener
    {
        private final String lockObjectId;
        private final PopupCarrierActivity activity;
        private final boolean yesClicked;

        public OnYesOrNoClickListener(final PopupCarrierActivity activity,
                final String lockObjectId, final boolean yesClicked)
        {
            this.activity = activity;
            this.lockObjectId = lockObjectId;
            this.yesClicked = yesClicked;
        }

        @Override
        public void onClick(final DialogInterface dialog, final int which)
        {
            Object lockObject = null;

            if (lockObjectId != null)
            {
                lockObject = LockObjectHandler.getLockObject(lockObjectId);
            }
            if (lockObject != null)
            {
                if (lockObject instanceof PopupResultWrapper)
                {
                    ((PopupResultWrapper)lockObject).setYesClicked(yesClicked);
                }
                synchronized (lockObject)
                {
                    lockObject.notifyAll();
                }
            }
            if (activity != null)
            {
                activity.finish();
            }
        }
    }
}

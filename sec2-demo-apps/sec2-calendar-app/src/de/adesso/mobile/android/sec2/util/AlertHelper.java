package de.adesso.mobile.android.sec2.util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.provider.Settings;
import de.adesso.mobile.android.sec2.R;

/**
 * Wrapper for AlertDialogs. Offers intents to "Wireless Settings" and "Security Settings".
 * @author mschmitz
 */
public abstract class AlertHelper {

    @SuppressWarnings ("unused")
    private static final Class<?> c = AlertHelper.class;

    /**
     * showAlertDialog
     */
    public static void showAlertDialog(final Context context, final String title, final String message) {
        Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.show();
    }

    /**
     * startWirelessSettings
     */
    public static void startWirelessSettings(final Context context) {
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        context.startActivity(intent);
    }

    /**
     * showWirelessDialog
     */
    public static void showWirelessDialog(final Context context, final OnClickListener cancelListener) {
        Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.wireless_title);
        builder.setMessage(R.string.wireless_message);
        builder.setPositiveButton(R.string.adjustments, new OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                startWirelessSettings(context);
            }
        });

        builder.setNegativeButton(android.R.string.cancel, cancelListener);
        builder.show();
    }

}

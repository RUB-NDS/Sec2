package org.sec2.android.servers.rest;

import java.util.GregorianCalendar;

import org.sec2.android.app.PopupCarrierActivity;
import org.sec2.android.util.Constants;
import org.sec2.android.util.CryptoUtils;
import org.sec2.android.util.LockObjectHandler;
import org.sec2.mwserver.core.rest.AbstractRestFunctionExecutor;
import org.sec2.mwserver.core.util.ICryptoUtils;

import android.content.Context;
import android.content.Intent;
import de.adesso.mobile.android.sec2.mwadapter.logging.LogHelper;

/**
 * This class implements the abstract methods of class
 * "AbstractRestFunctionExecutor" for Android-devices. The method
 * "allowsAppToRegister" shows a popup, asking the user, if he wants to allow
 * an App to register at the Sec2-middleware.
 *
 * @author schuessler
 */
public final class RestFunctionExecutor extends AbstractRestFunctionExecutor
{
    private static final Class<?> CLAZZ = RestFunctionExecutor.class;

    private final Context context;
    private ICryptoUtils cryptoUtils = null;

    /**
     * The constructor for this class.
     *
     * @param context - The context
     */
    public RestFunctionExecutor(final Context context)
    {
        this.context = context;
    }

    @Override
    protected boolean allowAppToRegister(final String appName)
    {
        Intent intent = null;
        PopupResultWrapper popupResult = null;
        long id;

        if (appName != null && !appName.isEmpty())
        {
            try
            {
                //Erstellen des Lock-Objektes
                popupResult = new PopupResultWrapper();
                id = (new GregorianCalendar()).getTimeInMillis();
                //Erhöhe ID um eines, bis Lock-Objekt im Lock-Objekt-Handler
                //gespeichert werden konnte
                while (!LockObjectHandler.setLockObject(Long.toHexString(id),
                        popupResult))
                {
                    id++;
                }

                //Erstelle Intent und starte Activity
                intent = new Intent(context, PopupCarrierActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Constants.INTENT_EXTRA_LOCK_OBJ_ID,
                        Long.toHexString(id));
                intent.putExtra(Constants.INTENT_EXTRA_APP_NAME, appName);
                context.startActivity(intent);

                //Warte auf das Ergebnis
                synchronized (popupResult)
                {
                    popupResult.wait();
                }
                return popupResult.isYesClicked();
            }
            catch (final InterruptedException ie)
            {
                LogHelper.logE(CLAZZ, ie);
                return false;
            }
        }
        else
        {
            LogHelper.logD(CLAZZ, "The extra-field \"appName\" of the intent "
                    + "must not be empty!");
            return false;
        }
    }

    @Override
    protected ICryptoUtils getCryptoUtils()
    {
        if (cryptoUtils == null)
        {
            cryptoUtils = new CryptoUtils();
        }

        return cryptoUtils;
    }
}

package de.adesso.mobile.android.sec2.util;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import de.adesso.mobile.android.sec2.debug.Debug;

/**
 * Wrapper for logging and toasting.
 * @author mschmitz
 */
public class LogHelper {

    public static final String TAG = "Sec2";

    /**
     * logVerbose
     */
    public static void logV(final String msg) {
        Log.v(TAG, msg);
    }

    public static void logV(final Object obj, final String message) {
        logV(obj.getClass().getSimpleName() + " - " + message);
    }

    public static void logV(final Class<?> clazz, final String message) {
        logV(clazz.getSimpleName() + " - " + message);
    }

    public static void logV(final Object obj, final List<?> list) {
        logV(obj.getClass().getSimpleName() + " - " + listToString(list));
    }

    /**
     * logDebug
     */
    public static void logD(final String msg) {
        Log.d(TAG, msg);
    }

    public static void logD(final Object obj, final String message) {
        logD(obj.getClass().getSimpleName() + " - " + message);
    }

    public static void logD(final Class<?> clazz, final String message) {
        logD(clazz.getSimpleName() + " - " + message);
    }

    public static void logD(final Object obj, final List<?> list) {
        logD(obj.getClass().getSimpleName() + " - " + listToString(list));
    }

    /**
     * logInfo
     */
    public static void logI(final String msg) {
        Log.i(TAG, msg);
    }

    public static void logI(final Object obj, final String message) {
        logI(obj.getClass().getSimpleName() + " - " + message);
    }

    public static void logI(final Class<?> clazz, final String message) {
        logI(clazz.getSimpleName() + " - " + message);
    }

    public static void logI(final Object obj, final List<?> list) {
        logI(obj.getClass().getSimpleName() + " - " + listToString(list));
    }

    /**
     * logWarn
     */
    public static void logW(final String msg) {
        Log.w(TAG, msg);
    }

    public static void logW(final Object obj, final String message) {
        logW(obj.getClass().getSimpleName() + " - " + message);
    }

    public static void logW(final Class<?> clazz, final String message) {
        logW(clazz.getSimpleName() + " - " + message);
    }

    public static void logW(final Object obj, final List<?> list) {
        logW(obj.getClass().getSimpleName() + " - " + listToString(list));
    }

    /**
     * logWarn from Exception | Throwable
     */
    public static void logW(final Class<?> clazz, final Throwable tr) {
        logW(clazz.getSimpleName() + " - " + Log.getStackTraceString(tr));
    }

    /**
     * logError
     */
    public static void logE(final String msg) {
        Log.e(TAG, msg);
    }

    public static void logE(final Object obj, final String message) {
        logE(obj.getClass().getSimpleName() + " - " + message);
    }

    public static void logE(final Class<?> clazz, final String message) {
        logE(clazz.getSimpleName() + " - " + message);
    }

    public static void logE(final Object obj, final List<?> list) {
        logE(obj.getClass().getSimpleName() + " - " + listToString(list));
    }

    /**
     * logError from Exception | Throwable
     */
    //    public static void logE(final Throwable tr) {
    //        logE(Log.getStackTraceString(tr));
    //    }

    public static void logE(final Class<?> clazz, final Throwable tr) {
        logE(clazz.getSimpleName() + " - " + Log.getStackTraceString(tr));
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * byteArrayToString
     */
    //    public static String byteArrayToString(final byte[] ba) {
    //
    //        final StringBuffer sb = new StringBuffer();
    //        sb.append("[");
    //
    //        if (ba == null) {
    //            sb.append("NULL");
    //        } else {
    //            for (int i = 0; i < ba.length; i++) {
    //                sb.append(ba[i]);
    //                if (i < ba.length - 1) {
    //                    sb.append(", ");
    //                }
    //            }
    //        }
    //
    //        sb.append("]");
    //        return sb.toString();
    //    }

    /**
     * stringArrayToString
     */
    //    public static String stringArrayToString(final String[] sa) {
    //
    //        if (sa == null) return "[NULL]";
    //
    //        return Arrays.toString(sa);
    //    }

    /**
     * listToString
     */
    public static String listToString(final List<?> list) {

        final StringBuffer sb = new StringBuffer();
        sb.append("[");

        if (list == null) {
            sb.append("NULL!");
        } else {
            for (int i = 0; i < list.size(); i++) {
                sb.append(list.get(i));
                if (i < list.size() - 1) {
                    sb.append(", ");
                }
            }
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * dateToString
     */
    //    public static String dateToString(final Date date) {
    //
    //        if (date == null) return "NULL";
    //
    //        return new SimpleDateFormat("dd.MM.yyyy").format(date.getTime());
    //    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------    

    /**
     * toast
     */
    public static void toast(final Context context, final CharSequence text) {
        if (Debug.TOAST_ENABLED) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            LogHelper.logD(text.toString());
        }
    }

}

package de.adesso.mobile.android.sec2.mwadapter.logging;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Wrapper for logging and toasting.
 *
 * @author mschmitz
 * @author nschuessler
 */
public class LogHelper {

    /**
     * Logging tag.
     */
    public static final String TAG = "Sec2";

    /**
     * logVerbose.
     * 
     * @param msg - The log message
     */
    public static void logV(final String msg) {
        Log.v(TAG, msg);
    }

    /**
     * logVerbose.
     * 
     * @param obj - The object that has called this log-method. The object's
     *  class name is prepended to the log message.
     * @param message - The log message
     */
    public static void logV(final Object obj, final String message) {
        logV(obj.getClass().getSimpleName() + " - " + message);
    }

    /**
     * logVerbose.
     * 
     * @param clazz - The class of the object that has called this log-method.
     * The class name is prepended to the log message.
     * @param message - The log message
     */
    public static void logV(final Class<?> clazz, final String message) {
        logV(clazz.getSimpleName() + " - " + message);
    }

    /**
     * logVerbose.
     * 
     * @param obj - The object that has called this log-method. The object's
     *  class name is prepended to the log message.
     * @param list - List of messages.
     */
    public static void logV(final Object obj, final List<?> list) {
        logV(obj.getClass().getSimpleName() + " - " + listToString(list));
    }

    /**
     * logDebug.
     * 
     * @param msg - The log message
     */
    public static void logD(final String msg) {
        Log.d(TAG, msg);
    }

    /**
     * logDebug.
     * 
     * @param obj - The object that has called this log-method. The object's
     *  class name is prepended to the log message.
     * @param message - The log message
     */
    public static void logD(final Object obj, final String message) {
        logD(obj.getClass().getSimpleName() + " - " + message);
    }

    /**
     * logDebug.
     * 
     * @param clazz - The class of the object that has called this log-method.
     * The class name is prepended to the log message.
     * @param message - The log message
     */
    public static void logD(final Class<?> clazz, final String message) {
        logD(clazz.getSimpleName() + " - " + message);
    }

    /**
     * logDebug.
     * 
     * @param obj - The object that has called this log-method. The object's
     *  class name is prepended to the log message.
     * @param list - List of messages.
     */
    public static void logD(final Object obj, final List<?> list) {
        logD(obj.getClass().getSimpleName() + " - " + listToString(list));
    }

    /**
     * logInfo.
     * 
     * @param msg - The log message
     */
    public static void logI(final String msg) {
        Log.i(TAG, msg);
    }

    /**
     * logInfo.
     * 
     * @param obj - The object that has called this log-method. The object's
     *  class name is prepended to the log message.
     * @param message - The log message
     */
    public static void logI(final Object obj, final String message) {
        logI(obj.getClass().getSimpleName() + " - " + message);
    }

    /**
     * logInfo.
     * 
     * @param clazz - The class of the object that has called this log-method.
     * The class name is prepended to the log message.
     * @param message - The log message
     */
    public static void logI(final Class<?> clazz, final String message) {
        logI(clazz.getSimpleName() + " - " + message);
    }

    /**
     * logInfo.
     * 
     * @param obj - The object that has called this log-method. The object's
     *  class name is prepended to the log message.
     * @param list - List of messages.
     */
    public static void logI(final Object obj, final List<?> list) {
        logI(obj.getClass().getSimpleName() + " - " + listToString(list));
    }

    /**
     * logWarn.
     * 
     * @param msg - The log message
     */
    public static void logW(final String msg) {
        Log.w(TAG, msg);
    }

    /**
     * logWarn.
     * 
     * @param obj - The object that has called this log-method. The object's
     *  class name is prepended to the log message.
     * @param message - The log message
     */
    public static void logW(final Object obj, final String message) {
        logW(obj.getClass().getSimpleName() + " - " + message);
    }

    /**
     * logWarn.
     * 
     * @param clazz - The class of the object that has called this log-method.
     * The class name is prepended to the log message.
     * @param message - The log message
     */
    public static void logW(final Class<?> clazz, final String message) {
        logW(clazz.getSimpleName() + " - " + message);
    }

    /**
     * logWarn.
     * 
     * @param obj - The object that has called this log-method. The object's
     *  class name is prepended to the log message.
     * @param list - List of messages.
     */
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
     * logError.
     * 
     * @param msg - The log message
     */
    public static void logE(final String msg) {
        Log.e(TAG, msg);
    }

    /**
     * logError.
     * 
     * @param obj - The object that has called this log-method. The object's
     *  class name is prepended to the log message.
     * @param message - The log message
     */
    public static void logE(final Object obj, final String message) {
        logE(obj.getClass().getSimpleName() + " - " + message);
    }

    /**
     * logError.
     * 
     * @param clazz - The class of the object that has called this log-method.
     *  The class name is prepended to the log message.
     * @param message - The log message
     */
    public static void logE(final Class<?> clazz, final String message) {
        logE(clazz.getSimpleName() + " - " + message);
    }

    /**
     * logError.
     * 
     * @param obj - The object that has called this log-method. The object's
     *  class name is prepended to the log message.
     * @param list - List of messages.
     */
    public static void logE(final Object obj, final List<?> list) {
        logE(obj.getClass().getSimpleName() + " - " + listToString(list));
    }

    /**
     * logError from Exception | Throwable.
     * 
     * @param clazz - The class of the object that has called this log-method.
     *  The class name is prepended to the log message.
     * @param tr - The throwable
     */
    public static void logE(final Class<?> clazz, final Throwable tr) {
        logE(clazz.getSimpleName() + " - " + Log.getStackTraceString(tr));
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------

    ///**
    // * byteArrayToString
    // */
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

    ///**
    // * stringArrayToString
    // */
    //    public static String stringArrayToString(final String[] sa) {
    //
    //        if (sa == null) return "[NULL]";
    //
    //        return Arrays.toString(sa);
    //    }

    /**
     * listToString. Convert a list to its string representation.
     * 
     * @param list - The list to be converted
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

    ///**
    // * dateToString
    // */
    //    public static String dateToString(final Date date) {
    //
    //        if (date == null) return "NULL";
    //
    //        return new SimpleDateFormat("dd.MM.yyyy").format(date.getTime());
    //    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Generates a toast.
     * 
     * @param context - The context
     * @param text - The text
     */
    public static void toast(final Context context, final CharSequence text) {
        if (Debug.TOAST_ENABLED) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            LogHelper.logD(text.toString());
        }
    }

}

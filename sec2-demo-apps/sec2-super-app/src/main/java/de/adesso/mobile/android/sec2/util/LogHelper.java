
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

    /**
     * String object used for logging
     */
    public static final String TAG = "Sec2";

    /**
     * constructor 
     */
    public LogHelper() {
    }

    /**
     * Method to log on VERBOSE level
     * @param msg the message to log
     */
    public static void logV(final String msg) {
        Log.v(TAG, msg);
    }

    /**
     * Method to log on VERBOSE level
     * @param obj Object used for Tag
     * @param message the message to log
     */
    public static void logV(final Object obj, final String message) {
        logV(obj.getClass().getSimpleName() + " - " + message);
    }

    /**
     * Method to log on VERBOSE level
     * @param clazz Class used for Tag
     * @param message the message to log
     */
    public static void logV(final Class<?> clazz, final String message) {
        logV(clazz.getSimpleName() + " - " + message);
    }

    /**
     * Method to log on VERBOSE level
     * @param obj Object used for Tag
     * @param list  the list to log
     */
    public static void logV(final Object obj, final List<?> list) {
        logV(obj.getClass().getSimpleName() + " - " + listToString(list));
    }

    /**
     * Method to log on DEBUG level
     * @param msg the message to log
     */
    public static void logD(final String msg) {
        Log.d(TAG, msg);
    }

    /**
     * Method to log on DEBUG level
     * @param obj Object used for Tag
     * @param message the message to log
     */
    public static void logD(final Object obj, final String message) {
        logD(obj.getClass().getSimpleName() + " - " + message);
    }

    /**
     * Method to log on DEBUG level
     * @param clazz Class used for Tag
     * @param message the message to log
     */
    public static void logD(final Class<?> clazz, final String message) {
        logD(clazz.getSimpleName() + " - " + message);
    }

    /**
     * Method to log on DEBUG level
     * @param obj Object used for Tag
     * @param list  the list to log
     */
    public static void logD(final Object obj, final List<?> list) {
        logD(obj.getClass().getSimpleName() + " - " + listToString(list));
    }

    /**
     * Method to log on INFORMATION level
     * @param msg the message to log
     */
    public static void logI(final String msg) {
        Log.i(TAG, msg);
    }

    /**
     * Method to log on INFORMATION level
     * @param obj Object used for Tag
     * @param message the message to log
     */
    public static void logI(final Object obj, final String message) {
        logI(obj.getClass().getSimpleName() + " - " + message);
    }

    /**
     * Method to log on INFORMATION level
     * @param clazz Class used for Tag
     * @param message the message to log
     */
    public static void logI(final Class<?> clazz, final String message) {
        logI(clazz.getSimpleName() + " - " + message);
    }

    /**
     * Method to log on INFORMATION level
     * @param obj Object used for Tag
     * @param list  the list to log
     */
    public static void logI(final Object obj, final List<?> list) {
        logI(obj.getClass().getSimpleName() + " - " + listToString(list));
    }

    /**
     * Method to log on WARNING level
     * @param msg the message to log
     */
    public static void logW(final String msg) {
        Log.w(TAG, msg);
    }

    /**
     * Method to log on WARNING level
     * @param obj Object used for Tag
     * @param message the message to log
     */
    public static void logW(final Object obj, final String message) {
        logW(obj.getClass().getSimpleName() + " - " + message);
    }

    /**
     * Method to log on WARNING level
     * @param clazz Class used for Tag
     * @param message the message to log
     */
    public static void logW(final Class<?> clazz, final String message) {
        logW(clazz.getSimpleName() + " - " + message);
    }

    /**
     * Method to log on WARNING level
     * @param obj Object used for Tag
     * @param list  the list to log
     */
    public static void logW(final Object obj, final List<?> list) {
        logW(obj.getClass().getSimpleName() + " - " + listToString(list));
    }

    /**
     * Method to log on WARNING level
     * @param clazz Class used for Tag
     * @param tr Throwable to log
     */
    public static void logW(final Class<?> clazz, final Throwable tr) {
        logW(clazz.getSimpleName() + " - " + Log.getStackTraceString(tr));
    }

    /**
     * Method to log on ERROR level
     * @param msg the message to log
     */
    public static void logE(final String msg) {
        Log.e(TAG, msg);
    }

    /**
     * Method to log on ERROR level
     * @param obj Object used for Tag
     * @param message the message to log
     */
    public static void logE(final Object obj, final String message) {
        logE(obj.getClass().getSimpleName() + " - " + message);
    }

    /**
     * Method to log on ERROR level
     * @param clazz Class used for Tag
     * @param message the message to log
     */
    public static void logE(final Class<?> clazz, final String message) {
        logE(clazz.getSimpleName() + " - " + message);
    }

    /**
     * Method to log on ERROR level
     * @param obj Object used for Tag
     * @param list the list to log
     */
    public static void logE(final Object obj, final List<?> list) {
        logE(obj.getClass().getSimpleName() + " - " + listToString(list));
    }

    /**
     * Method to log on ERROR level
     * @param clazz Class used for Tag
     * @param tr Throwable to log
     */
    public static void logE(final Class<?> clazz, final Throwable tr) {
        logE(clazz.getSimpleName() + " - " + Log.getStackTraceString(tr));
    }

    /**
     * Method to convert a list to String
     * @param list the list to log
     * @return String converted list
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
     * Method to create a Toast
     * @param context Context in which the Toast will be inflated
     * @param text the actual text of the Toast
     */
    public static void toast(final Context context, final CharSequence text) {
        if (Debug.TOAST_ENABLED) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            LogHelper.logD(text.toString());
        }
    }

}

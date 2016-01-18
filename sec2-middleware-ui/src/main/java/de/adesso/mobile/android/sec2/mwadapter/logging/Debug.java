package de.adesso.mobile.android.sec2.mwadapter.logging;

/**
 * This class collects some debug switches, which are used inside some classes.
 * The pro is that we have one location for switching.
 * 
 * @author mschmitz
 */
public abstract class Debug {

    // set this to FALSE and all switches are turned off
    private static final boolean DEBUG = true;

    // for better reading the constants
    private static final boolean OFF = false;

    /**
     * Switch to enable logging. Is TRUE when application is in debug-mode.
     * Otherwise it is FALSE.
     */
    public static final boolean LOG_ENABLED = !DEBUG ? OFF : true;

    /**
     * Switch to enable toasts. Is TRUE when application is in debug-mode.
     * Otherwise it is FALSE.
     */
    public static final boolean TOAST_ENABLED = !DEBUG ? OFF : true;

    /**
     * Switch to load only icons. Is always FALSE.
     */
    public static final boolean JUST_LOADINGICONS_ENABLED = !DEBUG ? OFF : false;

    /**
     * Switch to enable caching of icons. Is always FALSE.
     */
    public static final boolean ICONCACHE_DISABLED = !DEBUG ? OFF : false;

}

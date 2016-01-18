package de.adesso.mobile.android.sec2.util;

/**
 * Class containing some commonly used constants by classes of the App.
 * @author schuessler
 *
 */
public final class Constants {

    private static final String CLAZZ = Constants.class.getCanonicalName();

    /**
     * ID for storing a result in an intent as extra (Intent.putExtra(INTENT_EXTRA_RESULT, result)).
     */
    public static final String INTENT_EXTRA_RESULT = CLAZZ + ".result";

    /**
     * ID for storing a notice as extra (Intent.putExtra(INTENT_EXTRA_NOTICE, notice)).
     */
    public static final String INTENT_EXTRA_NOTICE = CLAZZ + ".notice";

    /**
     * ID for storing an object of class AbstractDomDocumentCreator as extra (Intent.putExtra(INTENT_EXTRA_DOM, notice)).
     */
    public static final String INTENT_EXTRA_DOM = CLAZZ + ".domDocumentCreator";

    /**
     * ID for storing an event as extra (Intent.putExtra(INTENT_EXTRA_EVENT, event)).
     */
    public static final String INTENT_EXTRA_EVENT = CLAZZ + ".event";

    /**
     * ID for storing an event as extra (Intent.putExtra(INTENT_EXTRA_TASK, task)).
     */
    public static final String INTENT_EXTRA_TASK = CLAZZ + ".task";

    /**
     * Key for preference where the password is stored.
     */
    public static final String PREF_KEY_LOGIN = "login_pw";

    private Constants() {}
}

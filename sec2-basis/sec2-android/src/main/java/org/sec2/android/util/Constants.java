package org.sec2.android.util;

/**
 * Class containing some commonly used constants by classes of the
 * sec2-android-module.
 *
 * @author nike
 */
public final class Constants
{
    private static final String CLAZZ = Constants.class.getCanonicalName();

    /**
     * ID for storing the ID of an lock object (@see LockObjectHandler) to be
     * used by the target of the intent
     * (Intent.putExtra(INTENT_EXTRA_LOCK_OBJ_ID, lockObjectId)).
     */
    public static final String INTENT_EXTRA_LOCK_OBJ_ID =
            CLAZZ + ".lockObjectId";

    /**
     * ID for storing the app name of the app which wants to register itself at
     * the Sec2-middleware
     * (Intent.putExtra(INTENT_EXTRA_APP_NAME, appName)).
     */
    public static final String INTENT_EXTRA_APP_NAME = CLAZZ + ".appName";

    /**
     * ID for storing the login password to the administration app of the
     * Sec2-middleware.
     * (Intent.putExtra(INTENT_EXTRA_LOGIN_PW, loginPw))
     */
    public static final String INTENT_EXTRA_LOGIN_PW = CLAZZ + ".loginPw";

    /**
     * ID for storing the key for the encrypted database, where all
     * app-authentication-keys are stored.
     * (Intent.putExtra(INTENT_EXTRA_DB_KEY, dbKey))
     */
    public static final String INTENT_EXTRA_DB_KEY = CLAZZ + ".dbKey";

    /**
     * ID for storing a selected @see User object
     * (Intent.putExtra(INTENT_EXTRA_USER, user)).
     */
    public static final String INTENT_EXTRA_USER = CLAZZ + ".user";

    /**
     * ID for storing a selected @see Group object
     * (Intent.putExtra(INTENT_EXTRA_GROUP, group)).
     */
    public static final String INTENT_EXTRA_GROUP = CLAZZ + ".group";

    /**
     * ID for storing an array of user-IDs
     * (Intent.putExtra(INTENT_EXTRA_USER_IDS, userIds)).
     */
    public static final String INTENT_EXTRA_USER_IDS = CLAZZ + ".userIds";

    /**
     * ID for storing an array of User objects
     * (Intent.putExtra(INTENT_EXTRA_USERS, users)).
     */
    public static final String INTENT_EXTRA_USERS = CLAZZ + ".users";

    /**
     * ID for storing the result of an activity, which was started for result
     * (Intent.putExtra(INTENT_EXTRA_RESULT, result)).
     */
    public static final String INTENT_EXTRA_RESULT = CLAZZ + ".result";

    private Constants(){}
}

package de.adesso.mobile.android.sec2.mwadapter.util;

import android.app.Activity;
import android.database.Cursor;
import android.provider.ContactsContract;
import de.adesso.mobile.android.sec2.mwadapter.model.User;

/**
 * Helper class which tries to match an email address with a name in the contact
 * list of the Android smartphone.
 * 
 * @author schuessler
 *
 */
public class EmailToUserNameMapper
{
    private static final String[] columns = new String[]{ContactsContract.Contacts.DISPLAY_NAME};
    private static final String whereClause = ContactsContract.Contacts.IN_VISIBLE_GROUP + "=? AND "
            + ContactsContract.CommonDataKinds.Email.DATA + "=?";

    //Private constructor to avoid creation of objects.
    private EmailToUserNameMapper(){};

    /**
     * Tries to map the email address which was passed to the constructor to a name from the contact list
     * of the Android smartphone. If the email address could be matched, the name of the passed User object
     * is set to the name found, otherwise it is set to NULL. The method User.getUserEmail() must not
     * return NULL or be empty, otherwise a NullPointerException is thrown.
     * 
     * @param activity - The calling activity
     * @param user - The user whose name has to be set.
     */
    public static synchronized void emailToUserName(final Activity activity, final User user)
    {
        Cursor result = null;

        if(user.getUserEmail() == null || user.getUserEmail().isEmpty())
            throw new NullPointerException("The user's email address must not be NULL!");
        result = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                columns, whereClause, new String[]{"1", user.getUserEmail()}, null);
        if(result != null && result.moveToFirst())
        {
            if(!result.isNull(0)) user.setUserName(result.getString(0));
            else user.setUserName("");
            result.close();
        }
        else user.setUserName(null);
    }
}

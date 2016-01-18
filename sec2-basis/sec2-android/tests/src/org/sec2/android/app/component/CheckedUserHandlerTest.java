package org.sec2.android.app.component;

import android.test.AndroidTestCase;
import de.adesso.mobile.android.sec2.mwadapter.model.User;

/**
 * This JUnit-Class tests the methods of class CheckedUserHandler.
 * 
 * @author nike
 */
public final class CheckedUserHandlerTest extends AndroidTestCase
{
    private CheckedUserHandler emptyHandler = new CheckedUserHandler();
    private CheckedUserHandler filledHandler = new CheckedUserHandler
            (new User[]{new User("1", "A", "a"), new User("2", "B", "b"),
                    new User("3", "C", "c")});

    /**
     * This method tests the getCheckedUsers()-method, when the handler is
     * empty.
     */
    public void testGetCheckedUsersEmpty()
    {
        assertEquals(0, emptyHandler.getCheckedUsers().length);
    }

    /**
     * This method tests the getNumberOfCheckedUsers()-method, when the handler
     * is empty.
     */
    public void testGetNumberOfCheckedUsersEmpty()
    {
        assertEquals(0, emptyHandler.getNumberOfCheckedUsers());
    }

    /**
     * This method tests the getCheckedUsers()-method, when the handler is not
     * empty.
     */
    public void testGetCheckedUsersFilled()
    {
        boolean[] checkedUsers = filledHandler.getCheckedUsers();

        assertEquals(3, checkedUsers.length);
        for(int i = 0; i < 3; i++) assertFalse(checkedUsers[i]);
    }

    /**
     * This method tests the getNumberOfCheckedUsers()-method, when the
     * handler is not empty.
     */
    public void testGetNumberOfCheckedUsersFilled()
    {
        assertEquals(0, filledHandler.getNumberOfCheckedUsers());
    }

    /**
     * This method tests the isChecked()-method, when the handler is not empty.
     */
    public void testIsCheckedFilled()
    {
        assertFalse(filledHandler.isChecked(0));
    }

    /**
     * This method tests the setChecked()-method, when the handler is not
     * empty.
     */
    public void testSetCheckedFilled()
    {
        boolean[] checkedUsers = null;

        filledHandler.setChecked(1, true);
        checkedUsers = filledHandler.getCheckedUsers();
        assertFalse(checkedUsers[0]);
        assertTrue(checkedUsers[1]);
        assertFalse(checkedUsers[2]);
        assertEquals(1, filledHandler.getNumberOfCheckedUsers());
        assertTrue(filledHandler.isChecked(1));
        filledHandler.setChecked(1, false);
    }

    /**
     * This method tests the toggle()-method, when the handler is not
     * empty.
     */
    public void testToggleFilled()
    {
        boolean[] checkedUsers = null;

        filledHandler.toggle(1);
        checkedUsers = filledHandler.getCheckedUsers();
        assertFalse(checkedUsers[0]);
        assertTrue(checkedUsers[1]);
        assertFalse(checkedUsers[2]);
        assertEquals(1, filledHandler.getNumberOfCheckedUsers());
        assertTrue(filledHandler.isChecked(1));
        filledHandler.toggle(1);
    }
}

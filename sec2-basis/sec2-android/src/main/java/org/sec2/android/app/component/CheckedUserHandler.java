package org.sec2.android.app.component;

import java.util.Arrays;

import de.adesso.mobile.android.sec2.mwadapter.model.User;
import de.adesso.mobile.android.sec2.mwadapter.util.UserHandler;

/**
 * Works like its super class, but adds an additional array to mark, if a user
 * is checked or not.
 *
 * @author nike
 */
public class CheckedUserHandler extends UserHandler
{
    private boolean[] checked;
    private int numberOfCheckedUsers = 0;

    /**
     * This constructor constructs an object of type CheckedUserHandler for an
     * empty array of type User.
     */
    public CheckedUserHandler()
    {
        this(null);
    }

    /**
     * The constructor for the CheckedUserHandler. It splits up the passed
     * array of type User in three arrays. One array for the users' IDs, one
     * array for the users' names, one array for the users' email-addresses and
     * one for the information, if a user is checked or not.
     *
     * @param users - An array of type User
     */
    public CheckedUserHandler(final User[] users)
    {
        super(users);
        if (users != null)
        {
            checked = new boolean[users.length];
            for (int i = 0; i < users.length; i++)
            {
                checked[i] = false;
            }
        }
        else
        {
            checked = new boolean[0];
        }
    }

    /**
     * Sets the user at "index" to the value of "checked".
     *
     * @param index - The index of the user, to be checked/unchecked
     * @param checked - TRUE if the user is checked, otherwise FALSE
     */
    public void setChecked(final int index, final boolean checked)
    {
        if (this.checked[index] != checked)
        {
            if (checked)
            {
                numberOfCheckedUsers++;
            }
            else
            {
                numberOfCheckedUsers--;
            }
        }
        this.checked[index] = checked;
    }

    /**
     * Returns, whether the user at "index" is checked or not.
     *
     * @param index - The index of the user
     *
     * @return Whether the user is checked or not
     */
    public boolean isChecked(final int index)
    {
        return checked[index];
    }

    /**
     * Toggles the checked-status of the user at "index".
     *
     * @param index - The index of the group
     */
    public void toggle(final int index)
    {
        if (checked[index])
        {
            numberOfCheckedUsers--;
        }
        else
        {
            numberOfCheckedUsers++;
        }
        checked[index] = !checked[index];
    }

    /**
     * Returns the number of checked users.
     *
     * @return The number of checked users
     */
    public int getNumberOfCheckedUsers()
    {
        return numberOfCheckedUsers;
    }

    /**
     * Returns a copy of the array, where it is marked, whether a user is
     * checked or not.
     *
     * @return A copy of the array where it is marked, whether a user is
     *  checked or not
     */
    public boolean[] getCheckedUsers()
    {
        return Arrays.copyOf(checked, checked.length);
    }
}

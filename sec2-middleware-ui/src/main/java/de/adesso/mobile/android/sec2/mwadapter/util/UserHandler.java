package de.adesso.mobile.android.sec2.mwadapter.util;

import de.adesso.mobile.android.sec2.mwadapter.model.User;

/**
 * A class for easier handling of arrays of type User. This class splits up an array of type
 * Group in three arrays. On array for the users' IDs, one array for the users' names and one
 * array for the users' email-addresses.
 * This makes it e.g. possible to get an array with all IDs of the users or with all of the names
 * of the users with only one method call.
 * 
 * @author nike
 *
 */
public class UserHandler
{
    private final String[] userIds;
    private final String[] userNames;
    private final String[] emailAddresses;

    /**
     * The constructor for the UserHandler. It splits up the passed array of type User in two
     * arrays. One for all the containing IDs and one for all the containing names.
     * 
     * @param users - An array of type User
     */
    public UserHandler(final User[] users)
    {
        if(users != null)
        {
            userIds = new String[users.length];
            userNames = new String[users.length];
            emailAddresses = new String[users.length];
            for(int i = 0; i < users.length; i++)
            {
                userIds[i] = users[i].getUserId();
                userNames[i] = users[i].getUserName();
                emailAddresses[i] = users[i].getUserEmail();
            }
        }
        else
        {
            userIds = new String[0];
            userNames = new String[0];
            emailAddresses = new String[0];
        }
    }

    /**
     * An array with the IDs of all users which where contained in the array of type User.
     * 
     * @return all the IDs of the users.
     */
    public String[] getUserIds()
    {
        return userIds;
    }

    /**
     * An array with the names of all users which where contained in the array of type User.
     * 
     * @return all the names of the users.
     */
    public String[] getUserNames()
    {
        return userNames;
    }

    /**
     * An array with the email-addresses of all users, which where contained in the array of type User.
     * 
     * @return all the email-addresses of the users.
     */
    public String[] getEmailAddresses()
    {
        return emailAddresses;
    }

    /**
     * Returns the number of contained users.
     * 
     * @return The number of contained users.
     */
    public int getCount()
    {
        return userIds.length;
    }

    /**
     * This method returns the User-object at the given index of the user-object-list. If the given position is out of bound
     * (index < 0 || index > list-size), NULL will be returned.
     * 
     * @param index - The index in the list of the user to be returned
     * 
     * @return The user at the given index of the list. Returns NULL, if the given position is out of bound.
     */
    public User getUserAtIndex(int index)
    {
        if(index >= 0 && index < getCount())
            return new User(userIds[index], userNames[index], emailAddresses[index]);
        else return null;
    }
}
package de.adesso.mobile.android.sec2.mwadapter.util;

import java.util.Arrays;

import de.adesso.mobile.android.sec2.mwadapter.model.Group;
import de.adesso.mobile.android.sec2.mwadapter.model.User;

/**
 * A class for easier handling of arrays of type Group. This class splits up an array of type
 * Group in two arrays. One array for the group's IDs, one array for the group's names and one
 * for the group's owners.
 * This makes it e.g. possible to get an array with all IDs of the groups or with all of the names
 * of the groups with only one method call.
 * 
 * @author nike
 *
 */
public class GroupHandler
{
    // groupId became a group name
//    private final String[] groupIds;
    private final String[] groupNames;
    private final User[] groupOwners;
    private final int numberOfGroups;

    /**
     * The constructor for the GroupHandler. It splits up the passed array of type Group in two
     * arrays. One for all the containing IDs and one for all the containing names.
     * 
     * @param groups - An array of type Group
     */
    public GroupHandler(final Group[] groups)
    {
        if(groups != null) numberOfGroups = groups.length;
        else numberOfGroups = 0;

//        groupIds = new String[numberOfGroups];
        groupNames = new String[numberOfGroups];
        groupOwners = new User[numberOfGroups];
        for(int i = 0; i < numberOfGroups; i++)
        {
//            groupIds[i] = groups[i].getGroupId();
            groupNames[i] = groups[i].getGroupName();
            groupOwners[i] = groups[i].getGroupOwner();
        }
    }

    /**
     * An array with the IDs of all groups which where contained in the array of type Group.
     * 
     * @return all the IDs of the groups.
     */
    public String[] getGroupIds()
    {
        return getGroupNames();
    }

    /**
     * An array with the names of all groups which where contained in the array of type Group.
     * 
     * @return all the names of the groups.
     */
    public String[] getGroupNames()
    {
        return Arrays.copyOf(groupNames, groupNames.length);
    }

    /**
     * Returns the Id of the group at position "index"
     * 
     * @param index - The index of the group whose ID is to be returned
     * 
     * @return The group ID
     */
    public String getId(final int index)
    {
        return getGroupName(index);
    }

    /**
     * Returns the name of the group at position "index"
     * 
     * @param index - The index of the group whose name is to be returned
     * 
     * @return The group name
     */
    public String getGroupName(final int index)
    {
        return groupNames[index];
    }

    /**
     * Returns the number of groups.
     * 
     * @return The number of groups
     */
    public int getNumberOfGroups()
    {
        return numberOfGroups;
    }

    /**
     * This method returns the Group-object at the given index of the group-objects-list. If the given position is out of bound
     * (index < 0 || index > list-size), NULL will be returned.
     * 
     * @param index - The index in the list of the group to be returned
     * 
     * @return The group at the given index of the list. Returns NULL, if the given position is out of bound.
     */
    public Group getGroupAtIndex(final int index)
    {
        if(index >= 0 && index < numberOfGroups) {
            return new Group(groupNames[index], groupNames[index], groupOwners[index]);
        } else {
            return null;
        }
    }
}
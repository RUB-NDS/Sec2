package de.adesso.mobile.android.sec2.mwadapter.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Bean-class representing a group-object in the Sec2-Framework. Furthermore it
 * implements the "Parcelable"-interface so that it can be stored in bundles
 * or extras of intents.
 * 
 * @author schuessler
 */
public class Group implements Parcelable
{
    // group Id became a group name
//    private String groupId = null;
    private String groupName = null;
    private User groupOwner = null;
    private boolean groupOwnerSet = false;

    /**
     * This variable is required by the Parcelable-interface.
     */
    public static final Parcelable.Creator<Group> CREATOR = new GroupCreator();

    /**
     * The empty constructor. It constructs a Group-object where the attributes
     * groupId, groupName and groupOwner are set to NULL. The group owner is
     * not set.
     */
    public Group(){}

    /**
     * This constructor expects the Id and the name of the group. The group
     * owner is not set.
     * 
     * @param groupId - The Id of the group
     * @param groupName - The nane of the group
     */
    @Deprecated
    public Group(final String groupId, final String groupName)
    {
//        this.groupId = groupId;
        this.groupName = groupName;
    }
    
    public Group(final String groupName)
    {
        this.groupName = groupName;
    }

    /**
     * This is the preferred constructor. It expects the Id, the name and the
     * owner of the group to be set. If User-object for the group owner is
     * NULL, the group owner is not set.
     * 
     * @param groupId - The Id of the group
     * @param groupName - The name of the group
     * @param groupOwner - The owner of the group
     */
    @Deprecated
    public Group(final String groupId, final String groupName, final User groupOwner)
    {
//        this.groupId = groupId;
        this.groupName = groupName;
        this.groupOwner = groupOwner;
        if(this.groupOwner != null) groupOwnerSet = true;
    }
    
    /**
     * This is the preferred constructor. It expects the name and the
     * owner of the group to be set. If User-object for the group owner is
     * NULL, the group owner is not set.
     * 
     * @param groupName - The name of the group
     * @param groupOwner - The owner of the group
     */
    public Group(final String groupName, final User groupOwner)
    {
        this.groupName = groupName;
        this.groupOwner = groupOwner;
        if(this.groupOwner != null) groupOwnerSet = true;
    }

    /**
     * Protected constructor, which should only be used, to create a Group-object, after it was parceled.
     * 
     * @param in - The Parcel-object.
     */
    protected Group(final Parcel in)
    {
//        groupId = in.readString();
        groupName = in.readString();
        groupOwner = in.readParcelable(User.class.getClassLoader());
        if(groupOwner != null) groupOwnerSet = true;
    }

    /**
     * Returns the Id of the group.
     *
     * @return the groupId
     */
    @Deprecated
    public String getGroupId() {
        return groupName;
    }

    /**
     * Sets the id of the group.
     *
     * @param groupId the groupId to set
     */
    @Deprecated
    public void setGroupId(final String groupId) {
        this.groupName = groupId;
    }

    /**
     * Returns the name of the group.
     *
     * @return the groupName
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Sets the name of the group.
     *
     * @param groupName the groupName to set
     */
    public void setGroupName(final String groupName) {
        this.groupName = groupName;
    }

    /**
     * Returns the owner of the group.
     *
     * @return the groupOwner
     */
    public User getGroupOwner()
    {
        return groupOwner;
    }

    /**
     * Sets the owner of the group. If the User-object for the group owner is
     * NULL, the group owner is not set.
     *
     * @param groupOwner the groupOwner to set
     */
    public void setGroupOwner(final User groupOwner)
    {
        this.groupOwner = groupOwner;
        if(this.groupOwner != null) groupOwnerSet = true;
        else groupOwnerSet = false;
    }

    /**
     * Returns TRUE, if the owner of the group was set. Otherwise FALSE.
     * 
     * @return Whether the owner of the group was set or not
     */
    public boolean isGroupOwnerSet()
    {
        return groupOwnerSet;
    }

    /* (non-Javadoc)
     * @see android.os.Parcelable#describeContents()
     */
    @Override
    public int describeContents()
    {
        // Returns 0, because the method here has no special meaning, but must be implementend because of the interface "Parcelable"
        return 0;
    }

    /* (non-Javadoc)
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(final Parcel arg0, final int arg1)
    {
//        arg0.writeString(groupId);
        arg0.writeString(groupName);
        arg0.writeParcelable(groupOwner, arg1);
    }

    private static final class GroupCreator implements Parcelable.Creator<Group>
    {
        @Override
        public Group createFromParcel(final Parcel source)
        {
            return new Group(source);
        }

        @Override
        public Group[] newArray(final int size)
        {
            return new Group[size];
        }
    }
}
package org.sec2.android.model;

import android.os.Parcel;
import android.os.Parcelable;
import de.adesso.mobile.android.sec2.mwadapter.model.Group;
import de.adesso.mobile.android.sec2.mwadapter.model.User;

/**
 * Bean-class representing a group in the Sec2-Framework. It extends the
 * Group-class by an attribute containing the number of group-members.
 *
 * @author schuessler
 */
public class CountedGroup extends Group
{
    private int memberCount = 0;

    /**
     * This variable is specified by the Parcelable-interface.
     */
    public static final Parcelable.Creator<CountedGroup> CREATOR =
            new CountedGroupCreator();

    /**
     * The empty standard-constructor.
     */
    public CountedGroup()
    {
        super();
    }

    /**
     * Constructor for directly passing the group's ID and name.
     *
     * @param groupId - The group's ID
     * @param groupName - The group's name
     */
    public CountedGroup(final String groupId, final String groupName)
    {
        super(groupId, groupName);
    }

    /**
     * Constructor for directly passing the group's ID, name and owner.
     *
     * @param groupId - The group's ID
     * @param groupName - The group's name
     * @param groupOwner - The group's owner
     */
    public CountedGroup(final String groupId, final String groupName,
            final User groupOwner)
    {
        super(groupId, groupName, groupOwner);
    }

    /**
     * The preferred constructor for directly passing the group's ID, name,
     * owner and the number of members of this group.
     *
     * @param groupId - The group's ID
     * @param groupName - The group's name
     * @param groupOwner - The group's owner
     * @param memberCount - The number of members of this group
     */
    public CountedGroup(final String groupId, final String groupName,
            final User groupOwner, final int memberCount)
    {
        super(groupId, groupName, groupOwner);
        this.memberCount = memberCount;
    }

    /**
     * Protected constructor, which should only be used, to create a
     * CountedGroup-object, after it was parceled.
     *
     * @param in - The Parcel-object.
     */
    protected CountedGroup(final Parcel in)
    {
        super(in);
        memberCount = in.readInt();
    }

    /**
     * Returns the number of group-members. If a value less than 0 is returned,
     * the number of group-members wasn't set.
     *
     * @return The number of group-members; a value less than 0, if the number
     *  of group-members wasn't set.
     */
    public int getMemberCount()
    {
        return memberCount;
    }

    /**
     * Sets the number of group-members.
     *
     * @param memberCount - The number of group-members
     */
    public void setMemberCount(final int memberCount)
    {
        this.memberCount = memberCount;
    }

    /* (non-Javadoc)
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(final Parcel arg0, final int arg1)
    {
        super.writeToParcel(arg0, arg1);
        arg0.writeInt(memberCount);
    }

    /*
     * Private class, used by the Parcelable-interface to recreate a
     * CountedGroup-object, after it was parceled.
     */
    private static final class CountedGroupCreator
    implements Parcelable.Creator<CountedGroup>
    {
        @Override
        public CountedGroup createFromParcel(final Parcel source)
        {
            return new CountedGroup(source);
        }

        @Override
        public CountedGroup[] newArray(final int size)
        {
            return new CountedGroup[size];
        }
    }
}

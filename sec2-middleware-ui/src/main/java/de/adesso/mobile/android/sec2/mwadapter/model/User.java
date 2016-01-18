package de.adesso.mobile.android.sec2.mwadapter.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Bean-class representing an user-object in the Sec2-Framework. Furthermore it
 * implements the "Parcelable"-interface so that it can stored in bundles or
 * extras of intents.
 * 
 * @author schuessler
 *
 */
public class User implements Parcelable
{
    private String userId = "";
    private String userName = "";
    private String userEmail = "";

    /**
     * This variable is required by the Parcelable-interface.
     */
    public static final Parcelable.Creator<User> CREATOR = new UserCreator();

    /**
     * This is the preferred constructor. it expects the id, the name and the
     * email address of the user to be set.
     *
     * @param userId - The ID of the user
     * @param userName - The name of the user
     * @param userEmail - The email-address of the user
     */
    public User(final String userId, final String userName, final String userEmail)
    {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
    }

    private User(final Parcel in)
    {
        userId = in.readString();
        userName = in.readString();
        userEmail = in.readString();
    }

    /**
     * The empty constructor. It constructs a Group-object where the attributes
     * userId, userName and userEmail are set to an empty string.
     */
    public User(){}

    /**
     * Returns the ID of the user.
     *
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the ID of the user.
     *
     * @param userId the userId to set
     */
    public void setUserId(final String userId) {
        this.userId = userId;
    }

    /**
     * Returns the name of the user.
     *
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the name of the user.
     *
     * @param userName the userName to set
     */
    public void setUserName(final String userName) {
        this.userName = userName;
    }

    /**
     * Returns the email-address of the user.
     *
     * @return the userEmail
     */
    public String getUserEmail()
    {
        return userEmail;
    }

    /**
     * Sets the email-address of the user.
     *
     * @param userEmail the userEmail to set
     */
    public void setUserEmail(final String userEmail)
    {
        this.userEmail = userEmail;
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
        arg0.writeString(userId);
        arg0.writeString(userName);
        arg0.writeString(userEmail);
    }

    private static final class UserCreator implements Parcelable.Creator<User>
    {
        @Override
        public User createFromParcel(final Parcel source)
        {
            return new User(source);
        }

        @Override
        public User[] newArray(final int size)
        {
            return new User[size];
        }
    }
}
package org.sec2.android.model;

import android.os.Parcel;
import android.os.Parcelable;
import de.adesso.mobile.android.sec2.mwadapter.crypto.AppAuthKey;

/**
 * This class represents a session key which is used by the administration app
 * of the Sec2-middleware to communicate with the REST-interface of the
 * middleware like all third-party-apps have to do it, if they want to
 * communicate with the Sec2-middleware. The session key is used by the
 * administration-app instead of the "App Authentication Key", but it has
 * nearly the same structure why this class bases on the class "AppAuthKey"
 * from the MwAdapter-Library. The field "sessionToken" must be used instead of
 * the app's name when communication with the REST-interface. Furthermore it
 * implements the "Parcelable"-interface so that it can be send via IPC.
 *
 * @author schuessler
 */
public class SessionKey extends AppAuthKey implements Parcelable
{
    /**
     * This variable is specified by the Parcelable-interface.
     */
    public static final Parcelable.Creator<SessionKey> CREATOR =
            new SessionKeyCreator();

    private String sessionToken = null;

    /**
     * Constructor for class SessionKey, expecting that the "key", the
     * "algorithm" and the "sessionToken" are passed.
     *
     * @param key - The value of the key
     * @param algorithm - The algorithm, which has to be used with the key
     * @param sessionToken - A string token, which is bound to the key like the
     *  app's name in context of the "App authentication key"
     */
    public SessionKey(final String key, final String algorithm,
            final String sessionToken)
    {
        super(key, algorithm);
        this.sessionToken = sessionToken;
    }

    /**
     * Empty constructor for class SessionKey. It constructs an object, whose
     * attributes "key", "algorithm" and "sessionToken" are set to NULL.
     */
    public SessionKey()
    {
        super(null, null);
        sessionToken = null;
    }

    private SessionKey(final Parcel in)
    {
        this(); //Instantiate object first
        setKey(in.readString());
        setAlgorithm(in.readString());
        sessionToken = in.readString();
    }

    /* (non-Javadoc)
     * @see android.os.Parcelable#describeContents()
     */
    @Override
    public int describeContents()
    {
        // Returns 0, because the method here has no special meaning, but must
        //be implemented because of the interface "Parcelable"
        return 0;
    }

    /* (non-Javadoc)
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(final Parcel arg0, final int arg1)
    {
        arg0.writeString(getKey());
        arg0.writeString(getAlgorithm());
        arg0.writeString(sessionToken);
    }

    /**
     * Returns the session-token which has to be used instead of the app's name
     * when communication with the REST-interface.
     *
     * @return the sessionToken
     */
    public String getSessionToken() {
        return sessionToken;
    }

    /**
     * Sets the session-token which has to be used instead of the app's name
     * when communication with the REST-interface.
     *
     * @param sessionToken - The sessionToken to set
     */
    public void setSessionToken(final String sessionToken) {
        this.sessionToken = sessionToken;
    }

    private static final class SessionKeyCreator
    implements Parcelable.Creator<SessionKey>
    {
        @Override
        public SessionKey createFromParcel(final Parcel source)
        {
            return new SessionKey(source);
        }

        @Override
        public SessionKey[] newArray(final int size)
        {
            return new SessionKey[size];
        }
    }
}

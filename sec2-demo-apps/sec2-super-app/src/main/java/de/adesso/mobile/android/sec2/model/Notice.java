
package de.adesso.mobile.android.sec2.model;

import java.io.Serializable;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class representing a notice.
 * 
 * @author schuessler
 *
 */
public class Notice implements Parcelable, Serializable, Cloneable {

    /**
     * Place holder for parts of the notice text, which are to be encrypted. 
     * These parts are extracted from
     * the notice text and moved to a special array.
     */
    public static final String PLACE_HOLDER = "[?]";

    /**
     * The pattern for the String sequence of the place holder.
     */
    public static final String PLACE_HOLDER_PATTERN = "\\[\\?\\]";

    private static final long serialVersionUID = 6343143810669063547L;

    private String mSubject = null;
    private String mNoticeText = null;
    private List<String> mPartsToEncrypt = null;
    private GregorianCalendar mCreationDate = null;
    private Lock mLock = null;
    private String mFilename = null;

    /**
     * Constructor
     */
    public Notice() {

    }

    /**
     * Constructor used for Parcelable
     * @param source object containing the parcelable object
     */
    private Notice(final Parcel source) {
        this.mSubject = source.readString();
        this.mNoticeText = source.readString();
        source.readList(mPartsToEncrypt, String.class.getClassLoader());
        this.mCreationDate = new GregorianCalendar();
        this.mCreationDate.setTime(new Date(source.readLong()));
        this.mLock = Lock.fromInt(source.readInt());
        this.mFilename = source.readString();
    }

    /**
     * @return the subject
     */
    public final String getSubject() {
        return mSubject;
    }

    /**
     * @param subject the subject to set
     */
    public final void setSubject(final String subject) {
        this.mSubject = subject;
    }

    /**
     * @return the noticeText
     */
    public final String getNoticeText() {
        return mNoticeText;
    }

    /**
     * @param noticeText the noticeText to set
     */
    public final void setNoticeText(final String noticeText) {
        this.mNoticeText = noticeText;
    }

    /**
     * @return the partsToEncrypt
     */
    public final List<String> getPartsToEncrypt() {
        return mPartsToEncrypt;
        // if (mPartsToEncrypt == null) {
        // return null;
        // } else {
        // return mPartsToEncrypt;
        // }
    }

    /**
     * @param partsToEncrypt the partsToEncrypt to set
     */
    public final void setPartsToEncrypt(final List<String> partsToEncrypt) {
        this.mPartsToEncrypt = partsToEncrypt;
        // if (partsToEncrypt != null) {
        // Collections.copy(this.mPartsToEncrypt, partsToEncrypt);
        // }
    }

    /**
     * @return the creationDate
     */
    public final GregorianCalendar getCreationDate() {
        return mCreationDate;
    }

    /**
     * @param creationDate the creationDate to set
     */
    public final void setCreationDate(final GregorianCalendar creationDate) {
        this.mCreationDate = creationDate;
    }

    /**
     * @return the lock
     */
    public final Lock getLock() {
        return mLock;
    }

    /**
     * @param lock the lock to set
     */
    public final void setLock(final Lock lock) {
        this.mLock = lock;
    }

    
    /**
     * @return
     */
    public final String getFilename() {
        return mFilename;
    }


    /**
     * @param fileName
     */
    public final void setFilename(final String fileName) {
        this.mFilename = fileName;
    }
    
    /*
     * (non-Javadoc)
     * @see android.os.Parcelable#describeContents()
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(this.mSubject);
        dest.writeString(this.mNoticeText);
        dest.writeList(mPartsToEncrypt);
        dest.writeLong(this.mCreationDate.getTimeInMillis());
        dest.writeInt(this.mLock.getType());
        dest.writeString(this.mFilename);
    }

    /**
     * Creator
     */
    public static final Parcelable.Creator<Notice> CREATOR = new Parcelable.Creator<Notice>() {

        @Override
        public Notice createFromParcel(final Parcel source) {
            return new Notice(source);
        }

        @Override
        public Notice[] newArray(final int size) {
            return new Notice[size];
        }
    };

    /*
     * (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final String lineBreak = "\n";
        final StringBuilder builder = new StringBuilder();
        builder.append(this.mSubject);
        builder.append(lineBreak);
        builder.append(this.mNoticeText);
        builder.append(lineBreak);
        if (mPartsToEncrypt != null) {
            for (final String item : this.mPartsToEncrypt) {
                builder.append(item);
                builder.append(lineBreak);
            }
        }
        if (this.mCreationDate != null) {
            builder.append(this.mCreationDate.getTime().toString());
            builder.append(lineBreak);
        }
        if (this.mLock != null) {
            builder.append("Lock: " + this.mLock.getType());
        }
        return builder.toString();
    };

}

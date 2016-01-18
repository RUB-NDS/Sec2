package de.adesso.mobile.android.sec2.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Model class for start end values of a NoticeItemList.
 * @author hoppe
 */
public class NoticeSelection implements Parcelable {

    @SuppressWarnings ("unused")
    private static final Class<?> c = NoticeSelection.class;

    @XStreamAlias ("section")
    public java.lang.String section;
    @XStreamAlias ("xenc:EncryptedData")
    public EncryptedData encryptedData;

    public NoticeSelection() {}

    public NoticeSelection(String section, EncryptedData encryptedData) {
        this.section = section;
        this.encryptedData = encryptedData;
    }

    // Constructor
    //    public NoticeSelection(final Cursor cursor) {
    //        this.id = cursor.getLong(cursor.getColumnIndex(SQL.NoticeSelection.ID));
    //        this.noticeId = cursor.getLong(cursor.getColumnIndex(SQL.NoticeSelection.ID_NOTICE));
    //    }

    // Constructor
    public NoticeSelection(final Parcel source) {
        readFromParcel(source);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(final Parcel source) {
        this.section = source.readString();
        this.encryptedData = source.readParcelable(EncryptedData.class.getClassLoader());

    }

    @Override
    public void writeToParcel(Parcel dest, final int flags) {
        dest.writeString(this.section);
        dest.writeParcelable(encryptedData, flags);
    }

    public static final Parcelable.Creator<NoticeSelection> CREATOR = new Parcelable.Creator<NoticeSelection>() {

        @Override
        public NoticeSelection createFromParcel(Parcel source) {
            return new NoticeSelection(source);
        }

        @Override
        public NoticeSelection[] newArray(int size) {
            return new NoticeSelection[size];
        }
    };

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("NoticeSelection( ");
        sb.append("section:" + section);
        sb.append(", ");
        sb.append("encryptedData:" + encryptedData);
        sb.append(" )");

        return sb.toString();
    }
}

package de.adesso.mobile.android.sec2.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class KeyInfo implements Parcelable {

    @XStreamAsAttribute
    @XStreamAlias ("xmlns:ds")
    public java.lang.String xmlns_ds;
    @XStreamAlias ("ds:KeyName")
    public java.lang.String ds_KeyName;

    public KeyInfo() {
        this.xmlns_ds = "http://www.w3.org/2000/09/xmldsig#";
        this.ds_KeyName = "Group 1";
    }

    // Constructor
    public KeyInfo(final Parcel source) {
        readFromParcel(source);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(final Parcel source) {
        this.xmlns_ds = source.readString();
        this.ds_KeyName = source.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, final int flags) {
        dest.writeString(this.xmlns_ds);
        dest.writeString(this.ds_KeyName);
    }

    public static final Parcelable.Creator<KeyInfo> CREATOR = new Parcelable.Creator<KeyInfo>() {

        @Override
        public KeyInfo createFromParcel(Parcel source) {
            return new KeyInfo(source);
        }

        @Override
        public KeyInfo[] newArray(int size) {
            return new KeyInfo[size];
        }
    };

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\nKeyInfo(");
        builder.append("xmlns:ds: " + xmlns_ds);
        builder.append(", ");
        builder.append("ds:KeyName: " + ds_KeyName);
        builder.append(")\n");
        return builder.toString();
    }

}

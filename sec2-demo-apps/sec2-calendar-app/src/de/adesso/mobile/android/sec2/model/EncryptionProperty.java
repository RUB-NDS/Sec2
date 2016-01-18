package de.adesso.mobile.android.sec2.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

public class EncryptionProperty implements Parcelable {

    @XStreamAlias ("ds:KeyInfo")
    public KeyInfo ds_keyInfo;

    public EncryptionProperty() {
        this.ds_keyInfo = new KeyInfo();
    }

    public EncryptionProperty(Parcel source) {
        readFromParcel(source);
    }

    public static final Parcelable.Creator<EncryptionProperty> CREATOR = new Parcelable.Creator<EncryptionProperty>() {

        @Override
        public EncryptionProperty createFromParcel(Parcel source) {
            return new EncryptionProperty(source);
        }

        @Override
        public EncryptionProperty[] newArray(int size) {
            return new EncryptionProperty[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel source) {
        this.ds_keyInfo = source.readParcelable(KeyInfo.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(ds_keyInfo, flags);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\nEncryptionProperty(");
        builder.append("ds:KeyInfo: " + ds_keyInfo);
        builder.append(")\n");
        return builder.toString();
    }

}

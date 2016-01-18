package de.adesso.mobile.android.sec2.model;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class EncryptionProperties implements Parcelable {

    @XStreamImplicit (itemFieldName = "xenc:EncryptionProperty")
    public List<EncryptionProperty> encryptionPropertieList = new ArrayList<EncryptionProperty>();

    public EncryptionProperties() {
        this.encryptionPropertieList.add(new EncryptionProperty());
    }

    public EncryptionProperties(Parcel source) {
        readFromParcel(source);
    }

    public static final Parcelable.Creator<EncryptionProperties> CREATOR = new Parcelable.Creator<EncryptionProperties>() {

        @Override
        public EncryptionProperties createFromParcel(Parcel source) {
            return new EncryptionProperties(source);
        }

        @Override
        public EncryptionProperties[] newArray(int size) {
            return new EncryptionProperties[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel source) {
        encryptionPropertieList = new ArrayList<EncryptionProperty>();
        source.readTypedList(encryptionPropertieList, EncryptionProperty.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(encryptionPropertieList);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\nEncryptionProperties(");
        builder.append("xenc:EncryptionProperty: " + encryptionPropertieList.toString());
        builder.append(")\n");
        return builder.toString();
    }
}

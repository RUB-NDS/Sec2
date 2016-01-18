package de.adesso.mobile.android.sec2.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class EncryptedData implements Parcelable {

    @XStreamAsAttribute
    @XStreamAlias ("xmlns:xenc")
    public java.lang.String xmlns_xenc;
    @XStreamAlias ("xenc:EncryptionMethod")
    public EncryptionMethod encryptedMethod;

    @XStreamAlias ("xenc:EncryptionProperties")
    public EncryptionProperties encryptionProperties;

    public EncryptedData() {
        this.xmlns_xenc = "http://www.w3.org/2001/04/xmlenc#";
        this.encryptedMethod = new EncryptionMethod();
        this.encryptionProperties = new EncryptionProperties();
    }

    // Constructor
    public EncryptedData(final Parcel source) {
        readFromParcel(source);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(final Parcel source) {
        this.xmlns_xenc = source.readString();
        this.encryptedMethod = source.readParcelable(EncryptionMethod.class.getClassLoader());
        this.encryptionProperties = source.readParcelable(EncryptionProperties.class.getClassLoader());

    }

    @Override
    public void writeToParcel(Parcel dest, final int flags) {
        dest.writeString(this.xmlns_xenc);
        dest.writeParcelable(encryptedMethod, flags);
        dest.writeParcelable(encryptionProperties, flags);
    }

    public static final Parcelable.Creator<EncryptedData> CREATOR = new Parcelable.Creator<EncryptedData>() {

        @Override
        public EncryptedData createFromParcel(Parcel source) {
            return new EncryptedData(source);
        }

        @Override
        public EncryptedData[] newArray(int size) {
            return new EncryptedData[size];
        }
    };

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\nEncryptedData(");
        builder.append("xmlns_xenc: " + xmlns_xenc);
        builder.append(", ");
        builder.append("xenc:EncryptionMethod: " + encryptedMethod.toString());
        builder.append(", ");
        builder.append("xenc:EncryptionProperties: " + encryptionProperties.toString());
        builder.append(")\n");
        return builder.toString();
    }

}

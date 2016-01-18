package de.adesso.mobile.android.sec2.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class EncryptionMethod implements Parcelable {

    @XStreamAsAttribute
    @XStreamAlias ("Algorithm")
    public java.lang.String algorithm;

    // TODO
    //    @XStreamAlias ("xenc:EncryptionMethod")
    //    public EncryptionMethod encryptedMethod;
    //
    //    @XStreamImplicit (itemFieldName = "xenc:EncryptionProperty")
    //    public List<EncryptionProperty> encryptionProperties = new ArrayList<EncryptionProperty>();

    public EncryptionMethod() {
        this.algorithm = "http://www.w3.org/2001/04/xmlenc#aes128-cbc";
    }

    // Constructor
    public EncryptionMethod(final Parcel source) {
        readFromParcel(source);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(final Parcel source) {
        this.algorithm = source.readString();

    }

    @Override
    public void writeToParcel(Parcel dest, final int flags) {
        dest.writeString(this.algorithm);
    }

    public static final Parcelable.Creator<EncryptionMethod> CREATOR = new Parcelable.Creator<EncryptionMethod>() {

        @Override
        public EncryptionMethod createFromParcel(Parcel source) {
            return new EncryptionMethod(source);
        }

        @Override
        public EncryptionMethod[] newArray(int size) {
            return new EncryptionMethod[size];
        }
    };

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\nEncryptionMethod(");
        builder.append("Algorithm: " + algorithm);
        builder.append(")\n");
        return builder.toString();
    }
}

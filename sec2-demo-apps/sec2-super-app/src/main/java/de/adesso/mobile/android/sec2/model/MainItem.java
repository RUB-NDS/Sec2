package de.adesso.mobile.android.sec2.model;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * MainItem
 * @author hoppe
 */
public class MainItem implements Parcelable {

    public String title;
    public int resId;
    public transient Intent intent;

    public MainItem(final String title, final int resId, final Intent intent) {
        this.title = title;
        this.resId = resId;
        this.intent = intent;
    }

    private MainItem(final Parcel source) {
        this.title = source.readString();
        this.resId = source.readInt();
        this.intent = null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, final int flags) {
        dest.writeString(title);
        dest.writeInt(resId);
    }

    /**
     * Creator
     */
    public static final Parcelable.Creator<MainItem> CREATOR = new Parcelable.Creator<MainItem>() {

        @Override
        public MainItem createFromParcel(final Parcel source) {
            return new MainItem(source);
        }

        @Override
        public MainItem[] newArray(final int size) {
            return new MainItem[size];
        }
    };

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("MainItem( ");
        sb.append("title:" + title);
        sb.append(", ");
        sb.append("resId:" + resId);
        sb.append(", ");
        sb.append("intent:" + intent);
        sb.append(" )\n");
        return sb.toString();
    }

}

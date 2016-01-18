package de.adesso.mobile.android.sec2.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Lock
 * an enum to save the states for the model NoticeListItem
 * @author hoppe
 */
public enum Lock implements Parcelable {

    UNLOCKED(0), PARTIALLY(1), LOCKED(2);

    private Integer type;

    // Constructor
    Lock(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return this.type;
    }

    public static Lock getEnum(Integer type) {
        for (Lock ct : Lock.values()) {
            if (ct.getType() == type) {
                return ct;
            }
        }
        return Lock.UNLOCKED;
    }

    public static Lock fromInt(final int i) {
        for (Lock lock : Lock.values()) {
            if (lock.type == i) {
                return lock;
            }
        }
        return Lock.UNLOCKED;
        //        throw new IllegalArgumentException("Lock was never");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel source) {
        this.type = source.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
    }

    public static final Parcelable.Creator<Lock> CREATOR = new Parcelable.Creator<Lock>() {

        @Override
        public Lock createFromParcel(Parcel source) {
            return Lock.fromInt(source.readInt());
        }

        @Override
        public Lock[] newArray(int size) {
            return new Lock[size];
        }
    };

}

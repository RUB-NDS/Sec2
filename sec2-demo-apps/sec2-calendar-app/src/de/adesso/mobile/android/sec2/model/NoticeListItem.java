package de.adesso.mobile.android.sec2.model;

import java.util.ArrayList;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import de.adesso.mobile.android.sec2.db.SQL;

/**
 * NoticeListItem
 * 
 * model class to store all notices that a user creates or has
 * 
 * @author bruch
 */
public class NoticeListItem implements Parcelable {

    @XStreamAlias ("nid")
    public long nid;
    @XStreamAlias ("date")
    public String date;
    @XStreamAlias ("subject")
    public String subject;
    @XStreamAlias ("lock")
    public Lock lock;
    @XStreamImplicit (itemFieldName = "content")
    public ArrayList<NoticeSelection> noticeSelectionList;

    public NoticeListItem() {}

    public NoticeListItem(final long id, final String date, final String subject, final int lock, ArrayList<NoticeSelection> noticeSelectionList) {
        this.nid = id;
        this.date = date;
        this.subject = subject;
        this.lock = Lock.getEnum(lock);
        this.noticeSelectionList = noticeSelectionList;
    }

    public NoticeListItem(final Cursor cursor) {
        this.nid = cursor.getLong(cursor.getColumnIndex(SQL.NoticeListItem.ID));
        this.date = cursor.getString(cursor.getColumnIndex(SQL.NoticeListItem.DATE));
        this.subject = cursor.getString(cursor.getColumnIndex(SQL.NoticeListItem.SUBJECT));
        this.lock = Lock.getEnum((cursor.getInt(cursor.getColumnIndex(SQL.NoticeListItem.LOCK))));
        this.noticeSelectionList = new ArrayList<NoticeSelection>();
    }

    public NoticeListItem(final Parcel source) {
        readFromParcel(source);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(final Parcel source) {
        this.noticeSelectionList = new ArrayList<NoticeSelection>();

        this.nid = source.readLong();
        this.date = source.readString();
        this.subject = source.readString();
        this.lock = Lock.getEnum(source.readInt());
        source.readTypedList(noticeSelectionList, NoticeSelection.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, final int flags) {
        dest.writeLong(this.nid);
        dest.writeString(this.date);
        dest.writeString(this.subject);
        dest.writeInt(this.lock.getType());
        dest.writeTypedList(this.noticeSelectionList);
    }

    public static final Parcelable.Creator<NoticeListItem> CREATOR = new Parcelable.Creator<NoticeListItem>() {

        @Override
        public NoticeListItem createFromParcel(Parcel source) {
            return new NoticeListItem(source);
        }

        @Override
        public NoticeListItem[] newArray(int size) {
            return new NoticeListItem[size];
        }
    };

    public String getContent() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < noticeSelectionList.size(); i++) {
            sb.append(noticeSelectionList.get(i).section);
        }
        return (!sb.toString().equalsIgnoreCase("") ? sb.toString() : "");
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("NoticeListItem( ");
        sb.append("id:" + nid);
        sb.append(", ");
        sb.append("date:" + date);
        sb.append(", ");
        sb.append("subject:" + subject);
        sb.append(", ");
        sb.append("lock:" + lock);
        sb.append(", ");
        sb.append("noticeSelectionList:" + noticeSelectionList.toString());
        sb.append(" )");

        return sb.toString();
    }

}

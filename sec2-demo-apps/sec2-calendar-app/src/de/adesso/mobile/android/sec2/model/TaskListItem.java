package de.adesso.mobile.android.sec2.model;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * TaskListItem
 * 
 * model class to store all tasks that a user creates or has
 * 
 * @author benner
 */
public class TaskListItem implements Parcelable {

    @SuppressWarnings ("unused")
    private static final Class<?> c = TaskListItem.class;

    @XStreamAlias ("nid")
    public long nid;
    @XStreamAlias ("date")
    public String date;
    @XStreamAlias ("subject")
    public String subject;
    @XStreamAlias ("lock")
    public Lock lock;
    @XStreamAlias ("priority")
    public Priority priority;
    @XStreamAlias ("content")
    public String content;
    @XStreamAlias ("checked")
    public Boolean isChecked;
    @XStreamImplicit (itemFieldName = "taskSelection")
    public List<TaskSelection> taskSelectionList;

    
    
       // Constructor
    public TaskListItem(final long id, final String date, final String subject, final int lock,final int priority,boolean isChecked,final String content) {
        this.nid = nid;
        this.date = date;
        this.subject = subject;
        this.lock = Lock.getEnum(lock);
        this.priority = Priority.getEnum(priority);
        this.content = content;
        this.isChecked = isChecked;
        this.taskSelectionList = new ArrayList<TaskSelection>();
    }

    // Constructor
    public TaskListItem(final Cursor cursor) {
    	//TODO auf Task umstellen
//    	
//        this.id = cursor.getLong(cursor.getColumnIndex(SQL.NoticeListItem.ID));
//        this.date = cursor.getString(cursor.getColumnIndex(SQL.NoticeListItem.DATE));
//        this.subject = cursor.getString(cursor.getColumnIndex(SQL.NoticeListItem.SUBJECT));
//        this.lock = Lock.getEnum((cursor.getInt(cursor.getColumnIndex(SQL.NoticeListItem.LOCK))));
//        this.content = cursor.getString(cursor.getColumnIndex(SQL.NoticeListItem.CONTENT));
//        this.taskSelectionList = new ArrayList<TaskSelection>();
    }

    // Constructor
    public TaskListItem(final Parcel source) {
        readFromParcel(source);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(final Parcel source) {
        this.taskSelectionList = new ArrayList<TaskSelection>();

        this.nid = source.readLong();
        this.date = source.readString();
        this.subject = source.readString();
        this.priority = Priority.getEnum(source.readInt());
        this.isChecked = source.readInt() == 0?false:true;
        this.lock = Lock.getEnum(source.readInt());
        this.content = source.readString();
        source.readTypedList(taskSelectionList, TaskSelection.CREATOR);

    }

    @Override
    public void writeToParcel(Parcel dest, final int flags) {
        dest.writeLong(this.nid);
        dest.writeString(this.date);
        dest.writeString(this.subject);
        dest.writeInt(this.lock.getType());
        dest.writeInt(this.priority.getType());
        dest.writeInt(this.isChecked == true?1:0);
        dest.writeString(this.content);
        dest.writeTypedList(this.taskSelectionList);
    }

    public static final Parcelable.Creator<TaskListItem> CREATOR = new Parcelable.Creator<TaskListItem>() {

        @Override
        public TaskListItem createFromParcel(Parcel source) {
            return new TaskListItem(source);
        }

        @Override
        public TaskListItem[] newArray(int size) {
            return new TaskListItem[size];
        }
    };



	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TaskListItem [id=");
		builder.append(nid);
		builder.append(", date=");
		builder.append(date);
		builder.append(", subject=");
		builder.append(subject);
		builder.append(", lock=");
		builder.append(lock);
		builder.append(", priority=");
		builder.append(priority);
		builder.append(", content=");
		builder.append(content);
		builder.append(", isChecked=");
		builder.append(isChecked);
		builder.append(", taskSelectionList=");
		builder.append(taskSelectionList);
		builder.append("]");
		return builder.toString();
	}

  

}

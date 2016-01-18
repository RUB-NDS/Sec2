/**
 * 
 */
package de.adesso.mobile.android.sec2.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import de.adesso.mobile.android.sec2.db.SQL;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author benner
 *
 */
public class TaskSelection implements Parcelable{
	
	@SuppressWarnings ("unused")
    private static final Class<?> c = TaskSelection.class;

    @XStreamAlias ("id")
    public long id;
    @XStreamAlias ("start")
    public int start;
    @XStreamAlias ("end")
    public int end;
  	@XStreamAlias ("taskId")
    public long taskId;
		
	/**
	 * @param id
	 * @param start
	 * @param end
	 * @param taskId
	 */
	public TaskSelection(long id, int start, int end, long taskId) {
		super();
		this.id = id;
		this.start = start;
		this.end = end;
		this.taskId = taskId;
	}
	
	 // Constructor
    public TaskSelection(final Cursor cursor) {
    	//TODO auf TaskSelection umstellen
//        this.id = cursor.getLong(cursor.getColumnIndex(SQL.NoticeSelection.ID));
//        this.start = cursor.getInt(cursor.getColumnIndex(SQL.NoticeSelection.START));
//        this.end = cursor.getInt(cursor.getColumnIndex(SQL.NoticeSelection.END));
//        this.taskId = cursor.getLong(cursor.getColumnIndex(SQL.NoticeSelection.ID_NOTICE));
    }

 // Constructor
    public TaskSelection(final Parcel source) {
        readFromParcel(source);
    }


	@Override
	public int describeContents() {
		return 0;
	}
	
	public void readFromParcel(final Parcel source) {
        this.id = source.readLong();
        this.start = source.readInt();
        this.end = source.readInt();
        this.taskId = source.readLong();
    }
	
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		 dest.writeLong(this.id);
	     dest.writeInt(this.start);
	     dest.writeInt(this.end);
	     dest.writeLong(this.taskId);
		
	}
	
	public static final Parcelable.Creator<TaskSelection> CREATOR = new Parcelable.Creator<TaskSelection>() {

        @Override
        public TaskSelection createFromParcel(Parcel source) {
            return new TaskSelection(source);
        }

        @Override
        public TaskSelection[] newArray(int size) {
            return new TaskSelection[size];
        }
    };


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TaskSelection [id=");
		builder.append(id);
		builder.append(", start=");
		builder.append(start);
		builder.append(", end=");
		builder.append(end);
		builder.append(", taskId=");
		builder.append(taskId);
		builder.append("]");
		return builder.toString();
	}
	
		
}

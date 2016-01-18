package de.adesso.mobile.android.sec2.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import de.adesso.mobile.android.sec2.util.LogHelper;

/**
 * A helper class to manage database creation and version management.
 * 
 * @author hoppe
 */
public class Sec2Database extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sec2.db";
    private static final int DATABASE_VERSION = 5;

    // Constructor
    public Sec2Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LogHelper.logV(this, "onCreate");
        db.execSQL(SQL.NoticeListItem.CREATE);
        db.execSQL(SQL.NoticeSelection.CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogHelper.logV(this, "onUpgrade");
        db.execSQL(SQL.NoticeListItem.DROP);
        db.execSQL(SQL.NoticeSelection.DROP);
        onCreate(db);
    }

}

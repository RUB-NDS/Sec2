
package de.adesso.mobile.android.sec2.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import de.adesso.mobile.android.sec2.debug.Debug;
import de.adesso.mobile.android.sec2.util.LogHelper;

/**
 * A helper class to manage database creation and version management.
 * 
 * @author hoppe
 */
class Sec2Database extends SQLiteOpenHelper {

    private final static String DATABASE_PATH = Debug.DB_DUMP_ENABLED ? Environment
            .getExternalStorageDirectory() + "/sec2-database/" : "";
    private static final String DATABASE_NAME = "sec2.db";
    private static final String DATABASE = DATABASE_PATH + DATABASE_NAME;

    private static final int DATABASE_VERSION = 14;

    // Constructor
    public Sec2Database(final Context context) {
        super(context, DATABASE, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        LogHelper.logV(this, "onCreate");
        // db.execSQL(SQL.NoticesTable.CREATE);
        // db.execSQL(SQL.EventsTable.CREATE);
        // db.execSQL(SQL.TasksTable.CREATE);
        db.execSQL(SQL.FilesTable.CREATE);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        LogHelper.logV(this, "onUpgrade");
        switch (oldVersion) {
            case 6:
                db.execSQL(SQL.EventsTable.CREATE);
                break;
            case 7:
                db.execSQL(SQL.TasksTable.CREATE);
                break;
            case 8:
                db.execSQL(SQL.EventsTable.DROP);
                db.execSQL(SQL.EventsTable.CREATE);
                break;
            default:
                // db.execSQL(SQL.NoticesTable.DROP);
                // db.execSQL(SQL.EventsTable.DROP);
                // db.execSQL(SQL.TasksTable.DROP);
                db.execSQL(SQL.FilesTable.DROP);
                onCreate(db);
        }
    }

}

package de.adesso.mobile.android.sec2.db;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import de.adesso.mobile.android.sec2.model.NoticeListItem;

/**
 * Collection of methods for database deletes.
 * @author hoppe
 */
public abstract class Deleter {

    @SuppressWarnings ("unused")
    private static final Class<?> c = Deleter.class;

    /**
     * deleteNotice
     * 
     * delete a single NoticeListItem with the provided id
     */
    public static void deleteNotice(NoticeListItem notice, SQLiteDatabase con) throws SQLException {

        SQLiteStatement stmt = con.compileStatement(SQL.NoticeListItem.DELETE);
        stmt.bindLong(1, notice.nid);
        stmt.execute();
    }

    /**
     * deleteNoticeSelectionAll
     * 
     * delete all NoticeSelections of a notice connected by the id of the NoticeListItem
     */
    public static void deleteNoticeSelectionAll(NoticeListItem notice, SQLiteDatabase con) throws SQLException {

        SQLiteStatement stmt = con.compileStatement(SQL.NoticeSelection.DELETE);
        stmt.bindLong(1, notice.nid);
        stmt.execute();
    }
}

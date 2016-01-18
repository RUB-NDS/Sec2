package de.adesso.mobile.android.sec2.db;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import de.adesso.mobile.android.sec2.model.NoticeListItem;
import de.adesso.mobile.android.sec2.model.NoticeSelection;

/**
 * Collection of methods for database inserts.
 * @author hoppe
 */
public abstract class Inserter {

    @SuppressWarnings ("unused")
    private static final Class<?> c = Inserter.class;

    /**
     * insertNotice
     * 
     * insert a single NoticeListItem into the database. return the primary key which is auto generated
     */
    public static long insertNotice(NoticeListItem notice, SQLiteDatabase con) throws SQLException {

        SQLiteStatement stmt = con.compileStatement(SQL.NoticeListItem.INSERT);

        // ignore id when inserting
        stmt.bindString(1, notice.date);
        stmt.bindString(2, notice.subject);
        stmt.bindLong(3, notice.lock.getType());
        //        stmt.bindString(4, notice.content);

        // right now the id has changed and we are not updated yet
        return stmt.executeInsert();
    }

    /**
     * insertNoticeSelection
     * 
     * insert a single NoticeSelection into the database. return the primary key which is auto generated
     */
    public static long insertNoticeSelection(NoticeSelection noticeSelection, SQLiteDatabase con) throws SQLException {

        SQLiteStatement stmt = con.compileStatement(SQL.NoticeSelection.INSERT);

        // ignore id when inserting
        //        stmt.bindLong(1, noticeSelection.start);
        //        stmt.bindLong(2, noticeSelection.end);
        //        stmt.bindLong(3, noticeSelection.noticeId);

        // right now the id has changed and we are not updated yet
        return stmt.executeInsert();
    }

    /**
     * insertNoticeSelectionList
     * 
     * insert a list of NoticeSelections into the database. return the primary key which is auto generated
     */
    public static long insertNoticeSelectionList(NoticeListItem notice, SQLiteDatabase con) throws SQLException {
        long rc = -1;
        for (NoticeSelection noticeSelection : notice.noticeSelectionList) {
            //            noticeSelection.noticeId = notice.nid;
            rc = insertNoticeSelection(noticeSelection, con);
        }
        return rc;
    }
}

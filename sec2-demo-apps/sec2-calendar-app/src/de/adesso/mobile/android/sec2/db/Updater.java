package de.adesso.mobile.android.sec2.db;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import de.adesso.mobile.android.sec2.model.NoticeListItem;

/**
 * Collection of methods for database updates.
 * 
 * @author hoppe
 */
public abstract class Updater {

    /**
     * updateNotice
     * 
     * update a single NoticeListItem specified by the id.
     */

    public static void updateNotice(NoticeListItem notice, SQLiteDatabase con) throws SQLException {

        SQLiteStatement stmt = con.compileStatement(SQL.NoticeListItem.UPDATE);

        stmt.bindString(1, notice.date);
        stmt.bindString(2, notice.subject);
        stmt.bindLong(3, notice.lock.getType());
        //        stmt.bindString(4, notice.content);
        //        stmt.bindLong(5, notice.nid);
        stmt.bindLong(4, notice.nid);

        stmt.execute();
    }

    /**
     * updateNoticeSelection
     * 
     * update a single NoticeSelection specified by the id.
     * 
     * Note: currently not used
     */

    //    public static void updateNoticeSelection(NoticeSelection noticeSelection, SQLiteDatabase con) throws SQLException {
    //
    //        SQLiteStatement stmt = con.compileStatement(SQL.NoticeSelection.UPDATE);
    //
    //        stmt.bindLong(1, noticeSelection.start);
    //        stmt.bindLong(2, noticeSelection.end);
    //        stmt.bindLong(3, noticeSelection.noticeId);
    //        stmt.bindLong(4, noticeSelection.id);
    //
    //        stmt.execute();
    //    }

    /**
     * updateNoticeSelectionList
     * 
     * update all NoticeSelection specififed by the id
     * 
     * Note: currently not used
     */

    //    public static void updateNoticeSelectionList(List<NoticeSelection> noticeSelectionList, SQLiteDatabase con) throws SQLException {
    //
    //        for (NoticeSelection noticeSelection : noticeSelectionList) {
    //            updateNoticeSelection(noticeSelection, con);
    //        }
    //    }

}

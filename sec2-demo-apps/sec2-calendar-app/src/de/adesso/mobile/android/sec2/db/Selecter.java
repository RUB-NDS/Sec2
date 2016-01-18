package de.adesso.mobile.android.sec2.db;

import java.util.ArrayList;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;
import de.adesso.mobile.android.sec2.model.NoticeListItem;

/**
 * Collection of methods for database selects.
 * @author hoppe
 */
public abstract class Selecter {

    /**
     * selectNotice
     * 
     * select a single NoticeListItem from database and return it
     * 
     * Note: Currently not needed.
     */
    //    public static NoticeListItem selectNotice(Cursor cursor) {
    //        long id = cursor.getLong(cursor.getColumnIndex(SQL.NoticeListItem.ID));
    //        String date = cursor.getString(cursor.getColumnIndex(SQL.NoticeListItem.DATE));
    //        String subject = cursor.getString(cursor.getColumnIndex(SQL.NoticeListItem.SUBJECT));
    //        int lock = cursor.getInt(cursor.getColumnIndex(SQL.NoticeListItem.LOCK));
    //        String content = cursor.getString(cursor.getColumnIndex(SQL.NoticeListItem.CONTENT));
    //
    //        return new NoticeListItem(id, date, subject, lock, content);
    //    }

    /**
     * selectNoticeList
     * 
     * select all NoticeListItem with its selections from database and return them
     */

    public static List<NoticeListItem> selectNoticeList(SQLiteDatabase con) {
        List<NoticeListItem> noticeList = new ArrayList<NoticeListItem>();
        //        Cursor cursor = null;
        //        long pk = 0;
        //        NoticeListItem nli = null;
        //        try {
        //            cursor = con.rawQuery(SQL.NoticeJoinSelect.SELECT, null);
        //            if (cursor.getCount() > 0) {
        //
        //                while (cursor.moveToNext()) {
        //
        //                    final long id = cursor.getLong(cursor.getColumnIndex(SQL.NoticeListItem.ID));
        //
        //                    if (pk == 0) {
        //                        pk = id;
        //                        nli = new NoticeListItem(cursor);
        //                        final long fk = cursor.getLong(cursor.getColumnIndex(SQL.NoticeSelection.ID));
        //                        if (fk != 0) {
        //                            final NoticeSelection ns = new NoticeSelection(cursor);
        //                            nli.noticeSelectionList.add(ns);
        //                        }
        //                    } else {
        //                        if (pk == id) {
        //                            final long fk = cursor.getLong(cursor.getColumnIndex(SQL.NoticeSelection.ID));
        //                            if (fk != 0) {
        //                                final NoticeSelection ns = new NoticeSelection(cursor);
        //                                nli.noticeSelectionList.add(ns);
        //                            }
        //                        } else {
        //                            noticeList.add(nli);
        //                            pk = id;
        //                            nli = new NoticeListItem(cursor);
        //                            final long fk = cursor.getLong(cursor.getColumnIndex(SQL.NoticeSelection.ID));
        //                            if (fk != 0) {
        //                                final NoticeSelection ns = new NoticeSelection(cursor);
        //                                nli.noticeSelectionList.add(ns);
        //                            }
        //                        }
        //                    }
        //                }
        //                noticeList.add(nli);
        //            }
        //        } finally {
        //            try {
        //                cursor.close();
        //            } catch (Exception ignored) {}
        //        }
        return noticeList;
    }

    /**
     * selectNoticeSelection
     * 
     * select a single NoticeSelection from database and return it
     * 
     * Note: Currently not needed.
     */

    //    public static NoticeSelection selectNoticeSelection(Cursor cursor) {
    //        long id = cursor.getLong(cursor.getColumnIndex(SQL.NoticeSelection.ID));
    //        int start = cursor.getInt(cursor.getColumnIndex(SQL.NoticeSelection.START));
    //        int end = cursor.getInt(cursor.getColumnIndex(SQL.NoticeSelection.END));
    //        long idnotice = cursor.getLong(cursor.getColumnIndex(SQL.NoticeSelection.ID_NOTICE));
    //
    //        return new NoticeSelection(id, start, end, idnotice);
    //    }

    /**
     * selectNoticeSelectionList
     * 
     * select all NoticeSelection from database and return them
     * 
     * Note: Currently not needed.
     */

    //    public static List<NoticeSelection> selectNoticeSelectionList(SQLiteDatabase con) {
    //
    //        List<NoticeSelection> noticeSelectionList = new ArrayList<NoticeSelection>();
    //
    //        Cursor cursor = null;
    //        try {
    //            cursor = con.rawQuery(SQL.NoticeSelection.SELECT, null);
    //            while (cursor.moveToNext()) {
    //                noticeSelectionList.add(selectNoticeSelection(cursor));
    //            }
    //        } finally {
    //            try {
    //                cursor.close();
    //            } catch (Exception ignored) {}
    //        }
    //        return noticeSelectionList;
    //    }

}

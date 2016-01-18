package de.adesso.mobile.android.sec2.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

/**
 * This class handles the database functionality for notices
 * @author schuessler
 *
 */
public class NoticesDbHandler extends DefaultDbHandler {

    private final SQLiteStatement INSERT;

    /**
     * Constructor
     * 
     * @param context - The context
     * @param writeable - Whether the database is to open writeable
     */
    public NoticesDbHandler(final Context context, final boolean writeable) {
        super(context, writeable);
        INSERT = DB.compileStatement(SQL.NoticesTable.INSERT);
    }

    /**
     * Saves the file name of a notice in the database.
     * 
     * @param name - The file name
     * @return TRUE, if insertion was successful, FALSE otherwise
     */
    public boolean saveNoticeName(final String name) {
        INSERT.bindString(1, name);
        return INSERT.executeInsert() != -1;
    }

    /**
     * Returns the file names of all notices, which are stored in the database.
     * 
     * @return A list with the file names of all notices.
     */
    public List<String> getNoticeNames() {
        final Cursor result = DB.rawQuery(SQL.NoticesTable.SELECT_FILE_NAME, null);
        final ArrayList<String> names = new ArrayList<String>(result.getCount());

        while (result.moveToNext()) {
            names.add(result.getString(0));
        }

        return names;
    }

    /**
     * Deletes the file name of a notice from the database.
     * 
     * @param name - The file name to be deleted
     * @return number of file names deleted. Should be 1 in normal
     */
    public int deleteNoticeName(final String name) {
        return DB.delete(SQL.NoticesTable.TABLENAME, SQL.NoticesTable.COLUMN_FILE_NAME + "=?",
                new String[] { name });
    }
}

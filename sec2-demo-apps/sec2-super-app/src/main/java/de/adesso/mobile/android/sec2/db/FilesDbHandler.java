package de.adesso.mobile.android.sec2.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

/**
 * This class handles the database functionality for events
 * 
 * @author hoppe
 */
public class FilesDbHandler extends DefaultDbHandler {

    private final SQLiteStatement INSERT;

    /**
     * Constructor
     * 
     * @param context - The context
     * @param writeable - Whether the database is to open writeable
     */
    public FilesDbHandler(final Context context, final boolean writeable) {
        super(context, writeable);
        INSERT = DB.compileStatement(SQL.FilesTable.INSERT);
    }

    /**
     * Saves the file name of an event in the database.
     * 
     * @param name - The file name
     * @return TRUE, if insertion was successful, FALSE otherwise
     */
    public boolean saveFileName(final String name) {
        INSERT.bindString(1, name);
        return INSERT.executeInsert() != -1;
    }

    /**
     * Returns the file names of all events, which are stored in the database.
     * 
     * @return A list with the file names of all events.
     */
    public List<String> getFileNames() {
        final Cursor result = DB.rawQuery(SQL.FilesTable.SELECT_FILE_NAME, null);
        final ArrayList<String> names = new ArrayList<String>(result.getCount());

        while (result.moveToNext()) {
            names.add(result.getString(0));
        }
        result.close();
        return names;
    }

    /**
     * Returns the file names of all events, which are stored in the database.
     * 
     * @return A list with the file names of all events.
     */
    public boolean fileExists(final String fileName) {
        final Cursor result = DB.rawQuery(SQL.FilesTable.SELECT_FILE_EXISTS,
                new String[] { fileName });
        final boolean returnValue = result.getCount() > 0 ? true : false;
        result.close();
        return returnValue;
    }

    /**
     * Deletes the file name of an event from the database.
     * 
     * @param name - The file name to be deleted
     * @return number of file names deleted. Should be 1 in normal
     */
    public int deleteFileName(final String name) {
        return DB.delete(SQL.FilesTable.TABLENAME, SQL.FilesTable.COLUMN_FILE_NAME + "=?",
                new String[] { name });
    }
}

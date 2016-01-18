package de.adesso.mobile.android.sec2.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

/**
 * This class handles the database functionality for events
 * 
 * @author schuessler
 */
public class EventsDbHandler extends DefaultDbHandler {

    private final SQLiteStatement INSERT;

    /**
     * Constructor
     * 
     * @param context - The context
     * @param writeable - Whether the database is to open writeable
     */
    public EventsDbHandler(final Context context, final boolean writeable) {
        super(context, writeable);
        INSERT = DB.compileStatement(SQL.EventsTable.INSERT);
    }

    /**
     * Saves the file name of an event in the database.
     * 
     * @param name - The file name
     * @return TRUE, if insertion was successful, FALSE otherwise
     */
    public boolean saveEventName(final String name) {
        INSERT.bindString(1, name);
        return INSERT.executeInsert() != -1;
    }

    /**
     * Returns the file names of all events, which are stored in the database.
     * 
     * @return A list with the file names of all events.
     */
    public List<String> getEventNames() {
        final Cursor result = DB.rawQuery(SQL.EventsTable.SELECT_FILE_NAME, null);
        final ArrayList<String> names = new ArrayList<String>(result.getCount());

        while (result.moveToNext()) {
            names.add(result.getString(0));
        }

        return names;
    }

    /**
     * Deletes the file name of an event from the database.
     * 
     * @param name - The file name to be deleted
     * @return number of file names deleted. Should be 1 in normal
     */
    public int deleteEventName(final String name) {
        return DB.delete(SQL.EventsTable.TABLENAME, SQL.EventsTable.COLUMN_FILE_NAME + "=?",
                new String[] { name });
    }
}

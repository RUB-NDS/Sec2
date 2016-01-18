package de.adesso.mobile.android.sec2.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

/**
 * This class handles the database functionality for tasks
 * 
 * @author hoppe
 */
public class TasksDbHandler extends DefaultDbHandler {

    private final SQLiteStatement INSERT;

    /**
     * Constructor
     * 
     * @param context - The context
     * @param writeable - Whether the database is to open writeable
     */
    public TasksDbHandler(final Context context, final boolean writeable) {
        super(context, writeable);
        INSERT = DB.compileStatement(SQL.TasksTable.INSERT);
    }

    /**
     * Saves the file name of a task in the database.
     * 
     * @param name - The file name
     * @return TRUE, if insertion was successful, FALSE otherwise
     */
    public boolean saveTaskName(final String name) {
        INSERT.bindString(1, name);
        return INSERT.executeInsert() != -1;
    }

    /**
     * Returns the file names of all tasks, which are stored in the database.
     * 
     * @return A list with the file names of all tasks.
     */
    public List<String> getTaskNames() {
        final Cursor result = DB.rawQuery(SQL.TasksTable.SELECT_FILE_NAME, null);
        final ArrayList<String> names = new ArrayList<String>(result.getCount());

        while (result.moveToNext()) {
            names.add(result.getString(0));
        }

        return names;
    }

    /**
     * Deletes the file name of a task from the database.
     * 
     * @param name - The file name to be deleted
     * @return number of file names deleted. Should be 1 in normal
     */
    public int deleteTaskName(final String name) {
        return DB.delete(SQL.TasksTable.TABLENAME, SQL.TasksTable.COLUMN_FILE_NAME + "=?",
                new String[] { name });
    }
}

package de.adesso.mobile.android.sec2.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * This class handles the default database functionality. This is: opening a
 * database writable or readable and closing the database.
 * 
 * @author schuessler
 */
public class DefaultDbHandler {

    protected final SQLiteDatabase DB;

    /**
     * Constructor
     * 
     * @param context - The context
     * @param writeable - Whether the database is to open writeable
     */
    public DefaultDbHandler(final Context context, final boolean writeable) {
        final Sec2Database dbOpener = new Sec2Database(context);

        if (writeable) {
            DB = dbOpener.getWritableDatabase();
        } else {
            DB = dbOpener.getReadableDatabase();
        }
    }

    /**
     * Closes the database connection.
     */
    public void close() {
        DB.close();
    }

    @Override
    protected void finalize() {
        DB.close();
    }
}

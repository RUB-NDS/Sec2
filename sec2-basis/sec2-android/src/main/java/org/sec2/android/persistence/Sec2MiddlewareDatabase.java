package org.sec2.android.persistence;

import java.util.ArrayList;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import net.sqlcipher.database.SQLiteStatement;

import org.sec2.android.exceptions.Sec2MwPersistenceRuntimeException;

import android.content.Context;
import android.database.Cursor;
import de.adesso.mobile.android.sec2.mwadapter.logging.LogHelper;

/**
 * A helper class to manage database creation and version management. The class
 * is considered to be only visible within its package.
 *
 * @author schuessler
 */
class Sec2MiddlewareDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sec2.middleware.db";
    private static final int DATABASE_VERSION = 2;

    /**
     * The constructor of this class. Attention: Be sure that the method
     * "SQLiteDatabase.loadLibs()" was called <b>before</b> the first calling
     * of this constructor.
     *
     * @param context - The context where the DB-operations take place
     */
    public Sec2MiddlewareDatabase(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db)
    {
        LogHelper.logV(this, "onCreate");
        db.execSQL(SqlConstants.Keys.CREATE);
        db.execSQL(SqlConstants.Nonces.CREATE);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
            final int newVersion)
    {
        Cursor result = null;
        ArrayList<TableRow> rows = null;
        SQLiteStatement insert = null;
        TableRow row = null;

        LogHelper.logV(this, "onUpgrade");
        if (oldVersion < 1)
        {
            //save entries in the db
            result = db.rawQuery(SqlConstants.Keys.SELECT_ALL_WITHOUT_ID,
                    null);
            rows = new ArrayList<TableRow>(result.getCount());
            insert = db.compileStatement(SqlConstants.Keys.INSERT);
            while (result.moveToNext())
            {
                row = new TableRow();
                row.setAppName(result.getString(0));
                row.setKeyValue(result.getString(1));
                row.setKeyAlgorithm(result.getString(2));
                row.setKeyFormat(result.getString(3));
                rows.add(row);
            }
            //Drop table
            db.execSQL(SqlConstants.Keys.DROP);
            //Create new table
            db.execSQL(SqlConstants.Keys.CREATE);
            //Fill table with values
            for (int i = 0; i < rows.size(); i++)
            {
                row = rows.get(i);
                insert.bindString(1, row.getAppName());
                insert.bindString(2, row.getKeyValue());
                insert.bindString(3, row.getKeyAlgorithm());
                insert.bindString(4, row.getKeyFormat());
                if (insert.executeInsert() == -1)
                {
                    throw new Sec2MwPersistenceRuntimeException("Daten konnte "
                            + "nicht vollständig wieder hergestellt werden "
                            + "nach DB-Schema-Aktualisierung!");
                }
            }
        }
        if (oldVersion < 2)
        {
            db.execSQL(SqlConstants.Nonces.CREATE);
        }
    }

    private class TableRow
    {
        private String appName;
        private String keyValue;
        private String keyAlgorithm;
        private String keyFormat;

        public String getAppName()
        {
            return appName;
        }

        public void setAppName(final String appName)
        {
            this.appName = appName;
        }

        public String getKeyValue()
        {
            return keyValue;
        }

        public void setKeyValue(final String keyValue)
        {
            this.keyValue = keyValue;
        }

        public String getKeyAlgorithm()
        {
            return keyAlgorithm;
        }

        public void setKeyAlgorithm(final String keyAlgorithm)
        {
            this.keyAlgorithm = keyAlgorithm;
        }

        public String getKeyFormat()
        {
            return keyFormat;
        }

        public void setKeyFormat(final String keyFormat)
        {
            this.keyFormat = keyFormat;
        }
    }
}

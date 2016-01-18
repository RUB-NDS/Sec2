package org.sec2.android.persistence;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.LinkedList;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import net.sqlcipher.DatabaseUtils;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteStatement;

import org.sec2.android.exceptions.Sec2MwPersistenceRuntimeException;
import org.sec2.android.util.CryptoUtils;
import org.sec2.persistence.IPersistenceManager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

/**
 * This class provides access to the embedded SQLite-DB, holding the
 * app-authentication-keys of the apps and the nonces.
 * 
 * @author nike
 */
public class DbPersistenceManager implements IPersistenceManager
{
    private final Context context;
    private final String dbKey;
    private static final String ERROR_APP_SAVE = "\"App Authentication Key\" "
            + "konnte nicht gespeichert werden: Variable \"{0}\" darf "
            + "nicht {1} sein!";
    private static final String ERROR_APP_LOAD = "\"App Authentication Key\" "
            + "konnte nicht geladen werden: Spalte \"{0}\" war leer oder "
            + "NULL!";
    private static final String ERROR_NONCE_SAVE = "Nonce konnte nicht "
            + "gespeichert werden: Variable \"nonce\" darf nicht NULL sein!";
    private static boolean libsLoaded = false;

    /**
     * Constructor for the DbPersistenceManager.
     *
     * @param context - The context
     * @param dbKey - The key to open the encrypted database
     */
    public DbPersistenceManager(final Context context, final String dbKey)
    {
        this.context = context;
        this.dbKey = dbKey;
        //Check, if needed libs are loaded
        if (!libsLoaded)
        {
            SQLiteDatabase.loadLibs(context);
            libsLoaded = true;
        }
    }

    /* (non-Javadoc)
     * @see org.sec2.persistence.IPersistenceManager#saveAppAuthKey(
     * javax.crypto.SecretKey, java.lang.String)
     */
    @Override
    public boolean saveAppAuthKey(final SecretKey key, final String appName)
    {
        CryptoUtils cryptoUtils = null;
        Sec2MiddlewareDatabase dbOpener = null;
        SQLiteStatement insert = null;
        SQLiteStatement count = null;
        SQLiteDatabase db = null;
        ContentValues columnValues = null;
        long result = -1;

        if (key == null)
        {
            throw new Sec2MwPersistenceRuntimeException(MessageFormat.format(
                    ERROR_APP_SAVE, "key", "NULL"));
        }
        if (appName == null || appName.isEmpty())
        {
            throw new Sec2MwPersistenceRuntimeException(MessageFormat.format(
                    ERROR_APP_SAVE, "appName", "NULL oder leer"));
        }
        cryptoUtils = new CryptoUtils();
        dbOpener = new Sec2MiddlewareDatabase(context);
        db = dbOpener.getWritableDatabase(dbKey);
        //First check, if there is already an entry in the DB with the passed
        //app's name
        count = db.compileStatement(SqlConstants.Keys.COUNT_KEY_BY_APP_NAME);
        count.bindString(1, appName);
        db.beginTransaction();
        try
        {
            //If no entry already existed, insert new app authentication key
            if (count.simpleQueryForLong() == 0)
            {
                insert = db.compileStatement(SqlConstants.Keys.INSERT);
                insert.bindString(1, appName);
                insert.bindString(2, cryptoUtils.encodeSecretKeyAsHex(key,
                        true));
                insert.bindString(3, key.getAlgorithm());
                insert.bindString(4, key.getFormat());
                result = insert.executeInsert();
            }
            //Otherwise update app authentication key
            else
            {
                columnValues = new ContentValues(3);
                columnValues.put(SqlConstants.Keys.COLUMN_KEY,
                        cryptoUtils.encodeSecretKeyAsHex(key, true));
                columnValues.put(SqlConstants.Keys.COLUMN_ALGORITHM,
                        key.getAlgorithm());
                columnValues.put(SqlConstants.Keys.COLUMN_FORMAT,
                        key.getFormat());
                result = db.update(SqlConstants.Keys.TABLENAME, columnValues,
                        SqlConstants.Keys.COLUMN_APP_NAME + " = ?",
                        new String[]{appName});
            }
            db.setTransactionSuccessful();
        }
        finally
        {
            db.endTransaction();
            db.close();
        }

        return result != -1;
    }

    /* (non-Javadoc)
     * @see org.sec2.persistence.IPersistenceManager#getAppAuthKey(
     * java.lang.String)
     */
    @Override
    public SecretKey getAppAuthKey(final String appName)
    {
        final Sec2MiddlewareDatabase dbOpener =
                new Sec2MiddlewareDatabase(context);
        final SQLiteDatabase db = dbOpener.getWritableDatabase(dbKey);
        Cursor cursor = null;
        SecretKey result = null;
        String key = null;
        String algorithm = null;
        String format = null;
        CryptoUtils cryptoUtils = null;

        try
        {
            cursor = db.rawQuery(SqlConstants.Keys.SELECT_KEY_SPEC_BY_APP_NAME,
                    new String[]{appName});
            if (cursor.moveToFirst())
            {
                key = cursor.getString(0);
                algorithm = cursor.getString(1);
                format = cursor.getString(2);
                if (key == null || key.isEmpty())
                {
                    throw new Sec2MwPersistenceRuntimeException(
                            MessageFormat.format(ERROR_APP_LOAD, "key_value"));
                }
                if (algorithm == null || algorithm.isEmpty())
                {
                    throw new Sec2MwPersistenceRuntimeException(
                            MessageFormat.format(ERROR_APP_LOAD,
                                    "key_algorithm"));
                }
                if (format == null || format.isEmpty())
                {
                    throw new Sec2MwPersistenceRuntimeException(
                            MessageFormat.format(ERROR_APP_LOAD, "key_format"));
                }
                cryptoUtils = new CryptoUtils();
                try
                {
                    result = new SecretKeyFromDb(cryptoUtils.decodeHex(key),
                            algorithm, format);
                }
                catch (final UnsupportedEncodingException uee)
                {
                    throw new Sec2MwPersistenceRuntimeException(
                            MessageFormat.format(ERROR_APP_LOAD, "key_value"),
                            uee);
                }
            }

            return result;
        }
        finally
        {
            cursor.close();
            db.close();
        }
    }

    /* (non-Javadoc)
     * @see org.sec2.persistence.IPersistenceManager#deleteAppAuthKey(
     * java.lang.String)
     */
    @Override
    public boolean deleteAppAuthKey(final String appName)
    {
        final Sec2MiddlewareDatabase dbOpener =
                new Sec2MiddlewareDatabase(context);
        final SQLiteDatabase db = dbOpener.getWritableDatabase(dbKey);

        try
        {
            db.delete(SqlConstants.Keys.TABLENAME,
                    SqlConstants.Keys.COLUMN_APP_NAME + " = ?",
                    new String[]{appName});

            return true;
        }
        finally
        {
            db.close();
        }
    }

    /**
     * Sets the encryption-password for the SQLite-DB with SQLCipher-extension.
     *
     * @param context - The context
     * @param oldPassword - The old encryption-password
     * @param newPassword - The new encryption-password to set.
     */
    public static synchronized void setDbPassword(final Context context,
            final String oldPassword, final String newPassword)
    {
        Sec2MiddlewareDatabase dbOpener = null;
        SQLiteDatabase db = null;

        //Check, if needed libs are loaded
        if (!libsLoaded)
        {
            SQLiteDatabase.loadLibs(context);
            libsLoaded = true;
        }

        //Change DB password
        if (newPassword == null || newPassword.isEmpty())
        {
            throw new Sec2MwPersistenceRuntimeException(
                    "Variable \"newPassword\" darf nicht NULL sein!");
        }
        dbOpener = new Sec2MiddlewareDatabase(context);
        db = dbOpener.getWritableDatabase(oldPassword);

        try
        {
            //SQLiteStatment can't be used here, because it doesn't work with
            //PRAGMA command
            db.execSQL("PRAGMA rekey = "
                    + DatabaseUtils.sqlEscapeString(newPassword));
        }
        finally
        {
            db.close();
        }
    }

    @Override
    public String[] getRegisteredAppIds()
    {
        final Sec2MiddlewareDatabase dbOpener =
                new Sec2MiddlewareDatabase(context);
        final SQLiteDatabase db = dbOpener.getReadableDatabase(dbKey);
        Cursor cursor = null;
        final LinkedList<String> appIds = new LinkedList<String>();

        try
        {
            cursor = db.rawQuery(SqlConstants.Keys.SELECT_APP_NAME, null);
            if (cursor.moveToFirst())
            {
                do
                {
                    appIds.add(cursor.getString(0));
                }
                while(cursor.moveToNext());
            }
        }
        finally
        {
            db.close();
        }

        return appIds.toArray(new String[appIds.size()]);
    }

    @Override
    public boolean isNonceInDb(final String nonce)
    {
        Sec2MiddlewareDatabase dbOpener = null;
        SQLiteStatement count = null;
        SQLiteDatabase db = null;
        boolean result = false;

        if (nonce != null)
        {
            dbOpener = new Sec2MiddlewareDatabase(context);
            db = dbOpener.getReadableDatabase(dbKey);
            count = db.compileStatement(
                    SqlConstants.Nonces.COUNT_NONCE_BY_NONCE);
            count.bindString(1, nonce);
            try
            {
                result = (count.simpleQueryForLong() > 0);
            }
            finally
            {
                db.close();
            }
        }

        return result;
    }

    @Override
    public boolean saveNonceInDb(final long timestamp, final String nonce)
    {
        Sec2MiddlewareDatabase dbOpener = null;
        SQLiteStatement insert = null;
        SQLiteDatabase db = null;
        long result;

        if (nonce == null || nonce.isEmpty())
        {
            throw new Sec2MwPersistenceRuntimeException(ERROR_NONCE_SAVE);
        }
        dbOpener = new Sec2MiddlewareDatabase(context);
        db = dbOpener.getWritableDatabase(dbKey);
        insert = db.compileStatement(SqlConstants.Nonces.INSERT);
        insert.bindLong(1, timestamp);
        insert.bindString(2, nonce);
        try
        {
            result = insert.executeInsert();
        }
        finally
        {
            db.close();
        }

        return result != -1;
    }

    @Override
    public boolean deleteOldNonceInDb(final long threshold)
    {
        final Sec2MiddlewareDatabase dbOpener =
                new Sec2MiddlewareDatabase(context);
        final SQLiteDatabase db = dbOpener.getWritableDatabase(dbKey);

        try
        {
            db.delete(SqlConstants.Nonces.TABLENAME,
                    SqlConstants.Nonces.COLUMN_TIMESTAMP + " < ?",
                    new String[]{Long.valueOf(threshold).toString()});

            return true;
        }
        finally
        {
            db.close();
        }
    }

    private final class SecretKeyFromDb extends SecretKeySpec
    {
        private static final long serialVersionUID = -3569325883119680687L;
        private final String format;

        public SecretKeyFromDb(final byte[] key, final String algorithm,
                final String format)
        {
            super(key, algorithm);
            this.format = format;
        }

        @Override
        public String getFormat()
        {
            return format;
        }
    }
}

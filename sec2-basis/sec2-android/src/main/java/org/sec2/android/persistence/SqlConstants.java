package org.sec2.android.persistence;

/**
 * Collection of miscellaneous SQL queries used by the Sec2-middleware to
 * communicate with its database. The class is considered to be only visible
 * within its package.
 *
 * @author schuessler
 */
final class SqlConstants
{
    /**
     * This class contains SQL queries for table "keys".
     *
     * @author nike
     */
    static final class Keys
    {
        /**
         * The name of the DB-table.
         */
        public static final String TABLENAME = "keys";

        /**
         * The column "id".
         */
        public static final String COLUMN_ID = "id";

        /**
         * The column "app_name".
         */
        public static final String COLUMN_APP_NAME = "app_name";

        /**
         * The column "key_value".
         */
        public static final String COLUMN_KEY = "key_value";

        /**
         * The column "key_algorithm".
         */
        public static final String COLUMN_ALGORITHM = "key_algorithm";

        /**
         * The column "key_format".
         */
        public static final String COLUMN_FORMAT = "key_format";

        /**
         * Create-statement for creating the table "keys".
         *
         * CREATE TABLE keys
         * (id INTEGER PRIMARY KEY AUTOINCREMENT,
         *  app_name TEXT UNIQUE NOT NULL,
         *  key_value TEXT NOT NULL,
         *  key_algorithm TEXT NOT NULL,
         *  key_format TEXT NOT NULL)
         */
        public static final String CREATE = "CREATE TABLE " + TABLENAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_APP_NAME + " TEXT UNIQUE NOT NULL, " + COLUMN_KEY
                + " TEXT NOT NULL, " + COLUMN_ALGORITHM + " TEXT NOT NULL, "
                + COLUMN_FORMAT + " TEXT NOT NULL)";

        /**
         * Select-statement to fetch the whole content of table "keys" except
         * for column "id".
         *
         * SELECT app_name, key_value, key_algorithm, key_format FROM keys
         */
        public static final String SELECT_ALL_WITHOUT_ID = "SELECT "
                + COLUMN_APP_NAME + ", " + COLUMN_KEY + ", " + COLUMN_ALGORITHM
                + ", " + COLUMN_FORMAT + " FROM " + TABLENAME;

        /**
         * Drop-statement for dropping the table "keys" if existent.
         *
         * DROP TABLE IF EXISTS keys
         */
        public static final String DROP = "DROP TABLE IF EXISTS " + TABLENAME;

        /**
         * Insert-statement for inserting a row to the table "keys".
         *
         * INSERT INTO keys
         * (app_name,
         *  key_value,
         *  key_algorithm,
         *  key_format)
         * VALUES (?, ?, ?, ?)
         */
        public static final String INSERT = "INSERT INTO " + TABLENAME + " ("
                + COLUMN_APP_NAME + ", " + COLUMN_KEY + ", " + COLUMN_ALGORITHM
                + ", " + COLUMN_FORMAT + ") VALUES (?, ?, ?, ?)";

        /**
         * Statement for setting a new password with which the database is
         * encrypted.
         *
         * PRAGMA rekey = ?
         */
        public static final String NEW_PASSWORD = "PRAGMA rekey = ?";

        /**
         * Statement for counting the number of keys for a specific app-name.
         *
         * SELECT COUNT(key_value) FROM keys WHERE app_name = ?
         */
        public static final String COUNT_KEY_BY_APP_NAME = "SELECT COUNT("
                + COLUMN_KEY + ") FROM " + TABLENAME + " WHERE "
                + COLUMN_APP_NAME + " = ?";

        /**
         * Statement for selecting the entries of columns "key_value",
         * "key_algorithm" and "key_format" for a specific app-name.
         *
         * SELECT key_value, key_algorithm, key_format
         * FROM keys
         * WHERE app_name = ?
         */
        public static final String SELECT_KEY_SPEC_BY_APP_NAME = "SELECT "
                + COLUMN_KEY + ", " + COLUMN_ALGORITHM + ", " + COLUMN_FORMAT
                + " FROM " + TABLENAME + " WHERE " + COLUMN_APP_NAME + " = ?";

        /**
         * Statement for selecting all entries of column "app_name".
         *
         * SELECT app_name FROM keys
         */
        public static final String SELECT_APP_NAME = "SELECT "
                + COLUMN_APP_NAME + " FROM " + TABLENAME;

        //Prevent that objects are created from this class
        private Keys(){}
    }

    /**
     * This class contains SQL queries for table "nonces".
     *
     * @author nike
     */
    static final class Nonces
    {
        /**
         * The name of the DB-table.
         */
        public static final String TABLENAME = "nonces";

        /**
         * The column "id".
         */
        public static final String COLUMN_ID = "id";

        /**
         * The column "timestamp".
         */
        public static final String COLUMN_TIMESTAMP = "timestamp";

        /**
         * The column "nonce".
         */
        public static final String COLUMN_NONCE = "nonce";

        /**
         * Create-statement for creating the table "nonces".
         *
         * CREATE TABLE nonces
         * (id INTEGER PRIMARY KEY AUTOINCREMENT,
         *  timestamp INTEGER NOT NULL,
         *  nonce TEXT NOT NULL)
         */
        public static final String CREATE = "CREATE TABLE " + TABLENAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TIMESTAMP + " INTEGER NOT NULL, " + COLUMN_NONCE
                + " TEXT NOT NULL)";

        /**
         * Statement for counting the number of occurrences of a specific
         * nonce.
         *
         * SELECT COUNT(nonce) FROM nonces WHERE nonce = ?
         */
        public static final String COUNT_NONCE_BY_NONCE = "SELECT COUNT("
                + COLUMN_NONCE + ") FROM " + TABLENAME + " WHERE "
                + COLUMN_NONCE + " = ?";

        /**
         * Insert statement for inserting a row to the table "nonces".
         *
         * INSERT INTO nonces (timestamp, nonce) VALUES (?, ?)
         */
        public static final String INSERT = "INSERT INTO " + TABLENAME + " ("
                + COLUMN_TIMESTAMP + ", " + COLUMN_NONCE + ") VALUES (?, ?)";

        //Prevent that objects are created from this class
        private Nonces(){}
    }

    //Prevent that objects are created from this class
    private SqlConstants(){}
}

package de.adesso.mobile.android.sec2.db;

/**
 * Collection of SQL queries divided into several subclasses.
 * 
 * @author hoppe
 */
abstract class SQL {

    /**
     * @author hoppe
     * 
     * class to handle the database communication for a NoticeListItem
     */
    static final class NoticesTable {

        public static final String TABLENAME = "notices";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_FILE_NAME = "file_name";
        public static final String CREATE = "CREATE TABLE " + TABLENAME + " (" + COLUMN_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_FILE_NAME + " TEXT)";
        public static final String DROP = "DROP TABLE IF EXISTS " + TABLENAME;
        public static final String INSERT = "INSERT INTO " + TABLENAME + " (" + COLUMN_FILE_NAME
                + ") VALUES (?)";
        public static final String SELECT_ALL = "SELECT " + COLUMN_ID + ", " + COLUMN_FILE_NAME
                + " FROM " + TABLENAME;
        public static final String SELECT_FILE_NAME = "SELECT DISTINCT " + COLUMN_FILE_NAME
                + " FROM " + TABLENAME;

    }

    /**
     * @author hoppe
     * 
     * class to handle the database communication for an Event
     */
    static final class TasksTable {

        public static final String TABLENAME = "tasks";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_FILE_NAME = "file_name";
        public static final String CREATE = "CREATE TABLE " + TABLENAME + " (" + COLUMN_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_FILE_NAME + " TEXT)";
        public static final String DROP = "DROP TABLE IF EXISTS " + TABLENAME;
        public static final String INSERT = "INSERT INTO " + TABLENAME + " (" + COLUMN_FILE_NAME
                + ") VALUES (?)";
        public static final String SELECT_ALL = "SELECT " + COLUMN_ID + ", " + COLUMN_FILE_NAME
                + " FROM " + TABLENAME;
        public static final String SELECT_FILE_NAME = "SELECT DISTINCT " + COLUMN_FILE_NAME
                + " FROM " + TABLENAME;
    }

    /**
     * @author schuessler
     * 
     * class to handle the database communication for an Event
     */
    static final class EventsTable {

        public static final String TABLENAME = "events";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_FILE_NAME = "file_name";
        public static final String CREATE = "CREATE TABLE " + TABLENAME + " (" + COLUMN_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_FILE_NAME + " TEXT)";
        public static final String DROP = "DROP TABLE IF EXISTS " + TABLENAME;
        public static final String INSERT = "INSERT INTO " + TABLENAME + " (" + COLUMN_FILE_NAME
                + ") VALUES (?)";
        public static final String SELECT_ALL = "SELECT " + COLUMN_ID + ", " + COLUMN_FILE_NAME
                + " FROM " + TABLENAME;
        public static final String SELECT_FILE_NAME = "SELECT DISTINCT " + COLUMN_FILE_NAME
                + " FROM " + TABLENAME;
    }

    /**
     * @author schuessler
     * 
     * class to handle the database communication for an File
     */
    static final class FilesTable {

        public static final String TABLENAME = "files";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_FILE_NAME = "file_name";
        public static final String CREATE = "CREATE TABLE " + TABLENAME + " (" + COLUMN_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_FILE_NAME + " TEXT)";
        public static final String DROP = "DROP TABLE IF EXISTS " + TABLENAME;
        public static final String INSERT = "INSERT INTO " + TABLENAME + " (" + COLUMN_FILE_NAME
                + ") VALUES (?)";
        public static final String SELECT_ALL = "SELECT " + COLUMN_ID + ", " + COLUMN_FILE_NAME
                + " FROM " + TABLENAME;
        public static final String SELECT_FILE_NAME = "SELECT DISTINCT " + COLUMN_FILE_NAME
                + " FROM " + TABLENAME;
        public static final String SELECT_FILE_EXISTS = "SELECT DISTINCT " + COLUMN_FILE_NAME
                + " FROM " + TABLENAME + " WHERE " + COLUMN_FILE_NAME + " = ?";

    }

    /**
     * @author hoppe
     * 
     * class to handle the database communication for a NoticeSelection
     */

    public static final class NoticeSelection {

        public static final String TABLENAME = "noticeselection";
        public static final String ID = "s_id";
        public static final String START = "start";
        public static final String END = "end";
        public static final String ID_NOTICE = "idnotice";

        public static final String CREATE = "CREATE TABLE " + TABLENAME + " (" + ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + START + " INTEGER, " + END
                + " INTEGER, " + ID_NOTICE + " INTEGER" + ")";

        public static final String DROP = "DROP TABLE IF EXISTS " + TABLENAME;

        public static final String DELETE = "DELETE FROM " + TABLENAME + " WHERE " + ID_NOTICE
                + " = ?";

        public static final String INSERT = "INSERT INTO " + TABLENAME + " (" + START + ", " + END
                + ", " + ID_NOTICE + ") " + "VALUES (?, ?, ?)";

        public static final String SELECT = "SELECT " + ID + ", " + START + ", " + END + ", "
                + ID_NOTICE + " FROM " + TABLENAME;

        public static final String UPDATE = "UPDATE " + TABLENAME + " SET " + START + " = ?, "
                + END + " = ?, " + ID_NOTICE + " = ? " + "WHERE " + ID + " = ?";
    }

    /**
     * @author hoppe
     * 
     * class to handle the database communication for a joined selection
     */

    public static final class NoticeJoinSelect {

        public static final String TABLENAMENOTICE = "notice";
        public static final String NID = "n_id";
        public static final String DATE = "date";
        public static final String SUBJECT = "subject";
        public static final String LOCK = "lock";
        public static final String CONTENT = "content";
        public static final String TABLENAMENOTICESELECTION = "noticeselection";
        public static final String SID = "s_id";
        public static final String START = "start";
        public static final String END = "end";
        public static final String ID_NOTICE = "idnotice";

        public static final String SELECT = "SELECT " + TABLENAMENOTICE + "." + NID + ", "
                + TABLENAMENOTICE + "." + DATE + ", " + TABLENAMENOTICE + "." + SUBJECT + ", "
                + TABLENAMENOTICE + "." + LOCK + ", " + TABLENAMENOTICE + "." + CONTENT + ", "
                + TABLENAMENOTICESELECTION + "." + SID + ", " + TABLENAMENOTICESELECTION + "."
                + START + ", " + TABLENAMENOTICESELECTION + "." + END + ", "
                + TABLENAMENOTICESELECTION + "." + ID_NOTICE + " FROM " + TABLENAMENOTICE
                + " LEFT JOIN " + TABLENAMENOTICESELECTION + " ON " + TABLENAMENOTICE + "." + NID
                + " = " + TABLENAMENOTICESELECTION + "." + ID_NOTICE + " ORDER BY "
                + TABLENAMENOTICE + "." + NID;

    }

}

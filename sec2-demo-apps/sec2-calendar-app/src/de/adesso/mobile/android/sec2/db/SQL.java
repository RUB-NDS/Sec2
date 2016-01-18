package de.adesso.mobile.android.sec2.db;

/**
 * Collection of SQL queries divided into several subclasses.
 * 
 * @author hoppe
 */
public abstract class SQL {

    /**
     * @author hoppe
     * 
     * class to handle the database communication for a NoticeListItem
     */

    public static final class NoticeListItem {

        public static final String TABLENAME = "notice";
        public static final String ID = "n_id";
        public static final String DATE = "date";
        public static final String SUBJECT = "subject";
        public static final String LOCK = "lock";
        public static final String CONTENT = "content";

        public static final String CREATE = "CREATE TABLE " + TABLENAME + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DATE + " TEXT, " + SUBJECT
                + " TEXT, " + LOCK + " INTEGER, " + CONTENT + " TEXT" + ")";

        public static final String DROP = "DROP TABLE IF EXISTS " + TABLENAME;

        public static final String DELETE = "DELETE FROM " + TABLENAME + " WHERE " + ID + " = ?";

        public static final String INSERT = "INSERT INTO " + TABLENAME + " (" + DATE + ", " + SUBJECT + ", " + LOCK + ", " + CONTENT + ") "
                + "VALUES (?, ?, ?, ?)";

        public static final String SELECT = "SELECT " + ID + ", " + DATE + ", " + SUBJECT + ", " + LOCK + ", " + CONTENT + " FROM " + TABLENAME;

        public static final String UPDATE = "UPDATE " + TABLENAME + " SET " + DATE + " = ?, " + SUBJECT + " = ?, " + LOCK + " = ?, " + CONTENT + " = ? "
                + "WHERE " + ID + " = ?";
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

        public static final String CREATE = "CREATE TABLE " + TABLENAME + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + START + " INTEGER, " + END
                + " INTEGER, " + ID_NOTICE + " INTEGER" + ")";

        public static final String DROP = "DROP TABLE IF EXISTS " + TABLENAME;

        public static final String DELETE = "DELETE FROM " + TABLENAME + " WHERE " + ID_NOTICE + " = ?";

        public static final String INSERT = "INSERT INTO " + TABLENAME + " (" + START + ", " + END + ", " + ID_NOTICE + ") " + "VALUES (?, ?, ?)";

        public static final String SELECT = "SELECT " + ID + ", " + START + ", " + END + ", " + ID_NOTICE + " FROM " + TABLENAME;

        public static final String UPDATE = "UPDATE " + TABLENAME + " SET " + START + " = ?, " + END + " = ?, " + ID_NOTICE + " = ? " + "WHERE " + ID + " = ?";
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

        public static final String SELECT = "SELECT " + TABLENAMENOTICE + "." + NID + ", " + TABLENAMENOTICE + "." + DATE + ", " + TABLENAMENOTICE + "."
                + SUBJECT + ", " + TABLENAMENOTICE + "." + LOCK + ", " + TABLENAMENOTICE + "." + CONTENT + ", " + TABLENAMENOTICESELECTION + "." + SID + ", "
                + TABLENAMENOTICESELECTION + "." + START + ", " + TABLENAMENOTICESELECTION + "." + END + ", " + TABLENAMENOTICESELECTION + "." + ID_NOTICE
                + " FROM " + TABLENAMENOTICE + " LEFT JOIN " + TABLENAMENOTICESELECTION + " ON " + TABLENAMENOTICE + "." + NID + " = "
                + TABLENAMENOTICESELECTION + "." + ID_NOTICE + " ORDER BY " + TABLENAMENOTICE + "." + NID;

    }

}

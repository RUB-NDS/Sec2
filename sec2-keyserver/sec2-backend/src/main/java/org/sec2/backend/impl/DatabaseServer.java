package org.sec2.backend.impl;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sec2.backend.IChallenge;
import org.sec2.backend.IGroupInfo;
import org.sec2.backend.IUserInfo;
import org.sec2.backend.exceptions.GroupAlreadyExistsException;
import org.sec2.backend.exceptions.InvalidUserPKCException;
import org.sec2.backend.exceptions.UserAlreadyExistsException;
import org.sec2.backend.exceptions.UserNotFoundException;

import com.mysql.jdbc.Statement;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

/**
 * The DatabaseServer is used to persist users, groups, group memberships and 
 * key references in a database.
 * 
 * @author Utimaco Safeware
 */
public class DatabaseServer {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseServer.class.getName());

    public static final String PROPERTY_DATABASE_URL = "database.url";
    public static final String PROPERTY_DATABASE_USERNAME = "database.username";
    public static final String PROPERTY_DATABASE_PASSWORD = "database.password";
    public static final String PROPERTY_DATABASE_DRIVER = "database.driver";

    private static final int STATUS_INACTIVE = 1;
    private static final int STATUS_ACTIVE = 2;

    private Connection connection;
    private Properties properties;

    /**
     * Creates a new {@code DatabaseServer} with the given {@code configuration}.
     * 
     * @param configuration Configuration {@link Properties} object.
     */
    public DatabaseServer(Properties configuration) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                DatabaseServer.class.getName(),
                "DatabaseServer", 
                configuration
            );
        }
        this.properties = configuration;
        this.connection = createConnection();
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(
                DatabaseServer.class.getName(),
                "DatabaseServer"
            );
        }
    }

    /**
     * Closes the SQL connection.
     */
    protected void shutdown() {
        try {
            connection.close();
        } catch (SQLException e) {
            LOGGER.severe("SQL connection could not be closed: "
                    + e.getMessage());
        }
        
    }

    /**
     * Activates the user by setting his status to {@link #STATUS_ACTIVE}. 
     * 
     * This method performs no parameter validation.
     * 
     * @param userId ID of the user (SHA-256 hash of the user's signing 
     *               certificate)
     */
    public void activateUser(byte[] userId) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                DatabaseServer.class.getName(),
                "activateUser", 
                userId
            );
        }
        String query = "UPDATE users SET status = (?) WHERE (pk_hash = ?);";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(query);
            statement.setInt(1, STATUS_ACTIVE);
            statement.setBytes(2, userId);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            try {
                statement.close();
            }
            catch (SQLException e) {
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.exiting(
                        DatabaseServer.class.getName(),
                        "activateUser"
                    );
                }
            }
        }
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(
                DatabaseServer.class.getName(),
                "activateUser"
            );
        }
    }

    /**
     * Adds the user with ID {@code userId} to the group with the name 
     * {@code groupName}. 
     * 
     * This method performs no parameter validation. 
     * 
     * @param userId    ID of the user (SHA-256 hash of the user's signing 
     *                  certificate)
     * @param groupName Name of the group
     */
    public void addUserToGroup(byte[] userId, String groupName) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                DatabaseServer.class.getName(),
                "addUserToGroup", 
                new Object[]{ userId, groupName }
            );
        }
        String query = "INSERT INTO group_members (user_ref, group_ref) SELECT users.id, groups.id FROM users, groups WHERE groups.name = ? AND users.pk_hash = ?; ";
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, groupName);
            statement.setBytes(2, userId);
            statement.executeUpdate();
        }
        catch (MySQLIntegrityConstraintViolationException e) {
            // in 99% this means that the user is already in that group
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("Error at adding user to group: %s", e.getMessage()));
                e.printStackTrace();
            }
        }
        catch (SQLException e) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("SQL error at adding user to group: %s", e.getMessage()));
                e.printStackTrace();
            }
        }
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(
                DatabaseServer.class.getName(),
                "addUserToGroup"
            );
        }
    }

    /**
     * Checks the integrity of a row in the database.
     */
    @SuppressWarnings("unused")
    private void checkRowIntegrity() {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                DatabaseServer.class.getName(),
                "checkRowIntegrity"
            );
        }
        
        //TODO: implement
        
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(
                DatabaseServer.class.getName(),
                "checkRowIntegrity"
            );
        }
    }

    /**
     * Creates a {@link Connection} to the database.
     * @return
     */
    public Connection createConnection() {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                DatabaseServer.class.getName(),
                "createConnection"
            );
        }
        Connection connection = null;
        try {
            Class.forName(
                properties.getProperty(PROPERTY_DATABASE_DRIVER)
            ).newInstance();
            connection = DriverManager.getConnection(
                properties.getProperty(PROPERTY_DATABASE_URL), 
                properties.getProperty(PROPERTY_DATABASE_USERNAME), 
                properties.getProperty(PROPERTY_DATABASE_PASSWORD)
            );
        }
        catch (InstantiationException e) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("Error at instatiation of connection: %s", e.getMessage()));
                e.printStackTrace();
            }
        }
        catch (IllegalAccessException e) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("Illegal access: %s", e.getMessage()));
                e.printStackTrace();
            }
        }
        catch (ClassNotFoundException e) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("JDBC driver not found: %s", e.getMessage()));
                e.printStackTrace();
            }
        }
        catch (SQLException e) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("SQL error: %s", e.getMessage()));
                e.printStackTrace();
            }
        }

        
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(
                DatabaseServer.class.getName(),
                "createConnection",
                connection
            );
        }
        return connection;
    }

    /**
     * 
     * @param userId
     * @param groupName
     * @throws GroupAlreadyExistsException
     */
    public void createGroup(byte[] userId, String groupName)
            throws GroupAlreadyExistsException {
        String query = "INSERT INTO groups (" + "  operator" + ", name )"
                + " VALUES (?, ?)";
        PreparedStatement statement = null;
        String emailAddress = fetchEmailAddress(userId);
        int groupId = 0, internalUserId = 0;
        try {
            statement = connection.prepareStatement(query,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, emailAddress);
            statement.setString(2, groupName);
            // System.out.println(statement);
            statement.executeUpdate();
            ResultSet result = statement.getGeneratedKeys();
            result.first();
            groupId = result.getInt(1);
            result.close();
            internalUserId = fetchInternalUserId(userId);
        }
        catch (SQLException e) {
            if (e instanceof MySQLIntegrityConstraintViolationException) {
                throw new GroupAlreadyExistsException();
            }
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            try {
                statement.close();
            }
            catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        query = "INSERT INTO group_members (" + "  user_ref" + ", group_ref )"
                + " VALUES (?, ?)";
        try {
            statement = connection.prepareStatement(query);
            statement.setInt(1, internalUserId);
            statement.setInt(2, groupId);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            try {
                statement.close();
            }
            catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * 
     * @param userId
     * @param encryptionCertificate
     * @param signatureCertificate
     * @param emailAddress
     * @param challenge
     * @return
     * @throws UserAlreadyExistsException
     */
    public long createNewUser(byte[] userId, X509Certificate encryptionCertificate,
            X509Certificate signatureCertificate, String emailAddress,
            byte[] challenge) throws UserAlreadyExistsException {
        String query = "INSERT INTO users (" 
            + "  pk_hash" 
            + ", email"
            + ", pkc" 
            + ", sign_pkc" 
            + ", status" 
            + ", challenge"
            + ", registered_at )"
            + " VALUES (BINARY ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = null;
        long timestamp = System.currentTimeMillis();
        try {
            statement = connection.prepareStatement(query);
            statement.setBytes(1, userId);
            statement.setString(2, emailAddress);
            statement.setBytes(3, encryptionCertificate.getEncoded());
            statement.setBytes(4, signatureCertificate.getEncoded());
            statement.setInt(5, STATUS_INACTIVE);
            statement.setBytes(6, challenge);
            // XXX: use better source of time
            statement.setTimestamp(7, new Timestamp(timestamp));
            statement.execute();
        }
        catch (SQLException e) {
            if (e instanceof MySQLIntegrityConstraintViolationException) {
                e.printStackTrace();
                throw new UserAlreadyExistsException();
            }
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (CertificateEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            try {
                statement.close();
            }
            catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return timestamp;
    }

    /**
     * 
     * @param groupName
     * @return
     */
    public boolean deleteGroup(String groupName) {
        String query = "DELETE groups, group_members FROM groups INNER JOIN group_members ON groups.id = group_members.group_ref WHERE (name = ?);";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, groupName);
            if (statement.executeUpdate() == 0) {
                return false;
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                statement.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 
     * @param userId
     * @return
     */
    public String fetchEmailAddress(byte[] userId) {
        String query = "SELECT email FROM users WHERE (pk_hash = ?);";
        PreparedStatement statement = null;
        String emailAddress = null;
        try {
            statement = connection.prepareStatement(query);
            statement.setBytes(1, userId);
            ResultSet result = statement.executeQuery();
            result.first();
            emailAddress = result.getString(1);
            result.close();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            try {
                statement.close();
            }
            catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return emailAddress;
    }

    public X509Certificate fetchEncryptionPKC(byte[] userId) throws UserNotFoundException {
        String query = "SELECT pkc FROM users WHERE (pk_hash = ?);";
        PreparedStatement statement = null;
        X509Certificate certificate = null;
        try {
            statement = connection.prepareStatement(query);
            statement.setBytes(1, userId);
            ResultSet result = statement.executeQuery();
            if (!result.first())
                throw new UserNotFoundException();
            byte[] userPKC = result.getBytes(1);
            certificate = CertificateUtil.convertToX509Certificate(userPKC);
            result.close();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InvalidUserPKCException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            try {
                statement.close();
            }
            catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return certificate;
    }

    public int fetchGroupId(String groupName) {
        String query = "SELECT id FROM groups WHERE (name = ?);";
        PreparedStatement statement = null;
        int groupId = 0;
        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, groupName);
            ResultSet result = statement.executeQuery();
            if (!result.first()) {
                result.close();
                return groupId;
            }
            groupId = result.getInt(1);
            result.close();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            try {
                statement.close();
            }
            catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return groupId;
    }

    /**
     * Fetches a GroupInfo object from database that contains basic information 
     * about the group with name groupName.
     * 
     * @param groupName Name of the group to fetch
     * @return If a group with name groupName exists, a GroupInfo object 
     * containing information about the group is returned, null otherwise.
     */
    public GroupInfo fetchBasicGroupInfo(String groupName) {
        String query = "SELECT groups.id, " +
                "groups.operator, " +
                "groups.name, " +
                "users.pk_hash as operatorId, " +
                "users.sign_pkc as operatorSignPKC, " +
                "users.pkc as operatorEncPKC, " +
                "users.status " +
                "FROM groups, users WHERE (groups.name = ?) AND (users.email = groups.operator)";
        PreparedStatement statement = null;
        GroupInfo groupInfo = null;
        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, groupName);
            
            ResultSet result = statement.executeQuery();
            if (!result.first()) {
                result.close();
                return groupInfo;
            }
            
            String name = result.getString("groups.name");
            int id = result.getInt("groups.id");
            String operatorMail = result.getString("groups.operator");
            byte[] operatorId = result.getBytes("operatorId");
            byte[] operatorSignPKC = result.getBytes("operatorSignPKC");
            byte[] operatorEncPKC = result.getBytes("operatorEncPKC");
            
            
            
            groupInfo = new GroupInfo();
            groupInfo.setGroupName(name);
            groupInfo.setId(id);
            UserInfo operator = new UserInfo();
            operator.setEmailAddress(operatorMail);
            operator.setId(operatorId);
            operator.setConfirmed(result.getInt("users.status") == 2);
            operator.setEncryptionPKC(CertificateUtil.convertToX509Certificate(operatorEncPKC));
            operator.setSignaturePKC(CertificateUtil.convertToX509Certificate(operatorSignPKC));
            groupInfo.setOperator(operator);
            
            result.close();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InvalidUserPKCException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            try {
                statement.close();
            }
            catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return groupInfo;
    }

    private int fetchInternalUserId(byte[] userId) {
        String query = "SELECT id FROM users WHERE (pk_hash = ?);";
        PreparedStatement statement = null;
        int internalUserId = 0;
        try {
            statement = connection.prepareStatement(query);
            statement.setBytes(1, userId);
            ResultSet result = statement.executeQuery();
            result.first();
            internalUserId = result.getInt(1);
            result.close();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            try {
                statement.close();
            }
            catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return internalUserId;
    }

    public UserInfo fetchUserInfoByID(byte[] userID) throws UserNotFoundException {
        String query = "SELECT pk_hash, sign_pkc, pkc, email, status FROM users WHERE (pk_hash = ?);";
        
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(query);
            statement.setBytes(1, userID);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return fetchUserByPreparedStatement(statement);
    }
    
    
    public UserInfo fetchUserInfoByEmailAddress(String emailAddress) throws UserNotFoundException {
        String query = "SELECT pk_hash, sign_pkc, pkc, email, status FROM users WHERE (email = ?);";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, emailAddress);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return fetchUserByPreparedStatement(statement);
    }
    
    private UserInfo fetchUserByPreparedStatement(PreparedStatement statement) throws UserNotFoundException {
        byte[] signaturePKC = null;
        byte[] encryptionPKC = null;
        byte[] userId = null;
        String email = null;
        int status = -1;
        try {
            ResultSet result = statement.executeQuery();
            if (!result.first())
                throw new UserNotFoundException();
            userId = result.getBytes(1);
            signaturePKC = result.getBytes(2);
            encryptionPKC = result.getBytes(3);
            email = result.getString(4);
            status = result.getInt(5);
            result.close();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            try {
                statement.close();
            }
            catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setEmailAddress(email);
        userInfo.setId(userId);
        userInfo.setConfirmed(status == 2);
        try {
            userInfo.setEncryptionPKC(CertificateUtil.convertToX509Certificate(encryptionPKC));
            userInfo.setSignaturePKC(CertificateUtil.convertToX509Certificate(signaturePKC));
        }
        catch (InvalidUserPKCException e) {
            // XXX: if this fails, the database might be corrupted
            e.printStackTrace();
        }
        return userInfo;
    }
    
   
    public byte[] fetchSignaturePKC(byte[] userId) throws UserNotFoundException {
        String query = "SELECT sign_pkc FROM users WHERE (pk_hash = ?);";
        PreparedStatement statement = null;
        byte[] signaturePKC = null;
        try {
            statement = connection.prepareStatement(query);
            statement.setBytes(1, userId);
            ResultSet result = statement.executeQuery();
            if (!result.first())
                throw new UserNotFoundException();
            signaturePKC = result.getBytes(1);
            result.close();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            try {
                statement.close();
            }
            catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return signaturePKC;
    }

    public IChallenge fetchStoredChallenge(String emailAddress) {
        String query = "SELECT challenge, registered_at FROM users WHERE (email = ?);";
        PreparedStatement statement = null;
        Challenge challengeContainer = null;
        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, emailAddress);
            ResultSet result = statement.executeQuery();
            result.first();
            final byte[] challenge = result.getBytes("challenge");
            final Timestamp issueTimestamp = result.getTimestamp("registered_at");
            challengeContainer = new Challenge(challenge, issueTimestamp.getTime());
            result.close();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            try {
                statement.close();
            }
            catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return challengeContainer;
    }

    public String[] getAllKnownMembers(String email) {
        String query = "SELECT users.email FROM users WHERE users.id IN ( SELECT gm.user_ref FROM group_members JOIN (group_members gm, users us) ON gm.user_ref = us.id AND us.email = ? GROUP BY gm.user_ref );";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, email);
            ResultSet result = statement.executeQuery();
            if (!result.first())
                return new String[0];
            ArrayList<String> list = new ArrayList<String>();
            do {
                String field = result.getString(1);
                list.add(field);
            } while (result.next());
            result.close();
            return list.toArray(new String[0]);
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            try {
                statement.close();
            }
            catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

    public List<IUserInfo> getAllKnownUsers(byte[] operatorId) {
        String query = "SELECT pk_hash, email, pkc, sign_pkc, status FROM users " 
                + "WHERE users.id IN ( "
                + "SELECT gm2.user_ref FROM group_members gm2 WHERE gm2.group_ref IN ( "
                + "SELECT gm.group_ref FROM group_members " 
                + "JOIN (group_members gm, users u) " 
                + "ON gm.user_ref = u.id AND u.pk_hash = ? GROUP BY group_ref) GROUP BY user_ref) AND pk_hash != ? ";
        
        PreparedStatement statement = null;
        ResultSet result = null;
        ArrayList<IUserInfo> userList = new ArrayList<IUserInfo>();
        UserInfo userInfo = null;
        try {
            statement = connection.prepareStatement(query);
            statement.setBytes(1, operatorId);
            statement.setBytes(2, operatorId);
            result = statement.executeQuery();
            
            if (!result.first()) return userList;
            
            do {
                userInfo = new UserInfo();
                byte[] id = result.getBytes("pk_hash");
                userInfo.setId(id);
                String emailAddress = result.getString("email");
                userInfo.setEmailAddress(emailAddress);
                byte[] encryptionPKCBytes = result.getBytes("pkc");
                X509Certificate encryptionPKC = CertificateUtil.convertToX509Certificate(encryptionPKCBytes);
                userInfo.setEncryptionPKC(encryptionPKC);
                byte[] signaturePKCBytes = result.getBytes("sign_pkc");
                X509Certificate signaturePKC = CertificateUtil.convertToX509Certificate(signaturePKCBytes);
                userInfo.setSignaturePKC(signaturePKC);
                userInfo.setConfirmed(result.getInt("status") == 2);
                userList.add(userInfo);
            } while(result.next());
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InvalidUserPKCException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            try {
                statement.close();
                result.close();
            }
            catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return userList;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public boolean groupExists(String groupName) {
        String query = "SELECT COUNT(*) FROM groups WHERE (groups.name = ?);";
        int size = 1;
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, groupName);
            // System.out.println("SQL: " + statement.toString());
            ResultSet result = statement.executeQuery();
            result.first();
            size = result.getInt(1);
            result.close();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            try {
                statement.close();
            }
            catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return size > 0;
    }

    /**
     * 
     * @param userId
     * @param groupName
     * @return
     */
    public boolean isGroupMember(byte[] userId, String groupName) {
        int internalUserId = fetchInternalUserId(userId);
        int groupId = fetchGroupId(groupName);

        String query = "SELECT COUNT(*) FROM group_members WHERE (group_ref = ?) AND (user_ref = ?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(query);
            statement.setInt(2, internalUserId);
            statement.setInt(1, groupId);
            ResultSet result = statement.executeQuery();
            System.out.println(statement.toString());
            result.first();
            if (result.getInt(1) == 0) {
                result.close();
                return false;
            }
            result.close();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            try {
                statement.close();
            }
            catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return true;
    }
    
    public boolean isGroupOperator(IUserInfo operator, String groupName) {
        String query = "SELECT operator FROM groups WHERE (operator = ?) AND (name = ?);";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, operator.getEmailAddress());
            statement.setString(2, groupName);
            ResultSet result = statement.executeQuery();
            if (!result.first()) {
                result.close();
                return false;
            }
            result.close();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            try {
                statement.close();
            }
            catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return true;
    }

    public IGroupInfo updateGroup(IGroupInfo group) {
        PreparedStatement statement = null;
        try {
            connection.setAutoCommit(false); // Begin transaction mode
            List<IUserInfo> newMembers = group.getMembers();
            // Remove old members            
            String query = "DELETE FROM group_members WHERE group_ref IN (SELECT g.id FROM groups g WHERE g.name = ?)";
            statement = connection.prepareStatement(query);
            statement.setString(1, group.getGroupName());
            statement.executeUpdate();
            
            // Update memberships
            query = "INSERT IGNORE INTO group_members (group_ref, user_ref) " 
                         + "SELECT g.id, ? FROM groups AS g WHERE g.name = ?";
            
            byte[] userId;
            for(IUserInfo member : newMembers) {
                userId = member.getId();
                int internalUserId = fetchInternalUserId(userId);
                statement = connection.prepareStatement(query);
                statement.setInt(1, internalUserId);
                statement.setString(2, group.getGroupName());
                statement.executeUpdate();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback();
            }
            catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        finally {
            try {
                statement.close();
                
                connection.setAutoCommit(true);
            }
            catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return group;
    }

    public boolean userExists(byte[] userId, String emailAddress) {
        String query = "SELECT COUNT(*) FROM users WHERE (email = ?) OR (pk_hash = ?);";
        int size = 1;
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, emailAddress);
            statement.setBytes(2, userId);
            // System.out.println("SQL: " + statement.toString());
            ResultSet result = statement.executeQuery();
            result.first();
            size = result.getInt(1);
            result.close();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            try {
                statement.close();
            }
            catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return size > 0;
    }

    public ArrayList<IUserInfo> fetchGroupMembers(String groupName) {
        String query = "SELECT users.pk_hash, users.email, users.pkc, users.sign_pkc, users.status FROM users, groups, group_members WHERE (group_ref = groups.id) AND (user_ref = users.id) AND (groups.name = ?)";
        PreparedStatement statement = null;
        ArrayList<IUserInfo> userList = new ArrayList<IUserInfo>();
        ResultSet result = null;
        UserInfo userInfo = null;
        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, groupName);
              // System.out.println("SQL: " + statement.toString());
            result = statement.executeQuery();
            if (!result.first()) return userList;
            
            do {
                userInfo = new UserInfo();
                byte[] id = result.getBytes("users.pk_hash");
                userInfo.setId(id);
                String emailAddress = result.getString("users.email");
                userInfo.setEmailAddress(emailAddress);
                byte[] encryptionPKCBytes = result.getBytes("users.pkc");
                X509Certificate encryptionPKC = CertificateUtil.convertToX509Certificate(encryptionPKCBytes);
                userInfo.setEncryptionPKC(encryptionPKC);
                byte[] signaturePKCBytes = result.getBytes("users.sign_pkc");
                X509Certificate signaturePKC = CertificateUtil.convertToX509Certificate(signaturePKCBytes);
                userInfo.setSignaturePKC(signaturePKC);
                userInfo.setConfirmed(result.getInt("users.status") == 2);
                userList.add(userInfo);
            } while(result.next());
            result.close();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InvalidUserPKCException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            try {
                result.close();
                statement.close();
            }
            catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return userList;
    }

    public List<String> fetchKnownGroupNames(byte[] operatorId) {
        String query = "SELECT g.name FROM groups g WHERE g.id IN ( "
                + "SELECT gm.group_ref FROM group_members "
                + "JOIN (group_members gm, users u) "
                + "ON gm.user_ref = u.id and u.pk_hash = ? GROUP BY group_ref)";
        PreparedStatement statement = null;
        ArrayList<String> groupNames = new ArrayList<String>();
        ResultSet result = null;
        try {
            statement = connection.prepareStatement(query);
            statement.setBytes(1, operatorId);
              // System.out.println("SQL: " + statement.toString());
            result = statement.executeQuery();
            if (!result.first()) return groupNames;
            
            do {
                String name = result.getString("g.name");
                groupNames.add(name);
            } while(result.next());
            result.close();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            try {
                result.close();
                statement.close();
            }
            catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return groupNames;
    }

}

package org.sec2.mwserver.core.rest;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import junit.framework.TestCase;
import org.sec2.exceptions.ExMiddlewareException;

import org.sec2.managers.IAppKeyManager;
import org.sec2.managers.IGroupManager;
import org.sec2.managers.IUserManager;
import org.sec2.managers.ManagerProvider;
import org.sec2.managers.beans.Group;
import org.sec2.managers.beans.User;
import org.sec2.mwserver.core.TestCryptoUtils;
import org.sec2.mwserver.core.TestDbManager;
import org.sec2.mwserver.core.TestResponses;
import org.sec2.mwserver.core.exceptions.ExServerConnectionException;
import org.sec2.mwserver.core.util.ICryptoUtils;
import org.sec2.mwserver.core.xml.XmlResponseCreator;
import org.sec2.persistence.PersistenceManagerContainer;

/**
 * This JUnit-Class tests the non-abstract methods of class
 * AbstractRestFunctionExecutor.
 *
 * @author nike
 */
public final class AbstractRestFunctionExecutorTest extends TestCase {

    private TestRestFunctionExecutor trfe = new TestRestFunctionExecutor();
    private TestCryptoUtils cryptoUtils = new TestCryptoUtils();
    private Group group = null;
    private User registeredUser = null;
    private User user = null;
    private LinkedList<Group> groups = new LinkedList<Group>();
    private final LinkedList<User> knownUsers = new LinkedList<User>();
    private final LinkedList<User> users = new LinkedList<User>();
    private static final String GROUP_ID = "10";
    private static final String USER_ID = "Cw==";
    private static final String NONCE = "123456789";

    @Override
    public void setUp() throws Exception {
        IGroupManager groupManager = null;
        IUserManager userManager = null;
        String[] groupIds = null;
        List<byte[]> userIds = null;
        Group groupTmp = null;

        PersistenceManagerContainer.setPersistenceManager(new TestDbManager());
        groupManager = ManagerProvider.getInstance().getGroupManager();
        //Get group
        group = groupManager.getGroup(GROUP_ID);
        //Get groups for user
        groupIds = groupManager.getGroupsForUser(cryptoUtils.decodeBase64(
                USER_ID));
        for (int i = 0; i < groupIds.length; i++) {
            groupTmp = groupManager.getGroup(groupIds[i]);
            if (groupTmp != null) {
                groups.add(groupTmp);
            }
        }
        //Get known users
        userManager = ManagerProvider.getInstance().getUserManager();
        userIds = userManager.getKnownUsers();
        for (int i = 0; i < userIds.size(); i++) {
            knownUsers.add(userManager.getUser(userIds.get(i)));
        }
        //Get registered user
        registeredUser = userManager.getRegisteredUser();
        //Get user
        user = userManager.getUser(cryptoUtils.decodeBase64(USER_ID));
        //get users in group
        userIds = groupManager.getGroup(GROUP_ID).getMembers();
        for (int i = 0; i < userIds.size(); i++) {
            users.add(userManager.getUser(userIds.get(i)));
        }
    }

    /**
     * This method tests the getGroup()-method, if it writes the expected String
     * to the writer.
     */
    public void testGetGroup() throws ExMiddlewareException, ExServerConnectionException,
            ParserConfigurationException, TransformerException, UnsupportedEncodingException {
        StringWriter result = new StringWriter();
        BufferedWriter writer = new BufferedWriter(result);
        String content = null;
        String header = null;
        String expected = null;

        trfe.getGroup(GROUP_ID, NONCE, writer);
        content = XmlResponseCreator.createGetGroupResponse(group, NONCE,
                cryptoUtils);
        header = MessageFormat.format(TestResponses.RESPONSE_HEADER,
                "" + content.getBytes("UTF-8").length);
        expected = header + "\r\n" + content;
        assertEquals(expected.trim(), result.toString().trim());
    }

    /**
     * This method tests the getGroupsForUser()-method, if it writes the
     * expected String to the writer.
     */
    public void testGetGroupsForUser() throws Exception {
        StringWriter result = new StringWriter();
        BufferedWriter writer = new BufferedWriter(result);
        String content = null;
        String header = null;
        String expected = null;

        trfe.getGroupsForUser(USER_ID, NONCE, writer);
        content = XmlResponseCreator.createGetGroupsResponse(groups,
                NONCE);
        header = MessageFormat.format(TestResponses.RESPONSE_HEADER,
                "" + content.getBytes("UTF-8").length);
        expected = header + "\r\n" + content;
        assertEquals(expected.trim(), result.toString().trim());
    }

    /**
     * This method tests the getKnownUsers()-method, if it writes the expected
     * String to the writer.
     */
    public void testGetKnownUsers() throws Exception {
        StringWriter result = new StringWriter();
        BufferedWriter writer = new BufferedWriter(result);
        String content = null;
        String header = null;
        String expected = null;

        trfe.getKnownUsers(NONCE, writer);
        content = XmlResponseCreator.createGetUsersResponse(knownUsers,
                NONCE, cryptoUtils);
        header = MessageFormat.format(TestResponses.RESPONSE_HEADER,
                "" + content.getBytes("UTF-8").length);
        expected = header + "\r\n" + content;
        assertEquals(expected.trim(), result.toString().trim());
    }

    /**
     * This method tests the getRegisteredUser()-method, if it writes the
     * expected String to the writer.
     */
    public void testGetRegisteredUser() throws Exception {
        StringWriter result = new StringWriter();
        BufferedWriter writer = new BufferedWriter(result);
        String content = null;
        String header = null;
        String expected = null;

        trfe.getRegisteredUser(NONCE, writer);
        content = XmlResponseCreator.createGetUserResponse(registeredUser,
                NONCE, cryptoUtils);
        header = MessageFormat.format(TestResponses.RESPONSE_HEADER,
                "" + content.getBytes("UTF-8").length);
        expected = header + "\r\n" + content;
        assertEquals(expected.trim(), result.toString().trim());
    }

    /**
     * This method tests the getUser()-method, if it writes the expected String
     * to the writer.
     */
    public void testGetUser() throws Exception {
        StringWriter result = new StringWriter();
        BufferedWriter writer = new BufferedWriter(result);
        String content = null;
        String header = null;
        String expected = null;

        trfe.getUser(USER_ID, NONCE, writer);
        content = XmlResponseCreator.createGetUserResponse(user, NONCE,
                cryptoUtils);
        header = MessageFormat.format(TestResponses.RESPONSE_HEADER,
                "" + content.getBytes("UTF-8").length);
        expected = header + "\r\n" + content;
        assertEquals(expected.trim(), result.toString().trim());
    }

    /**
     * This method tests the getUsersInGroup()-method, if it writes the expected
     * String to the writer.
     */
    public void testGetUsersInGroup() throws Exception {
        StringWriter result = new StringWriter();
        BufferedWriter writer = new BufferedWriter(result);
        String content = null;
        String header = null;
        String expected = null;

        trfe.getUsersInGroup(GROUP_ID, NONCE, writer);
        content = XmlResponseCreator.createGetUsersResponse(users, NONCE,
                cryptoUtils);
        header = MessageFormat.format(TestResponses.RESPONSE_HEADER,
                "" + content.getBytes("UTF-8").length);
        expected = header + "\r\n" + content;
        assertEquals(expected.trim(), result.toString().trim());
    }

    /**
     * This method tests the register()-method, if it writes the expected String
     * to the writer.
     */
    public void testRegister() throws Exception {
        StringWriter result = new StringWriter();
        BufferedWriter writer = new BufferedWriter(result);
        String expected = null;
        String content = null;
        String header = null;
        final String appName = "Test-App";
        IAppKeyManager keyManager =
                ManagerProvider.getInstance().getAppKeyManager();

        trfe.register(appName, NONCE, writer);
        content = XmlResponseCreator.createRegisterResponse(
                keyManager.getKeyForApp(appName), NONCE, cryptoUtils);
        header = MessageFormat.format(TestResponses.RESPONSE_HEADER,
                "" + content.getBytes("UTF-8").length);
        expected = header + "\r\n" + content;
        assertEquals(expected.trim(), result.toString().trim());
    }

    private final class TestRestFunctionExecutor
            extends AbstractRestFunctionExecutor {

        @Override
        protected boolean allowAppToRegister(String appName) {
            return true;
        }

        @Override
        protected ICryptoUtils getCryptoUtils() {
            return cryptoUtils;
        }
    }
}

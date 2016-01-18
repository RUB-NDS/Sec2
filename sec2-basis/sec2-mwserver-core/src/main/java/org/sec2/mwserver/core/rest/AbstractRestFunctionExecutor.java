package org.sec2.mwserver.core.rest;

import java.io.BufferedWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.SecretKey;

import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.logging.LogLevel;
import org.sec2.managers.IAppKeyManager;
import org.sec2.managers.IGroupManager;
import org.sec2.managers.IUserManager;
import org.sec2.managers.ManagerProvider;
import org.sec2.managers.beans.Group;
import org.sec2.managers.beans.User;
import org.sec2.mwserver.core.HttpHeaderNames;
import org.sec2.mwserver.core.exceptions.ExServerConnectionException;
import org.sec2.mwserver.core.exceptions.HandleRequestException;
import org.sec2.mwserver.core.util.ICryptoUtils;
import org.sec2.mwserver.core.xml.XmlResponseCreator;

/**
 * This class implements the several functions of the REST-interface. It is
 * abstract, because methods allowAppToRegister() and getCryptoUtils() have to
 * be implemented platform-specific.
 *
 * @author nike
 */
public abstract class AbstractRestFunctionExecutor
{
    /**
     * Registers the App at the Middleware.
     *
     * @param appName - The unique appName under which the App will be
     *  registered at the Middleware
     * @param nonce - The nonce
     * @param writer - The writer to which the response has to be written.
     *
     * @throws ExMiddlewareException
     * @throws ExServerConnectionException
     * @throws HandleRequestException
     */
    public void register(final String appName, final String nonce,
            final BufferedWriter writer)
                    throws ExMiddlewareException, ExServerConnectionException,
                    HandleRequestException
                    {
        final IAppKeyManager keyManager =
                ManagerProvider.getInstance().getAppKeyManager();
        String content = null;
        String header = null;
        SecretKey key = null;

        if (allowAppToRegister(appName))
        {
            try
            {
                key = keyManager.createRequestValidationKey(appName);
                content = XmlResponseCreator.createRegisterResponse(key, nonce,
                        getCryptoUtils());
                header = createHeader(content.getBytes("utf-8").length);
                writer.write(header + content);
                writer.flush();
            }
            catch (final UnsupportedEncodingException uee)
            {
                throw new ExMiddlewareException(
                        "Fehler während der Schlüsselkodierung", null,
                        LogLevel.PROBLEM);
            }
            catch (final Exception e)
            {
                throw new ExServerConnectionException(
                        "Response konnte nicht verschickt werden", null,
                        LogLevel.PROBLEM);
            }
        }
        else
        {
            throw new HandleRequestException(
                    "Request konnte nicht verarbeitet werden. Entweder"
                            + "fehlende Header-Felder oder Benutzererlaubnis");
        }
                    }

    /**
     * Returns the user who is registered at the middleware.
     *
     * @param nonce - The nonce
     * @param writer - The writer to which the response has to be written.
     *
     * @throws ExMiddlewareException
     * @throws ExServerConnectionException
     */
    public void getRegisteredUser(final String nonce,
            final BufferedWriter writer) throws ExMiddlewareException,
            ExServerConnectionException
            {
        IUserManager userManager = null;
        String content = null;
        String header = null;

        try
        {
            userManager = ManagerProvider.getInstance().getUserManager();
            content = XmlResponseCreator.createGetUserResponse(
                    userManager.getRegisteredUser(), nonce, getCryptoUtils());
            header = createHeader(content.getBytes("utf-8").length);
            writer.write(header + content);
            writer.flush();
        }
        catch (final UnsupportedEncodingException uee)
        {
            throw new ExMiddlewareException(
                    "Fehler während der Responsegenerierung", null,
                    LogLevel.PROBLEM);
        }
        catch (final Exception e)
        {
            throw new ExServerConnectionException(
                    "Response konnte nicht verschickt werden", e,
                    LogLevel.PROBLEM);
        }
            }

    /**
     * Returns all users of the Group with the passed group ID.
     *
     * @param groupId - The group ID
     * @param nonce - The nonce
     * @param writer - The writer to which the response has to be written.
     *
     * @throws ExMiddlewareException
     * @throws ExServerConnectionException
     * @throws HandleRequestException
     */
    public void getUsersInGroup(final String groupId, final String nonce,
            final BufferedWriter writer)
                    throws ExMiddlewareException, ExServerConnectionException,
                    HandleRequestException {
        IGroupManager groupManager = null;
        IUserManager userManager = null;
        Group group = null;
        User user = null;
        List<byte[]> userIds = null;
        final LinkedList<User> users = new LinkedList<User>();
        String content = null;
        String header = null;

        try
        {
            groupManager = ManagerProvider.getInstance().getGroupManager();
            userManager = ManagerProvider.getInstance().getUserManager();
            if (groupId == null || groupId.isEmpty())
            {
                throw new HandleRequestException(
                        "Ungültiger Request: Header-Feld \""
                                + HttpHeaderNames.GROUP_ID
                                + "\" fehlt oder ist leer.");
            }
            group = groupManager.getGroup(groupId);
            if (group == null)
            {
                throw new ExMiddlewareException("Für die Gruppen-ID \""
                        + groupId + "\" konnte keine Gruppe gefunden werden.",
                        null, LogLevel.ATTENTION);
            }
            userIds = group.getMembers();
            if (userIds != null)
            {
                for (int i = 0; i < userIds.size(); i++)
                {
                    user = userManager.getUser(userIds.get(i));
                    if (user != null)
                    {
                        users.add(user);
                    }
                }
            }
            content = XmlResponseCreator.createGetUsersResponse(users, nonce,
                    getCryptoUtils());
            header = createHeader(content.getBytes("utf-8").length);
            writer.write(header + content);
            writer.flush();
        }
        catch (final UnsupportedEncodingException uee)
        {
            throw new ExMiddlewareException(
                    "Fehler während der Responsegenerierung", null,
                    LogLevel.PROBLEM);
        }
        catch (final NumberFormatException nfe)
        {
            throw new HandleRequestException(
                    "Ungültiger Request: Header-Feld \""
                            + HttpHeaderNames.GROUP_ID
                            + "\" enthielt keine gültige ID.");
        }
        catch (final Exception e)
        {
            throw new ExServerConnectionException(
                    "Response konnte nicht verschickt werden", null,
                    LogLevel.PROBLEM);
        }
    }

    /**
     * Returns the user with the passed ID.
     *
     * @param userId - The user ID base64-encoded
     * @param nonce - The nonce
     * @param writer - The writer to which the response has to be written.
     *
     * @throws ExMiddlewareException
     * @throws ExServerConnectionException
     */
    public void getUser(final String userId, final String nonce,
            final BufferedWriter writer)
                    throws ExMiddlewareException, ExServerConnectionException
                    {
        IUserManager userManager = null;
        String header = null;
        String content = null;
        User user = null;

        try
        {
            if (userId == null || userId.isEmpty())
            {
                throw new HandleRequestException(
                        "Ungültiger Request: Header-Feld \""
                                + HttpHeaderNames.USER_ID
                                + "\" fehlt oder ist leer.");
            }
            userManager = ManagerProvider.getInstance().getUserManager();
            user = userManager.getUser(getCryptoUtils().decodeBase64(userId));
            if (user == null)
            {
                throw new ExMiddlewareException("Für die Gruppen-ID \""
                        + userId + "\" konnte keine Gruppe gefunden werden.",
                        null, LogLevel.ATTENTION);
            }
            content = XmlResponseCreator.createGetUserResponse(user, nonce,
                    getCryptoUtils());
            header = createHeader(content.getBytes("utf-8").length);
            writer.write(header.toString() + content);
            writer.flush();
        }
        catch (final UnsupportedEncodingException uee)
        {
            throw new ExMiddlewareException(
                    "Fehler während der Responsegenerierung", null,
                    LogLevel.PROBLEM);
        }
        catch (final Exception e)
        {
            throw new ExServerConnectionException(
                    "Response konnte nicht verschickt werden", e,
                    LogLevel.PROBLEM);
        }
                    }

    /**
     * Returns the group with the passed ID.
     *
     * @param groupId - The group ID
     * @param nonce - The nonce
     * @param writer - The writer to which the response has to be written.
     *
     * @throws ExMiddlewareException
     * @throws ExServerConnectionException
     */
    public void getGroup(final String groupId, final String nonce,
            final BufferedWriter writer)
                    throws ExMiddlewareException, ExServerConnectionException
                    {
        IGroupManager groupManager = null;
        String header = null;
        String content = null;
        Group group = null;

        try
        {
            if (groupId == null || groupId.isEmpty())
            {
                throw new HandleRequestException(
                        "Ungültiger Request: Header-Feld \""
                                + HttpHeaderNames.GROUP_ID
                                + "\" fehlt oder ist leer.");
            }
            groupManager = ManagerProvider.getInstance().getGroupManager();
            group = groupManager.getGroup(groupId);
            if (group == null)
            {
                throw new ExMiddlewareException("Für die Gruppen-ID \""
                        + groupId + "\" konnte keine Gruppe gefunden werden.",
                        null, LogLevel.ATTENTION);
            }
            content = XmlResponseCreator.createGetGroupResponse(group, nonce,
                    getCryptoUtils());
            header = createHeader(content.getBytes("utf-8").length);
            writer.write(header.toString() + content);
            writer.flush();
        }
        catch (final UnsupportedEncodingException uee)
        {
            throw new ExMiddlewareException(
                    "Fehler während der Responsegenerierung", null,
                    LogLevel.PROBLEM);
        }
        catch (final Exception e)
        {
            throw new ExServerConnectionException(
                    "Response konnte nicht verschickt werden", e,
                    LogLevel.PROBLEM);
        }
                    }

    /**
     * Returns all groups where the user with the passed user ID is a member.
     *
     * @param userId - The user ID
     * @param nonce - The nonce
     * @param writer - The writer to which the response has to be written.
     *
     * @throws ExMiddlewareException
     * @throws ExServerConnectionException
     * @throws HandleRequestException
     */
    public void getGroupsForUser(final String userId, final String nonce,
            final BufferedWriter writer)
                    throws ExMiddlewareException, ExServerConnectionException,
                    HandleRequestException
                    {
        final LinkedList<Group> groups = new LinkedList<Group>();
        IGroupManager groupManager = null;
        String content = null;
        String header = null;
        Group group = null;
        String[] groupIds = null;

        try
        {
            if (userId == null || userId.isEmpty())
            {
                throw new HandleRequestException(
                        "Ungültiger Request: Header-Feld \""
                                + HttpHeaderNames.USER_ID
                                + "\" fehlt oder ist leer.");
            }
            groupManager = ManagerProvider.getInstance().getGroupManager();
            groupIds = groupManager.getGroupsForUser(
                    getCryptoUtils().decodeBase64(userId));
            if (groupIds != null)
            {
                for (int i = 0; i < groupIds.length; i++)
                {
                    group = groupManager.getGroup(groupIds[i]);
                    if (group != null)
                    {
                        groups.add(group);
                    }
                }
            }
            content = XmlResponseCreator.createGetGroupsResponse(groups,
                    nonce);
            header = createHeader(content.getBytes("utf-8").length);
            writer.write(header.toString() + content);
            writer.flush();
        }
        catch (final UnsupportedEncodingException uee)
        {
            throw new ExMiddlewareException(
                    "Fehler während der Responsegenerierung", null,
                    LogLevel.PROBLEM);
        }
        catch (final Exception e)
        {
            throw new ExServerConnectionException(
                    "Response konnte nicht verschickt werden", null,
                    LogLevel.PROBLEM);
        }
                    }

    /**
     * Returns a list of all users which are known to the user who is
     * logged into the middleware (and whose smartcard is inserted etc.).
     *
     * @param nonce - The nonce
     * @param writer - The writer to which the response has to be written.
     *
     * @throws ExMiddlewareException
     * @throws ExServerConnectionException
     * @throws HandleRequestException
     */
    public void getKnownUsers(final String nonce, final BufferedWriter writer)
            throws ExMiddlewareException, ExServerConnectionException
            {
        IUserManager userManager = null;
        String content = null;
        String header = null;
        List<byte[]> userIds = null;
        final LinkedList<User> users = new LinkedList<User>();
        User user = null;

        try
        {
            userManager = ManagerProvider.getInstance().getUserManager();
            userIds = userManager.getKnownUsers();
            if (userIds != null)
            {
                for (int i = 0; i < userIds.size(); i++)
                {
                    user = userManager.getUser(userIds.get(i));
                    if (user != null)
                    {
                        users.add(user);
                    }
                }
            }
            content = XmlResponseCreator.createGetUsersResponse(users, nonce,
                    getCryptoUtils());
            header = createHeader(content.getBytes("utf-8").length);
            writer.write(header.toString() + content);
            writer.flush();
        }
        catch (final UnsupportedEncodingException uee)
        {
            throw new ExMiddlewareException(
                    "Fehler während der Responsegenerierung", null,
                    LogLevel.PROBLEM);
        }
        catch (final Exception e)
        {
            throw new ExServerConnectionException(
                    "Response konnte nicht verschickt werden", null,
                    LogLevel.PROBLEM);
        }
            }

    private String createHeader(final int contentLength)
    {
        final StringBuffer header = new StringBuffer();

        header.append("HTTP/1.1 200 OK\r\n");
        header.append("Content-Length: " + contentLength + "\r\n");
        header.append("Content-Type: application/xml; charset=UTF-8\r\n");
        header.append("Connection: close\r\n");
        header.append("\r\n");

        return header.toString();
    }

    /**
     * This method returns TRUE, if the app with the passed name is allowed to
     * register at the Sec2-middleware. Otherwise this method returns FALSE.
     * The method is abstract to allow different implementations of the
     * enquiry, because on Android other UI-APIs are provided than on desktops.
     *
     * @param appName - The name of the app which whishes to get registered at
     *  the Sec2-middleware
     *
     * @return TRUE, if the app is allowed to register at the Sec2-middleware,
     *  FALSE otherwise
     */
    protected abstract boolean allowAppToRegister(String appName);

    /**
     * This method returns an instance of a class implementing the
     * ICryptoUtils-interface. The method is abstract so that different
     * implementations of the interface can be returned.
     *
     * @return - An instance of a class implementing the ICryptoUtils-interface
     */
    protected abstract ICryptoUtils getCryptoUtils();
}

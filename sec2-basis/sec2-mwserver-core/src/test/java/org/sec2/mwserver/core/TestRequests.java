package org.sec2.mwserver.core;

import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import org.sec2.core.XMLConstants;

import org.sec2.mwserver.core.util.ICryptoUtils;

/**
 * Class providing test-request-strings
 * 
 * @author nike
 */
public final class TestRequests
{
    /**
     * Request for calling the register()-function of the REST-interface.
     */
    public static final String REGISTER = "GET /register HTTP/1.1\n"
            + "Location: REST\n"
            + "x-sec2-appName: test\n"
            + "x-sec2-nonce: 123456789\n"
            + "x-sec2-timestamp: {0}\n"
            + "x-sec2-socketport: 50001\n"
            + "Host: localhost\n"
            + "Connection: Keep-Alive";
    /**
     * Request for calling the getRegisteredUser()-function of the
     * REST-interface.
     */
    public static final String GET_REGISTERED_USER =
            "GET /usermanagement/registereduser HTTP/1.1\n"
                    + "Location: REST\n"
                    + "x-sec2-appName: test\n"
                    + "x-sec2-nonce: 62299332\n"
                    + "x-sec2-timestamp: {0}\n"
                    + "x-sec2-socketport: 50001\n"
                    + "x-sec2-authentication: {1}\n"
                    + "Host: localhost\n"
                    + "Connection: Keep-Alive";
    /**
     * Request for calling the getUsersInGroup()-function of the
     * REST-interface.
     */
    public static final String GET_USERS_IN_GROUP =
            "GET /usermanagement/usersingrouplist HTTP/1.1\n"
                    + "Location: REST\n"
                    + "x-sec2-appName: test\n"
                    + "x-sec2-nonce: 62299332\n"
                    + "x-sec2-timestamp: {0}\n"
                    + "x-sec2-socketport: 50001\n"
                    + "x-sec2-groupId: 1\n"
                    + "x-sec2-authentication: {1}\n"
                    + "Host: localhost\n"
                    + "Connection: Keep-Alive";
    /**
     * Request for calling the getUser()-function of the REST-interface.
     */
    public static final String GET_USER =
            "GET /usermanagement/user HTTP/1.1\n"
                    + "Location: REST\n"
                    + "x-sec2-appName: test\n"
                    + "x-sec2-nonce: 62299332\n"
                    + "x-sec2-timestamp: {0}\n"
                    + "x-sec2-socketport: 50001\n"
                    + "x-sec2-userId: CY==\n"
                    + "x-sec2-authentication: {1}\n"
                    + "Host: localhost\n"
                    + "Connection: Keep-Alive";
    /**
     * Request for calling the getGroup()-function of the REST-interface.
     */
    public static final String GET_GROUP =
            "GET /usermanagement/group HTTP/1.1\n"
                    + "Location: REST\n"
                    + "x-sec2-appName: test\n"
                    + "x-sec2-nonce: 62299332\n"
                    + "x-sec2-timestamp: {0}\n"
                    + "x-sec2-socketport: 50001\n"
                    + "x-sec2-groupId: 1\n"
                    + "x-sec2-authentication: {1}\n"
                    + "Host: localhost\n"
                    + "Connection: Keep-Alive";
    /**
     * Request for calling the getGroupsForUser()-function of the
     * REST-interface.
     */
    public static final String GET_GROUPS_FOR_USER =
            "GET /usermanagement/groupsforuserlist HTTP/1.1\n"
                    + "Location: REST\n"
                    + "x-sec2-appName: test\n"
                    + "x-sec2-nonce: 62299332\n"
                    + "x-sec2-timestamp: {0}\n"
                    + "x-sec2-socketport: 50001\n"
                    + "x-sec2-userId: CY==\n"
                    + "x-sec2-authentication: {1}\n"
                    + "Host: localhost\n"
                    + "Connection: Keep-Alive";
    /**
     * Request for calling the getKnownUsers()-function of the REST-interface.
     */
    public static final String GET_KNOWN_USERS =
            "GET /usermanagement/knownuserslist HTTP/1.1\n"
                    + "Location: REST\n"
                    + "x-sec2-appName: test\n"
                    + "x-sec2-nonce: 62299332\n"
                    + "x-sec2-timestamp: {0}\n"
                    + "x-sec2-socketport: 50001\n"
                    + "x-sec2-authentication: {1}\n"
                    + "Host: localhost\n"
                    + "Connection: Keep-Alive";
    /**
     * Request not intended for the REST-interface.
     */
    public static final String NO_REST = "GET test.html HTTP/1.1\n"
            + "Location: www.examplecloud.com\n"
            + "x-sec2-appName: test\n"
            + "x-sec2-nonce: 62299332\n"
            + "x-sec2-timestamp: {0}\n"
            + "x-sec2-socketport: 50001\n"
            + "x-sec2-authentication: {1}\n"
            + "Host: localhost\n"
            + "Connection: Keep-Alive\n\n";
    /**
     * Request not intended for the REST-interface.
     */
    public static final String PUT_WITH_CONTENT = "PUT test.html HTTP/1.1\n"
            + "Location: www.examplecloud.com\n"
            + "Content-Type: xml\n"
            + "x-sec2-appName: test\n"
            + "x-sec2-nonce: 62299332\n"
            + "x-sec2-timestamp: {0}\n"
            + "x-sec2-socketport: 50001\n"
            + "x-sec2-authentication: {1}\n"
            + "Host: localhost\n"
            + "Connection: Keep-Alive\n\n";
    /**
     * A simple PUT-request.
     */
    public static final String PUT_REQUEST = "PUT test.html HTTP/1.1\n"
            + "Content-Length: {0}\n"
            + "Content-Type: text/html; charset = UTF-8\n"
            + "Host: localhost\n"
            + "Connection: Keep-Alive";
    /**
     * A simple DELETE-request.
     */
    public static final String DELETE_REQUEST = "DELETE test.html HTTP/1.1\n"
            + "Host: localhost\n"
            + "Connection: Keep-Alive";
    /**
     * Simple example-content to be used as content with HTTP-requests
     */
    public static final String CONTENT = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD"
            + " HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n"
            + "<html>\n"
            + "<head>\n"
            + "<title>Example-content</title>\n"
            + "</head>\n"
            + "<body>\n"
            + "<p>This is example-content</p>\n"
            + "</body>\n"
            + "</html>";

    /**
     * Returns a request for calling the register()-function of the REST
     * interface.
     *
     * @return A request for calling the register()-function of the REST
     *  interface.
     */
    public static String getRegisterRequest()
    {
        final Date now = new Date();
        final String timestamp = now.getTime() + "-" + now.getTime();

        return MessageFormat.format(REGISTER, timestamp);
    }

    /**
     * Returns a request for calling the getKnownUsers()-function of the REST
     * interface.
     *
     * @param key - The app authentication key
     * @param cryptoUtils - An instance implementing ICryptoUtils
     *
     * @return A request for calling the getKnownUsers()-function of the REST
     *  interface.
     */
    public static String getGetKnownUsersRequest(final SecretKey key,
            final ICryptoUtils cryptoUtils)
    {
        final Date now = new Date();
        final String timestamp = now.getTime() + "-" + now.getTime();
        final String header = "Location:REST;x-sec2-appName:test;"
                + "x-sec2-nonce:62299332;x-sec2-socketport:50001;"
                + "x-sec2-timestamp:" + timestamp + ";";
        String request = null;

        try
        {
            request = MessageFormat.format(GET_KNOWN_USERS, timestamp,
                    TestRequests.sign(header, key, cryptoUtils));
        }
        catch(Exception e)
        {
            request = null;
        }

        return request;
    }

    /**
     * Returns a request for calling the getRegisteredUser()-function of the
     * REST interface.
     *
     * @param key - The app authentication key
     * @param cryptoUtils - An instance implementing ICryptoUtils
     *
     * @return A request for calling the getRegisteredUser()-function of the
     *  REST interface.
     */
    public static String getGetRegisteredUserRequest(final SecretKey key,
            final ICryptoUtils cryptoUtils)
    {
        final Date now = new Date();
        final String timestamp = now.getTime() + "-" + now.getTime();
        final String header = "Location:REST;x-sec2-appName:test;"
                + "x-sec2-nonce:62299332;x-sec2-socketport:50001;"
                + "x-sec2-timestamp:" + timestamp + ";";
        String request = null;

        try
        {
            request = MessageFormat.format(GET_REGISTERED_USER, timestamp,
                    TestRequests.sign(header, key, cryptoUtils));
        }
        catch(Exception e)
        {
            request = null;
        }

        return request;
    }

    /**
     * Returns a request for calling the getUsersInGroup()-function of the REST
     * interface.
     *
     * @param key - The app authentication key
     * @param cryptoUtils - An instance implementing ICryptoUtils
     *
     * @return A request for calling the getUsersInGroup()-function of the REST
     * interface.
     */
    public static String getGetUsersInGroupRequest(final SecretKey key,
            final ICryptoUtils cryptoUtils)
    {
        final Date now = new Date();
        final String timestamp = now.getTime() + "-" + now.getTime();
        final String header = "Location:REST;x-sec2-appName:test;"
                + "x-sec2-groupId:1;x-sec2-nonce:62299332;"
                + "x-sec2-socketport:50001;"
                + "x-sec2-timestamp:" + timestamp + ";";
        String request = null;

        try
        {
            request = MessageFormat.format(GET_USERS_IN_GROUP, timestamp,
                    TestRequests.sign(header, key, cryptoUtils));
        }
        catch(Exception e)
        {
            request = null;
        }

        return request;
    }

    /**
     * Returns a request for calling the getUser()-function of the REST
     * interface.
     *
     * @param key - The app authentication key
     * @param cryptoUtils - An instance implementing ICryptoUtils
     *
     * @return A request for calling the getUser()-function of the REST
     * interface.
     */
    public static String getGetUserRequest(final SecretKey key,
            final ICryptoUtils cryptoUtils)
    {
        final Date now = new Date();
        final String timestamp = now.getTime() + "-" + now.getTime();
        final String header = "Location:REST;x-sec2-appName:test;"
                + "x-sec2-nonce:62299332;x-sec2-socketport:50001;"
                + "x-sec2-timestamp:" + timestamp + ";"
                + "x-sec2-userId:CY==;";
        String request = null;

        try
        {
            request = MessageFormat.format(GET_USER, timestamp,
                    TestRequests.sign(header, key, cryptoUtils));
        }
        catch(Exception e)
        {
            request = null;
        }

        return request;
    }

    /**
     * Returns a request for calling the getGroup()-function of the REST
     * interface.
     *
     * @param key - The app authentication key
     * @param cryptoUtils - An instance implementing ICryptoUtils
     *
     * @return A request for calling the getGroup()-function of the REST
     * interface.
     */
    public static String getGetGroupRequest(final SecretKey key,
            final ICryptoUtils cryptoUtils)
    {
        final Date now = new Date();
        final String timestamp = now.getTime() + "-" + now.getTime();
        final String header = "Location:REST;x-sec2-appName:test;"
                + "x-sec2-groupId:1;x-sec2-nonce:62299332;"
                + "x-sec2-socketport:50001;"
                + "x-sec2-timestamp:" + timestamp + ";";
        String request = null;

        try
        {
            request = MessageFormat.format(GET_GROUP, timestamp,
                    TestRequests.sign(header, key, cryptoUtils));
        }
        catch(Exception e)
        {
            request = null;
        }

        return request;
    }

    /**
     * Returns a request for calling the getGroupsForUser()-function of the
     * REST interface.
     *
     * @param key - The app authentication key
     * @param cryptoUtils - An instance implementing ICryptoUtils
     *
     * @return A request for calling the getGroupsForUser()-function of the
     * REST interface.
     */
    public static String getGetGroupsForUserRequest(final SecretKey key,
            final ICryptoUtils cryptoUtils)
    {
        final Date now = new Date();
        final String timestamp = now.getTime() + "-" + now.getTime();
        final String header = "Location:REST;x-sec2-appName:test;"
                + "x-sec2-nonce:62299332;x-sec2-socketport:50001;"
                + "x-sec2-timestamp:" + timestamp + ";"
                + "x-sec2-userId:CY==;";
        String request = null;

        try
        {
            request = MessageFormat.format(GET_GROUPS_FOR_USER, timestamp,
                    TestRequests.sign(header, key, cryptoUtils));
        }
        catch(Exception e)
        {
            request = null;
        }

        return request;
    }

    /**
     * Returns a request not intended for the REST interface.
     *
     * @param key - The app authentication key
     * @param cryptoUtils - An instance implementing ICryptoUtils
     *
     * @return A request not intended for the REST interface.
     */
    public static String getNoRestRequest(final SecretKey key,
            final ICryptoUtils cryptoUtils)
    {
        final Date now = new Date();
        final String timestamp = now.getTime() + "-" + now.getTime();
        // this is the header that should be signed
        final String header = "Location:www.examplecloud.com;"
                + "x-sec2-appName:test;x-sec2-nonce:62299332;"
                + "x-sec2-socketport:50001;"
                + "x-sec2-timestamp:" + timestamp + ";";
        String request = null;

        try
        {
            request = MessageFormat.format(NO_REST,
                    timestamp,TestRequests.sign(header, key, cryptoUtils));
        }
        catch(Exception e)
        {
            request = null;
        }

        return request;
    }
    
    /**
     * Returns a PUT request transmitting xml data
     * 
     * @param key - The app authentication key
     * @param cryptoUtils - An instance implementing ICryptoUtils
     *
     * @return a PUT request containing data.
     */
    public static String getPutRequestWithData(final SecretKey key,
            final ICryptoUtils cryptoUtils, final String location)
    {
        final Date now = new Date();
        final String timestamp = now.getTime() + "-" + now.getTime();
        // this is the header that should be signed
        final String header = "Location:" + location + ";"
                + "x-sec2-appName:test;x-sec2-nonce:62299332;"
                + "x-sec2-socketport:50001;"
                + "x-sec2-timestamp:" + timestamp + ";";
        String request = null;

        try
        {
            request = MessageFormat.format(getPutWithContent(location) + getLongDocument(
                    ConnectionHandlerTest.GROUP111_ID, 10000),
                    timestamp,TestRequests.sign(header, key, cryptoUtils));
        }
        catch(Exception e)
        {
            request = null;
        }

        return request;
    }
    
    //Signiert die Headers mittels HMAC und dem key als Schlüssel. Anschließend
    //wird die Signature Base64-kodiert zurückgeliefert.
    private static String sign(final String header, final SecretKey key,
            final ICryptoUtils cryptoUtils) throws GeneralSecurityException
            {

        final Mac macCreator = Mac.getInstance(key.getAlgorithm());;
        String signature = null;

        macCreator.init(key);
        signature = new String(cryptoUtils.encodeBase64(macCreator.doFinal(
                header.getBytes())));

        return signature;
            }

    private TestRequests(){}
    
    private static String getLongDocument(String groups, int byteLength) {
        String fill = "<abc>a</abc>";
        StringBuilder document = new StringBuilder(byteLength);
        document.append("<doc xmlns:xx=\"abc\">");
        document.append("<enc xmlns:sec2=\"").append(XMLConstants.SEC2_NS).
                append("\" sec2:Groups=\"").append(groups).append("\">");

        for (int i = 0; i < byteLength / fill.length(); i++) {
            document.append(fill); 
        }
        document.append("</enc>");
        document.append("</doc>");

        return document.toString();
    }
    
    private static String getPutWithContent(String location) {
        String out = PUT_WITH_CONTENT;
        out = out.replace("www.examplecloud.com", location);
        return out;
    }
}

package org.sec2.mwserver.core.xml;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.crypto.SecretKey;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.sec2.managers.beans.Group;
import org.sec2.managers.beans.User;
import org.sec2.managers.exceptions.OutOfSyncException;
import org.sec2.mwserver.core.util.ICryptoUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Creates the XML responses for the REST requests.
 *
 * @author schuessler
 *
 */
public class XmlResponseCreator {
    //The namespace

    private static final String NS_URI = "http://sec2.org/2012/03/middleware";
    //The prefix incl. ":"
    private static final String PREFIX = "sec2:";
    //The tagname "response"
    private static final String RESPONSE = "response";
    //The tagname "app-auth-key"
    private static final String APP_AUTH_KEY = "app-auth-key";
    //The tagname "app-auth-key-alg"
    private static final String APP_AUTH_KEY_ALG = "app-auth-key-alg";
    //The tagname "x-sec2-nonce"
    private static final String NONCE = "x-sec2-nonce";
    //The tagname "user"
    private static final String USER = "user";
    //The tagname "useridentifier"
    private static final String USER_ID = "useridentifier";
    //The tagname "useremail"
    private static final String EMAIL = "useremail";
    //The tagname "users"
    private static final String USERS = "users";
    //The tagname "groups"
    private static final String GROUPS = "groups";
    //The tagname "group"
    private static final String GROUP = "group";
    //The tagname "groupidentifier"
    private static final String GROUP_ID = "groupidentifier";
    //The tagname "groupname"
    private static final String GROUP_NAME = "groupname";
    //The tagname "groupowner"
    private static final String GROUP_OWNER = "groupowner";
    //The tagname "original"
    private static final String ORIGINAL = "original";

    //Private to prevent creation of objects of this class.
    private XmlResponseCreator() {
    }

    /**
     * Creates the response for the App registration request coming in over the
     * REST-interface using the URL /register. The response has the form
     *
     * <?xml version="1.0" encoding="UTF-8"?> <sec2:response
     * xmlns:sec2=http://sec2.org/2012/03/middleware"> <sec2:app-auth-key>[Key
     * in hex format]</sec2:app-auth-key>
     * <sec2:app-auth-key-alg>[Algorithm]</sec2:app-auth-key-alg>
     * <sec2:x-sec2-nonce>[99999999]</sec2:x-sec2-nonce> </sec2:response>
     *
     * @param key - The key to be returned in the response
     * @param nonce - The nonce to be returned in the response
     * @param cryptoUtils - An implementation of interface "CryptoUtils", which
     * is needed for encoding of the app-authorisation-key into its hexadecimal
     * representation
     *
     * @return The XML response
     *
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    public static String createRegisterResponse(final SecretKey key,
            final String nonce, final ICryptoUtils cryptoUtils)
            throws ParserConfigurationException, TransformerException {
        final Document xmlResponse = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        Element root = null;
        Element leaf = null;

        xmlResponse.setXmlStandalone(true);
        root = xmlResponse.createElementNS(NS_URI, PREFIX + RESPONSE);
        xmlResponse.appendChild(root);
        leaf = xmlResponse.createElement(PREFIX + APP_AUTH_KEY);
        leaf.setTextContent(cryptoUtils.encodeSecretKeyAsHex(key, true));
        root.appendChild(leaf);
        leaf = xmlResponse.createElement(PREFIX + APP_AUTH_KEY_ALG);
        leaf.setTextContent(key.getAlgorithm());
        root.appendChild(leaf);
        leaf = xmlResponse.createElement(PREFIX + NONCE);
        leaf.setTextContent(nonce);
        root.appendChild(leaf);
        xmlResponse.normalizeDocument();

        return convertDomToString(new DOMSource(xmlResponse));
    }

    /**
     * Creates the response for 1. the request of getting the registered user
     * which incomes over the REST-interface using the URL
     * /usermanagement/registereduser 2. the request of getting a user which
     * incomes over the REST interface using the URL /usermanagement/user
     *
     * The response has the form
     *
     * <?xml version="1.0" encoding="UTF-8"?> <sec2:response
     * xmlns:sec2=http://sec2.org/2012/03/middleware"> <sec2:user>
     * <sec2:useridentifier>[UserID]</sec2:useridentifier>
     * <sec2:useremail>[Email]</sec2:useremail> </sec2:user>
     * <sec2:x-sec2-nonce>[99999999]</sec2:x-sec2-nonce> </sec2:response>
     *
     * @param userObject - The registered user
     * @param nonce - The nonce to be returned in the response
     * @param cryptoUtils - An implementation of interface "CryptoUtils", which
     * is needed for encoding of the user ID into its base64 string
     * representation
     *
     * @return The XML response
     *
     * @throws ParserConfigurationException
     * @throws TransformerException If parameter userObject is NULL.
     */
    public static String createGetUserResponse(final User userObject,
            final String nonce, final ICryptoUtils cryptoUtils)
            throws ParserConfigurationException, TransformerException {
        Document xmlResponse = null;
        Element root = null;
        Element user = null;
        Element leaf = null;

        if (userObject == null) {
            throw new TransformerException(
                    "Das \"User\"-Objekt darf nicht NULL sein!");
        }
        xmlResponse = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .newDocument();
        xmlResponse.setXmlStandalone(true);
        root = xmlResponse.createElementNS(NS_URI, PREFIX + RESPONSE);
        xmlResponse.appendChild(root);
        user = xmlResponse.createElement(PREFIX + USER);
        root.appendChild(user);
        leaf = xmlResponse.createElement(PREFIX + USER_ID);
        leaf.setTextContent(cryptoUtils.encodeBase64(userObject.getUserID()));
        user.appendChild(leaf);
        leaf = xmlResponse.createElement(PREFIX + EMAIL);
        leaf.setTextContent(userObject.getEmailAddress());
        user.appendChild(leaf);
        leaf = xmlResponse.createElement(PREFIX + NONCE);
        leaf.setTextContent(nonce);
        root.appendChild(leaf);
        xmlResponse.normalizeDocument();

        return convertDomToString(new DOMSource(xmlResponse));
    }

    /**
     * Creates the response for the request of getting a group which incomes
     * over the REST-interface using the URL /usermanagement/group.
     *
     * The response has the form
     *
     * <?xml version="1.0" encoding="UTF-8"?> <sec2:response
     * xmlns:sec2=http://sec2.org/2012/03/middleware"> <sec2:group>
     * <sec2:groupidentifier>[GroupID]</sec2:groupidentifier>
     * <sec2:groupname>[GroupName]</sec2:groupname> <sec2:groupowner>
     * <sec2:useridentifier>[UserID]</sec2:useridentifier>
     * <sec2:useremail>[Email]</sec2:useremail> </sec2:groupowner> </sec2:group>
     * <sec2:x-sec2-nonce>[99999999]</sec2:x-sec2-nonce> </sec2:response>
     *
     * @param groupObject - The group object
     * @param nonce - The nonce to be returned in the response
     * @param cryptoUtils - An implementation of interface "CryptoUtils", which
     * is needed for encoding of the user ID into its base64 string
     * representation
     *
     * @return The XML response
     *
     * @throws ParserConfigurationException
     * @throws TransformerException If groupObject is NULL or not synchronised
     * anymore.
     */
    public static String createGetGroupResponse(final Group groupObject,
            final String nonce, final ICryptoUtils cryptoUtils)
            throws ParserConfigurationException, TransformerException {
        Document xmlResponse = null;
        Element root = null;
        Element group = null;
        Element leaf = null;
        Element owner = null;

        if (groupObject == null) {
            throw new TransformerException(
                    "Das \"Group\"-Objekt darf nicht NULL sein!");
        }
        xmlResponse = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        xmlResponse.setXmlStandalone(true);
        root = xmlResponse.createElementNS(NS_URI, PREFIX + RESPONSE);
        xmlResponse.appendChild(root);
        group = xmlResponse.createElement(PREFIX + GROUP);
        root.appendChild(group);
        leaf = xmlResponse.createElement(PREFIX + GROUP_ID);
        leaf.setTextContent(String.valueOf(groupObject.getGroupName()));
        group.appendChild(leaf);
        leaf = xmlResponse.createElement(PREFIX + GROUP_NAME);
        leaf.setTextContent(groupObject.getGroupName());
        group.appendChild(leaf);
        owner = xmlResponse.createElement(PREFIX + GROUP_OWNER);
        group.appendChild(owner);
        leaf = xmlResponse.createElement(PREFIX + USER_ID);
        leaf.setTextContent(cryptoUtils.encodeBase64(
                groupObject.getOwner().getUserID()));
        owner.appendChild(leaf);
        leaf = xmlResponse.createElement(PREFIX + EMAIL);
        leaf.setTextContent(groupObject.getOwner().getEmailAddress());
        owner.appendChild(leaf);
        leaf = xmlResponse.createElement(PREFIX + NONCE);
        leaf.setTextContent(nonce);
        root.appendChild(leaf);
        xmlResponse.normalizeDocument();

        return convertDomToString(new DOMSource(xmlResponse));
    }

    /**
     * Creates the response for 1. the request of getting all known users which
     * incomes over the REST- interface using the URL
     * /usermanagement/knownuserslist. 2. the request of getting all users of a
     * group which incomes over the REST-interface using the URL
     * /usermanagement/usersingrouplist
     *
     * The response has the form
     *
     * <?xml version="1.0" encoding="UTF-8"?> <sec2:response
     * xmlns:sec2=http://sec2.org/2012/03/middleware"> <sec2:users> <sec2:user>
     * <sec2:useridentifier>[UserID]</sec2:useridentifier>
     * <sec2:useremail>[Email]</sec2:useremail> </sec2:user> . . . </sec2:users>
     * <sec2:x-sec2-nonce>[99999999]</sec2:x-sec2-nonce> </sec2:response>
     *
     * @param userList - The list with all users to be returned in the response
     * @param nonce - The nonce to be returned in the response
     * @param cryptoUtils - An implementation of interface "CryptoUtils", which
     * is needed for encoding of the user ID into its base64 string
     * representation
     *
     * @return The XML response
     *
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    public static String createGetUsersResponse(final List<User> userList,
            final String nonce, final ICryptoUtils cryptoUtils)
            throws ParserConfigurationException, TransformerException {
        final Document xmlResponse = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        Element root = null;
        Element users = null;
        Element user = null;
        Element leaf = null;
        User userObject = null;

        xmlResponse.setXmlStandalone(true);
        root = xmlResponse.createElementNS(NS_URI, PREFIX + RESPONSE);
        xmlResponse.appendChild(root);
        users = xmlResponse.createElement(PREFIX + USERS);
        root.appendChild(users);
        if (userList != null) {
            for (int i = 0; i < userList.size(); i++) {
                userObject = userList.get(i);
                user = xmlResponse.createElement(PREFIX + USER);
                users.appendChild(user);
                leaf = xmlResponse.createElement(PREFIX + USER_ID);
                leaf.setTextContent(cryptoUtils.encodeBase64(
                        userObject.getUserID()));
                user.appendChild(leaf);
                leaf = xmlResponse.createElement(PREFIX + EMAIL);
                leaf.setTextContent(userObject.getEmailAddress());
                user.appendChild(leaf);
            }
        }
        leaf = xmlResponse.createElement(PREFIX + NONCE);
        leaf.setTextContent(nonce);
        root.appendChild(leaf);
        xmlResponse.normalizeDocument();

        return convertDomToString(new DOMSource(xmlResponse));
    }

    /**
     * Creates the response for 1. the request of getting all groups where a
     * user is member which incomes over the REST interface using the URL
     * /usermanagement/groupsforuserlist 2. the request of getting all known
     * groups which incomes over the REST- interface using the URL
     * /usermanagement/grouplist
     *
     * The response has the form
     *
     * <?xml version="1.0" encoding="UTF-8"?> <sec2:response
     * xmlns:sec2=http://sec2.org/2012/03/middleware"> <sec2:groups>
     * <sec2:group> <sec2:groupidentifier>[GroupID]</sec2:groupidentifier>
     * <sec2:groupname>[Groupname]</sec2:groupname> </sec2:group> . . .
     * </sec2:groups> <sec2:x-sec2-nonce>[99999999]</sec2:x-sec2-nonce>
     * </sec2:response>
     *
     * @param groupList - The list with all groups to be returned in the
     * response
     * @param nonce - The nonce to be returned in the response
     *
     * @return The XML response
     *
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    public static String createGetGroupsResponse(final List<Group> groupList,
            final String nonce) throws ParserConfigurationException,
            TransformerException {
        final Document xmlResponse = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        Element root = null;
        Element groups = null;
        Element group = null;
        Element leaf = null;
        Group groupObject = null;

        xmlResponse.setXmlStandalone(true);
        root = xmlResponse.createElementNS(NS_URI, PREFIX + RESPONSE);
        xmlResponse.appendChild(root);
        groups = xmlResponse.createElement(PREFIX + GROUPS);
        root.appendChild(groups);
        if (groupList != null) {
            for (int i = 0; i < groupList.size(); i++) {
                groupObject = groupList.get(i);
                group = xmlResponse.createElement(PREFIX + GROUP);
                groups.appendChild(group);
                leaf = xmlResponse.createElement(PREFIX + GROUP_ID);
                leaf.setTextContent(String.valueOf(
                        groupObject.getGroupName()));
                group.appendChild(leaf);
                leaf = xmlResponse.createElement(PREFIX + GROUP_NAME);
                leaf.setTextContent(groupObject.getGroupName());
                group.appendChild(leaf);
            }
        }
        leaf = xmlResponse.createElement(PREFIX + NONCE);
        leaf.setTextContent(nonce);
        root.appendChild(leaf);
        xmlResponse.normalizeDocument();

        return convertDomToString(new DOMSource(xmlResponse));
    }

    /**
     * Creates the content for the response for a request which was forwarded to
     * the cloud. The original content from the original response from the
     * cloud-server is encoded base64. If no original content was passed to the
     * method, the <sec2:original>-part is omited.
     *
     * The response has the form
     * 
     * TODO: do we need base64 encoding??? (we have to check application and the management
     * app if they need base64 and (maybe) remove it)
     *
     * <?xml version="1.0" encoding="UTF-8"?> <sec2:response
     * xmlns:sec2=http://sec2.org/2012/03/middleware"> <sec2:original>[Original
     * content base64 encoded]</sec2:original>
     * <sec2:x-sec2-nonce>[99999999]</sec2:x-sec2-nonce> </sec2:response>
     *
     * @param originalContent - The original content from the original response
     * from the cloud-server.
     * @param nonce - The nonce to be returned in the response
     * @param cryptoUtils - An implementation of interface "CryptoUtils", which
     * is needed for encoding of the original content into its base64 string
     * representation
     *
     * @return The XML response
     *
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    public static String createRedirectResponse(final String originalContent,
            final String nonce, final ICryptoUtils cryptoUtils)
            throws ParserConfigurationException, TransformerException {
        final Document xmlResponse = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        Element root = null;
        Element leaf = null;

        xmlResponse.setXmlStandalone(true);
        root = xmlResponse.createElementNS(NS_URI, PREFIX + RESPONSE);
        xmlResponse.appendChild(root);
        if (originalContent != null && !originalContent.isEmpty()) {
            try {
                leaf = xmlResponse.createElement(PREFIX + ORIGINAL);
                leaf.setTextContent(cryptoUtils.encodeBase64(
                        originalContent.getBytes("utf-8")));
                root.appendChild(leaf);
            } catch (final UnsupportedEncodingException uee) {
                throw new TransformerException(uee);
            }
        }
        leaf = xmlResponse.createElement(PREFIX + NONCE);
        leaf.setTextContent(nonce);
        root.appendChild(leaf);
        xmlResponse.normalizeDocument();

        return convertDomToString(new DOMSource(xmlResponse));
    }

    /*
     * Convertes a DOM document into its String representation
     */
    private static String convertDomToString(final DOMSource source)
            throws TransformerException {
        final Transformer transformer =
                TransformerFactory.newInstance().newTransformer();
        final StringWriter writer = new StringWriter();

        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(source, new StreamResult(writer));

        return writer.toString();
    }
}

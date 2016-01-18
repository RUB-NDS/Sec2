package org.sec2.mwserver.core.xml;

/**
 * Class providing strings of XML-responses from the Sec2-middleware.
 *
 * @author nike
 */
public final class TestXmlResponses
{
    /**
     * Response after calling REST-function /register.
     */
    public static final String XML_REGISTER =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<sec2:response xmlns:sec2="
                    + "\"http://sec2.org/2012/03/middleware\">\n"
                    + "<sec2:app-auth-key>a6468b7f2a0c81b63bbb63aa2155355ec24b"
                    + "6cf9047c1738ba84fd882eea18a774c36df6751ccb43bfc0184d88d"
                    + "4c29f59cfd68b4b0f0beda739f3cf9ea0a80f"
                    + "</sec2:app-auth-key>\n"
                    + "<sec2:app-auth-key-alg>HMACSHA512"
                    + "</sec2:app-auth-key-alg>\n"
                    + "<sec2:x-sec2-nonce>123456789</sec2:x-sec2-nonce>\n"
                    + "</sec2:response>";

    /**
     * Response after calling REST-function /usermanagement/registereduser or
     * /usermanagement/user.
     */
    public static final String XML_USER =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<sec2:response xmlns:sec2="
                    + "\"http://sec2.org/2012/03/middleware\">\n"
                    + "<sec2:user>\n"
                    + "<sec2:useridentifier>Cg==</sec2:useridentifier>\n"
                    + "<sec2:useremail>nike@sec2.org</sec2:useremail>\n"
                    + "</sec2:user>\n"
                    + "<sec2:x-sec2-nonce>123456789</sec2:x-sec2-nonce>\n"
                    + "</sec2:response>";

    /**
     * Response after calling REST-function /usermanagement/group.
     */
    public static final String XML_GROUP =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<sec2:response xmlns:sec2="
                    + "\"http://sec2.org/2012/03/middleware\">\n"
                    + "<sec2:group>\n"
                    + "<sec2:groupidentifier>Familie</sec2:groupidentifier>\n"
                    + "<sec2:groupname>Familie</sec2:groupname>\n"
                    + "<sec2:groupowner>\n"
                    + "<sec2:useridentifier>Cg==</sec2:useridentifier>\n"
                    + "<sec2:useremail>nike@sec2.org</sec2:useremail>\n"
                    + "</sec2:groupowner>\n"
                    + "</sec2:group>\n"
                    + "<sec2:x-sec2-nonce>123456789</sec2:x-sec2-nonce>\n"
                    + "</sec2:response>";

    /**
     * Response after calling REST-function /usermanagement/knownuserslist or
     * /usermanagement/usersingrouplist with empty list of users.
     */
    public static final String XML_USERS_EMPTY =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<sec2:response xmlns:sec2="
                    + "\"http://sec2.org/2012/03/middleware\">\n"
                    + "<sec2:users/>\n"
                    + "<sec2:x-sec2-nonce>123456789</sec2:x-sec2-nonce>\n"
                    + "</sec2:response>";

    /**
     * Response after calling REST-function /usermanagement/knownuserslist or
     * /usermanagement/usersingrouplist with two users in list of users.
     */
    public static final String XML_USERS =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<sec2:response xmlns:sec2="
                    + "\"http://sec2.org/2012/03/middleware\">\n"
                    + "<sec2:users>\n"
                    + "<sec2:user>\n"
                    + "<sec2:useridentifier>Cg==</sec2:useridentifier>\n"
                    + "<sec2:useremail>nike@sec2.org</sec2:useremail>\n"
                    + "</sec2:user>\n"
                    + "<sec2:user>\n"
                    + "<sec2:useridentifier>Cw==</sec2:useridentifier>\n"
                    + "<sec2:useremail>lena@sec2.org</sec2:useremail>\n"
                    + "</sec2:user>\n"
                    + "</sec2:users>\n"
                    + "<sec2:x-sec2-nonce>123456789</sec2:x-sec2-nonce>\n"
                    + "</sec2:response>";

    /**
     * Response after calling REST-function /usermanagement/groupsforuserlist
     * or /usermanagement/grouplist with empty list of groups.
     */
    public static final String XML_GROUPS_EMPTY =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<sec2:response xmlns:sec2="
                    + "\"http://sec2.org/2012/03/middleware\">\n"
                    + "<sec2:groups/>\n"
                    + "<sec2:x-sec2-nonce>123456789</sec2:x-sec2-nonce>\n"
                    + "</sec2:response>";

    /**
     * Response after calling REST-function /usermanagement/groupsforuserlist
     * or /usermanagement/grouplist with two groups in list of groups.
     */
    public static final String XML_GROUPS =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<sec2:response xmlns:sec2="
                    + "\"http://sec2.org/2012/03/middleware\">\n"
                    + "<sec2:groups>\n"
                    + "<sec2:group>\n"
                    + "<sec2:groupidentifier>Familie</sec2:groupidentifier>\n"
                    + "<sec2:groupname>Familie</sec2:groupname>\n"
                    + "</sec2:group>\n"
                    + "<sec2:group>\n"
                    + "<sec2:groupidentifier>Freunde</sec2:groupidentifier>\n"
                    + "<sec2:groupname>Freunde</sec2:groupname>\n"
                    + "</sec2:group>\n"
                    + "</sec2:groups>\n"
                    + "<sec2:x-sec2-nonce>123456789</sec2:x-sec2-nonce>\n"
                    + "</sec2:response>";

    /**
     * Response when middleware acts as an proxy and no original content is
     * available.
     */
    public static final String XML_PROXY_EMPTY =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<sec2:response xmlns:sec2="
                    + "\"http://sec2.org/2012/03/middleware\">\n"
                    + "<sec2:x-sec2-nonce>123456789</sec2:x-sec2-nonce>\n"
                    + "</sec2:response>";

    /**
     * Response when middleware acts as an proxy and original content is
     * available.
     */
    public static final String XML_PROXY =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<sec2:response xmlns:sec2="
                    + "\"http://sec2.org/2012/03/middleware\">\n"
                    + "<sec2:original>SGVsbG8gV29ybGQ=</sec2:original>\n"
                    + "<sec2:x-sec2-nonce>123456789</sec2:x-sec2-nonce>\n"
                    + "</sec2:response>";
}

package org.sec2.mwserver.core.xml;

import java.util.ArrayList;
import javax.crypto.spec.SecretKeySpec;
import junit.framework.TestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.sec2.managers.beans.Group;
import org.sec2.managers.beans.User;
import org.sec2.mwserver.core.TestCryptoUtils;

/**
 * This JUnit-Class tests the methods of class XmlResponseCreator.
 *
 * @author nike
 */
public class TestXmlResponseCreator extends TestCase {

    private static final byte[] KEY = new byte[]{-90, 70, -117, 127, 42,
        12, -127, -74, 59, -69, 99, -86, 33, 85, 53, 94, -62, 75, 108, -7,
        4, 124, 23, 56, -70, -124, -3, -120, 46, -22, 24, -89, 116, -61,
        109, -10, 117, 28, -53, 67, -65, -64, 24, 77, -120, -44, -62, -97,
        89, -49, -42, -117, 75, 15, 11, -19, -89, 57, -13, -49, -98, -96,
        -88, 15};
    private static final String ALGORITHM = "HMACSHA512";
    private static final String FORMAT = "RAW";
    private static final String NONCE = "123456789";
    private TestCryptoUtils cryptoUtils = new TestCryptoUtils();
    private User user = new User(new byte[]{10}, "nike@sec2.org");

    /**
     * This method tests the createRegisterResponse()-method, if it returns the
     * expected response.
     */
    public void testCreateRegisterResponse() {
        try {
            assertTrue(XMLUnit.compareXML(TestXmlResponses.XML_REGISTER,
                    XmlResponseCreator.createRegisterResponse(
                    new TestSecretKey(), NONCE, cryptoUtils)).identical());
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * This method tests the createGetUserResponse()-method, if it returns the
     * expected response.
     */
    public void testCreateGetUserResponse() {
        try {
            assertTrue(XMLUnit.compareXML(TestXmlResponses.XML_USER,
                    XmlResponseCreator.createGetUserResponse(user, NONCE,
                    cryptoUtils)).identical());
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * This method tests the createGetGroupResponse()-method, if it returns the
     * expected response.
     */
    public void testCreateGetGroupResponse() {
        Group group =
                new Group("Familie", user, new ArrayList<byte[]>());

        group.setSynced(true);
        try {
            assertTrue(XMLUnit.compareXML(TestXmlResponses.XML_GROUP,
                    XmlResponseCreator.createGetGroupResponse(group, NONCE,
                    cryptoUtils)).identical());
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * This method tests the createGetUsersResponse()-method, if it returns the
     * expected response, when the list of users is empty.
     */
    public void testCreateGetUsersResponseEmpty() {
        try {
            assertTrue(XMLUnit.compareXML(TestXmlResponses.XML_USERS_EMPTY,
                    XmlResponseCreator.createGetUsersResponse(null, NONCE,
                    cryptoUtils)).identical());
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * This method tests the createGetUsersResponse()-method, if it returns the
     * expected response, when the list of users contains two users.
     */
    public void testCreateGetUsersResponse() {
        ArrayList<User> list = new ArrayList<User>();

        list.add(user);
        list.add(new User(new byte[]{11}, "lena@sec2.org"));
        try {
            assertTrue(XMLUnit.compareXML(TestXmlResponses.XML_USERS,
                    XmlResponseCreator.createGetUsersResponse(list, NONCE,
                    cryptoUtils)).identical());
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * This method tests the createGetGroupsResponse()-method, if it returns the
     * expected response, when the list of groups contains is empty.
     */
    public void testCreateGetGroupsResponseEmpty() {
        try {
            assertTrue(XMLUnit.compareXML(TestXmlResponses.XML_GROUPS_EMPTY,
                    XmlResponseCreator.createGetGroupsResponse(null, NONCE)).identical());
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * This method tests the createGetGroupsResponse()-method, if it returns the
     * expected response, when the list of group contains two groups.
     */
    public void testCreateGetGroupsResponse() {
        ArrayList<Group> list = new ArrayList<Group>();
        Group groupFamily =
                new Group("Familie", user, new ArrayList<byte[]>());
        Group groupFriends =
                new Group("Freunde", user, new ArrayList<byte[]>());

        groupFamily.setSynced(true);
        groupFriends.setSynced(true);
        list.add(groupFamily);
        list.add(groupFriends);
        try {
            assertTrue(XMLUnit.compareXML(TestXmlResponses.XML_GROUPS,
                    XmlResponseCreator.createGetGroupsResponse(list, NONCE)).identical());
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * This method tests the createRedirectResponse()-method, if it returns the
     * expected response, when no original content is available.
     */
    public void testCreateRedirectResponseEmpty() {
        try {
            assertTrue(XMLUnit.compareXML(TestXmlResponses.XML_PROXY_EMPTY,
                    XmlResponseCreator.createRedirectResponse(null, NONCE,
                    cryptoUtils)).identical());
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * This method tests the createRedirectResponse()-method, if it returns the
     * expected response, when original content is available.
     */
    public void testCreateRedirectResponse() {
        try {
            assertTrue(XMLUnit.compareXML(TestXmlResponses.XML_PROXY,
                    XmlResponseCreator.createRedirectResponse("Hello World",
                    NONCE, cryptoUtils)).identical());
        } catch (Exception e) {
            fail();
        }
    }

    private final class TestSecretKey extends SecretKeySpec {

        private static final long serialVersionUID = -3569325883119680687L;
        private final String format;

        public TestSecretKey() {
            super(KEY, ALGORITHM);
            this.format = FORMAT;
        }

        @Override
        public String getFormat() {
            return format;
        }
    }
}

package org.sec2.backend.keyserver.test;

import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.Random;

import junit.framework.TestCase;

import org.sec2.backend.IGroupInfo;
import org.sec2.backend.exceptions.PermissionException;
import org.sec2.backend.impl.ConfigurationFactory;
import org.sec2.backend.impl.DatabaseServer;
import org.sec2.backend.impl.KeyServer;
import org.sec2.backend.impl.UserManagement;
import org.sec2.backend.test.TestUtil;
import org.sec2.statictestdata.TestKeyProvider;

public class GetGroupInfoTest extends TestCase {

    private KeyServer keyserver;
    private UserManagement um;
    private TestKeyProvider keyProvider;
    
    public GetGroupInfoTest(String name) {
        super(name);
    }
    
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.restoreDatabase();
        TestUtil.createUserAndConfirmEmail();
        this.keyProvider = TestKeyProvider.getInstance();
        Properties configuration = ConfigurationFactory.createDefault();
        DatabaseServer databaseServer = new DatabaseServer(configuration);
        this.keyserver = new KeyServer(databaseServer, configuration);
        this.um = new UserManagement(configuration);
    }



    /**
     * 
     */
    public void testGetGroupInfoWithNullUser() {
        System.out.println("begin testGetGroupInfoWithNullUser:");
        byte[] userId = null;
        String groupName = "";
        X509Certificate certificate = null;
        PermissionException exception = null;
        try {
            keyserver.getGroupInfo(userId, groupName, certificate);
        }
        catch (PermissionException e) {
            exception = e;
        }
        assertNotNull(exception);
        System.out.println("end testGetGroupInfoWithNullUser:");
    }
    
    /**
     * A user tries to retrieve information about a group but is no member of 
     * that group. 
     * 
     * The keyserver must throw a {@link PermissionException} in this case.
     */
    public void testGetGroupInfoWithNonExistingUser() {
        System.out.println("begin testGetGroupInfoWithNonExistingUser:");
        Random random = new Random();
        byte[] userId = new byte[16];
        random.nextBytes(userId);
        String groupName = "testgroup";
        X509Certificate certificate = null;
        IGroupInfo groupInfo = null;
        PermissionException exception = null;
        try {
            groupInfo = keyserver.getGroupInfo(userId, groupName, certificate);
        }
        catch (PermissionException e) {
            exception = e;
        }
        assertNotNull("The keyserver does not throw a PermissionException " +
                "if the user who's not in a group asks for information about " +
                "it.", exception);
        assertNull(groupInfo);
        System.out.println("end testGetGroupInfoWithNonExistingUser:");
    }
    
    public void testGetGroupInfo() {
        System.out.println("begin testGetGroupInfo:");
        byte[] userId = keyProvider.getUserID();
        X509Certificate certificate = keyProvider.getUserEncCert();
        IGroupInfo groupInfo = null;
        try {
            groupInfo = keyserver.getGroupInfo(userId, "user@sec2.org", certificate);
        }
        catch (PermissionException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertNotNull(groupInfo);
        System.out.println("end testGetGroupInfo:");
    }

}

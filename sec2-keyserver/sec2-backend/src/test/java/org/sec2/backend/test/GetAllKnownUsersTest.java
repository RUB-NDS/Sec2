package org.sec2.backend.test;

import java.util.List;
import java.util.Properties;

import org.sec2.backend.IGroupInfo;
import org.sec2.backend.IUserInfo;
import org.sec2.backend.exceptions.GroupAlreadyExistsException;
import org.sec2.backend.exceptions.InvalidGroupNameException;
import org.sec2.backend.exceptions.PermissionException;
import org.sec2.backend.exceptions.UserNotFoundException;
import org.sec2.backend.impl.ConfigurationFactory;
import org.sec2.backend.impl.DatabaseServer;
import org.sec2.backend.impl.KeyServer;
import org.sec2.backend.impl.UserManagement;
import org.sec2.statictestdata.TestKeyProvider;

import junit.framework.TestCase;

public class GetAllKnownUsersTest extends TestCase {

    private KeyServer keyserver;
    private UserManagement um;
    private TestKeyProvider keyProvider;
    
    @Override
    protected void setUp() throws Exception {
        TestUtil.restoreDatabase();
        TestUtil.createUserAndConfirmEmail();
        TestUtil.createUser2AndConfirmEmail();
        Properties configuration = ConfigurationFactory.createDefault();
        DatabaseServer databaseServer = new DatabaseServer(configuration);
        this.keyProvider = TestKeyProvider.getInstance();
        this.keyserver = new KeyServer(databaseServer, configuration);
        this.um = new UserManagement(configuration);
        super.setUp();
    }
    
    public void testGetAllKnownUsers() {
        System.out.println("begin testGetAllKnownUsers:");

        IGroupInfo group;
        try {
            group = um.createGroup(keyProvider.getUserID(), "getAllKnownUsersTest");
            IUserInfo user2 = um.getUserInfo(keyProvider.getUser2ID());
            group.getMembers().add(user2);
            um.modifyGroup(keyProvider.getUserID(), group);
            List<IUserInfo> knownUsers = um.getAllKnownUsers(keyProvider.getUserID());
            
            for(IUserInfo user: knownUsers) {
                System.out.println(user.getEmailAddress());
            }
            
            assertTrue(knownUsers.contains(user2));
            
            
        }
        catch (GroupAlreadyExistsException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        catch (UserNotFoundException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        catch (InvalidGroupNameException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        catch (PermissionException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        
        System.out.println("end testGetAllKnownUsers:");
    }
    
    
}

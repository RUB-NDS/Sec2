package org.sec2.backend.test;

import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

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

public class GetAllKnownGroupsTest extends TestCase {
    private KeyServer keyserver;
    private UserManagement um;
    private TestKeyProvider keyProvider;
    
    @Override
    protected void setUp() throws Exception {
        TestUtil.restoreDatabase();
        TestUtil.createUserAndConfirmEmail();
        Properties configuration = ConfigurationFactory.createDefault();
        DatabaseServer databaseServer = new DatabaseServer(configuration);
        this.keyProvider = TestKeyProvider.getInstance();
        this.keyserver = new KeyServer(databaseServer, configuration);
        this.um = new UserManagement(configuration);
        super.setUp();
    }
    
    public void testGetAllKnownGroups() {
        System.out.println("begin testGetAllKnownGroups:");

        try {
            um.createGroup(keyProvider.getUserID(), "GroupA");
            um.createGroup(keyProvider.getUserID(), "GroupB");
            um.createGroup(keyProvider.getUserID(), "GroupC");
            List<IGroupInfo> groups = um.getAllKnownGroups(keyProvider.getUserID());
            
            for(IGroupInfo group: groups) {
                System.out.println(group.getGroupName());
            }
            
            assertEquals(4, groups.size());
            
            
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
        System.out.println("end testGetAllKnownGroups:");
        
    }
    
}

package org.sec2.backend.test;

import junit.framework.TestCase;

import org.sec2.backend.IGroupInfo;
import org.sec2.backend.exceptions.GroupAlreadyExistsException;
import org.sec2.backend.exceptions.InvalidGroupNameException;
import org.sec2.backend.exceptions.PermissionException;
import org.sec2.backend.exceptions.UserNotFoundException;
import org.sec2.backend.impl.ConfigurationFactory;
import org.sec2.backend.impl.UserManagement;
import org.sec2.statictestdata.TestKeyProvider;

public class CreateGroupTest extends TestCase {

    private UserManagement um;
    private byte[] userPKC;
    private byte[] userId;
    private TestKeyProvider keyProvider;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.restoreDatabase();
        TestUtil.createUserAndConfirmEmail();
        keyProvider = TestKeyProvider.getInstance();
        um = new UserManagement(ConfigurationFactory.createDefault());
        assertTrue(um != null);
        userPKC = keyProvider.getUserSignCert().getEncoded();
        userId = TestUtil.calculateUserId(userPKC);
    }

   
    /**
     * Create group with name "test". This results in a new group.
     */
    public void testCreateGroupWithValidName() {
        System.out.println("begin testCreateGroupWithValidName:");
        IGroupInfo groupInfo = null;
        try {
            groupInfo = um.createGroup(userId, "test");
        }
        catch (GroupAlreadyExistsException e) {
            assertFalse("The group should not already exist", true);
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        
        assertEquals("Group name should equals 'test'", groupInfo
                .getGroupName(), "test");
        System.out.println("end testCreateGroupWithValidName:");
    }
    
    /**
     * Create group with name "test" again. 
     * This results in an GroupAlreadyExistsException.
     */
    public void testCreateGroupThatAlreadyExists() {
        System.out.println("begin testCreateGroupThatAlreadyExists:");
        // create group
        try {
            um.createGroup(userId, "test");
        }
        catch (Exception e) {}
        
        // create group again 
        IGroupInfo groupInfo = null;
        Exception exception = null;
        try {
            groupInfo = um.createGroup(userId, "test");
        }
        catch (GroupAlreadyExistsException e) {
            exception = e;
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
        assertNotNull(exception);
        assertNull("The group should already exist", groupInfo);
        System.out.println("end testCreateGroupThatAlreadyExists:");
    }
    
    /**
     *  Create group with empty userId. 
     *  This results in an UserNotFoundException.
     */
    public void testCreateGroupWithEmptyUserId() {
        System.out.println("begin testCreateGroupWithEmptyUserId:");
        IGroupInfo groupInfo = null;
        Exception exception = null;
        try {
            groupInfo = um.createGroup(null, "another");
        }
        catch (GroupAlreadyExistsException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        catch (UserNotFoundException e) {
            exception = e;
        }
        catch (InvalidGroupNameException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        catch (PermissionException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertNotNull(exception);
        assertNull(groupInfo);

        System.out.println("end testCreateGroupWithEmptyUserId.");
    }
    

    /**
     *  Create group with empty name. 
     *  This results in an InvalidGroupNameException.
     */
    public void testCreateGroupWithEmptyName() {
        System.out.println("begin testCreateGroupWithEmptyName:");

        IGroupInfo groupInfo = null;
        Exception exception = null;
        try {
            groupInfo = um.createGroup(userId, "");
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
            exception = e;
        }
        catch (PermissionException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertNotNull(exception);
        assertNull(groupInfo);
        System.out.println("end testCreateGroupWithEmptyName:");
    }

}

package org.sec2.backend.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.sec2.backend.IGroupInfo;
import org.sec2.backend.IUserInfo;
import org.sec2.backend.exceptions.PermissionException;
import org.sec2.backend.exceptions.UserNotFoundException;
import org.sec2.backend.impl.ConfigurationFactory;
import org.sec2.backend.impl.UserInfo;
import org.sec2.backend.impl.UserManagement;
import org.sec2.statictestdata.TestKeyProvider;

public class ModifyGroupTest  extends TestCase {

    private UserManagement um;
    private byte[] userPKC;
    private byte[] userId;
    private byte[] user2PKC;
    private byte[] user2Id;
    private TestKeyProvider keyProvider;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.restoreDatabase();
        TestUtil.createUser2AndConfirmEmail();
        TestUtil.createUserAndConfirmEmail();
        keyProvider = TestKeyProvider.getInstance();
        um = new UserManagement(ConfigurationFactory.createDefault());
        assertTrue(um != null);
        userPKC = keyProvider.getUserSignCert().getEncoded();
        userId = TestUtil.calculateUserId(userPKC);
        assertTrue(Arrays.equals(userId, keyProvider.getUserID()));
        user2PKC = keyProvider.getUser2SignCert().getEncoded();
        user2Id = TestUtil.calculateUserId(user2PKC);
    }
    
    public void testModifyGroup() {
        // Create a group
        IGroupInfo group = null;
        IUserInfo user2, user = user2 = null;
        try {
            group = um.createGroup(userId, "modifyGroupTest");
            user = um.getUserInfo(userId);
            user2 = um.getUserInfo(user2Id);
            assertEquals(1, group.getMembers().size());
            group.getMembers().add(user2);
            assertEquals(2, group.getMembers().size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            um.modifyGroup(userId, group);
            
            IGroupInfo groupInfo = um.getGroupInfo(userId, group.getGroupName());
            assertTrue(groupInfo.getMembers().contains(user));
            assertTrue(groupInfo.getMembers().contains(user2));
        }
        catch (PermissionException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        catch (UserNotFoundException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        
        
    }
    

}

package org.sec2.backend.test;


import java.security.cert.CertificateEncodingException;

import junit.framework.TestCase;

import org.bouncycastle.util.Arrays;
import org.sec2.backend.IGroupInfo;
import org.sec2.backend.IUserInfo;
import org.sec2.backend.exceptions.PermissionException;
import org.sec2.backend.exceptions.UserNotFoundException;
import org.sec2.backend.impl.ConfigurationFactory;
import org.sec2.backend.impl.MailHelper;
import org.sec2.backend.impl.UserManagement;
import org.sec2.statictestdata.TestKeyProvider;

public class GetGroupInfoTest extends TestCase {

    private byte[] userId;
    private UserManagement um;
    private TestKeyProvider keyProvider = TestKeyProvider.getInstance();
    
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // fixtures
        TestUtil.restoreDatabase();
        TestUtil.createUserAndConfirmEmail();
        
        byte[] userPKC = keyProvider.getUserSignCert().getEncoded();
        userId = TestUtil.calculateUserId(userPKC);
        um = new UserManagement(ConfigurationFactory.createDefault());
        assertNotNull(um);
    }

    /**
     * Try to get group information from existing group.
     */
    public void testRetrieveGroupInfoFromExistingGroup() {
        System.out.println("begin testRetrieveGroupInfoFromExistingGroup:");
        String emailAddress = MailHelper.extractEmailAddress(keyProvider.getUserSignCert());
        IGroupInfo groupInfo = null;
        try {
            groupInfo = um.getGroupInfo(userId, emailAddress);
        }
        catch (UserNotFoundException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        catch (PermissionException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertNotNull(groupInfo);
        System.out.println(groupInfo.toString());
        assertEquals(groupInfo.getGroupName(), emailAddress);
        IUserInfo operator = groupInfo.getOperator();
        assertNotNull(operator);
        assertTrue(Arrays.areEqual(operator.getId(), userId));
        try {
            assertTrue(Arrays.areEqual(operator.getEncryptionPKC().getEncoded(), keyProvider.getUserEncCert().getEncoded()));
        }
        catch (CertificateEncodingException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        try {
            assertTrue(Arrays.areEqual(operator.getSignaturePKC().getEncoded(), keyProvider.getUserSignCert().getEncoded()));
        }
        catch (CertificateEncodingException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals(operator.getEmailAddress(), emailAddress);
        assertTrue(groupInfo.getMembers().size() == 1);
        for(IUserInfo user : groupInfo.getMembers()) {
            System.out.println(user.getEmailAddress());
        }
        System.out.println("end testRetrieveGroupInfoFromExistingGroup.");
    }
    
    /**
     * Try to get group information from non-existing group.
     */
    public void testRetrieveGroupInfoFromNonExistingGroup() {
        System.out.println("begin testRetrieveGroupInfoFromNonExistingGroup:");

        IGroupInfo groupInfo = null;
        boolean caughtPermissionException = false;
        try {
            groupInfo = um.getGroupInfo(userId, "non-existant");
        }
        catch (UserNotFoundException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        catch (PermissionException e) {
            caughtPermissionException = true;
        }
        assertNull(groupInfo);
        assertTrue(caughtPermissionException);
        System.out.println("end testRetrieveGroupInfoFromNonExistingGroup.");
    }
    
    /**
     * Try to get group information with non-existing user.
     */
    public void testRetrieveGroupInfoWithNonExistingUser() {
        System.out.println("begin testRetrieveGroupInfoWithNonExistingUser:");
        
        IGroupInfo groupInfo = null;
        boolean caughtUserNotFoundException = false;
        try {
            groupInfo = um.getGroupInfo(null, "test");
        }
        catch (UserNotFoundException e) {
            caughtUserNotFoundException = true;
        }
        catch (PermissionException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertTrue(caughtUserNotFoundException);
        assertNull(groupInfo);
        
        System.out.println("end testRetrieveGroupInfoWithNonExistingUser.");        
    }
}

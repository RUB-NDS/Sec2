package org.sec2.backend.test;

import java.util.Arrays;

import junit.framework.TestCase;

import org.sec2.backend.IGroupInfo;
import org.sec2.backend.exceptions.ChallengeVerficationFailedException;
import org.sec2.backend.impl.AbstractUserManagementFactory;
import org.sec2.backend.impl.UserManagement;
import org.sec2.statictestdata.TestKeyProvider;

public class EmailConfirmationTest extends TestCase {

    private byte[] signature;
    private TestKeyProvider keyProvider;
    private UserManagement um;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.restoreDatabase();
        um = AbstractUserManagementFactory.createDefault();
        keyProvider = TestKeyProvider.getInstance();
        // Register user
        TestUtil.createUser();
        
        // calculate challenge
        assertTrue(um != null);
        signature = TestUtil.createChallengeSignature(keyProvider.getUserSignCert(), keyProvider.getUserSignKey().getPrivate());

    }

    public void testConfirmEmailWithValidRegistration() {
        System.out.println("begin testConfirmEmailWithValidRegistration:");

        IGroupInfo groupInfo = null;
        try {
            groupInfo = um.confirmEmail(signature, keyProvider.getUserSignCert());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertNotNull("GroupInfo should not be null", groupInfo);
        assertEquals("Size of initial group should be 1", 1, groupInfo.getMembers().size());
        assertTrue("The user ID does not match with registered user", Arrays.equals(keyProvider.getUserID(), groupInfo.getMembers().get(0).getId()));
        assertTrue("The group operator does not match with registered user", Arrays.equals(keyProvider.getUserID(), groupInfo.getOperator().getId()));
        
        System.out.println("end testConfirmEmailWithValidRegistration.");
    }
    
//    public void testConfirmEmailWithInvalidSignature() {
//        System.out.println("begin testConfirmEmailWithInvalidSignature:");
//
//        IGroupInfo groupInfo = null;
//        Exception ex = null;
//        try {
//            groupInfo = um.confirmEmail(new byte[20], keyProvider.getUserSignCert());
//        }
//        catch (ChallengeVerficationFailedException e) {
//            ex = e;
//        } catch(Exception e) {
//            fail(e.getMessage());
//        }
//        assertNotNull("ChallengeVerficationFailedException was not thrown", ex);
//        assertNull("groupInfo should not be set", groupInfo);
//        System.out.println("end testConfirmEmailWithInvalidSignature.");
//    }

}

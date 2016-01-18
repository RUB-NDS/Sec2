package org.sec2.backend.test;

import java.security.cert.X509Certificate;

import junit.framework.TestCase;
import junit.framework.TestResult;

import org.sec2.backend.exceptions.InvalidUserPKCException;
import org.sec2.backend.exceptions.UserAlreadyExistsException;
import org.sec2.backend.impl.AbstractUserManagementFactory;
import org.sec2.backend.impl.UserManagement;
import org.sec2.statictestdata.TestKeyProvider;

public class RegisterTest extends TestCase {

    UserManagement um;
    X509Certificate userSignCertificate;
    X509Certificate userEncCertificate;
    private TestKeyProvider keyProvider;

    
   
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.restoreDatabase();
        keyProvider = TestKeyProvider.getInstance();
        userEncCertificate = keyProvider.getUserEncCert();
        userSignCertificate = keyProvider.getUserSignCert();
        um = AbstractUserManagementFactory.createDefault();
    }

    /**
	 * 
	 */
    public void testSingleRegistration() {
        System.out.println("begin testSingleRegistration:");
        boolean registered = false;
        try {
            registered = um.register(userEncCertificate, userSignCertificate);
        }
        catch (InvalidUserPKCException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (UserAlreadyExistsException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertTrue("Valid registrations return true", registered);
        System.out.println("end testSingleRegistration.");
    }

    /**
	 * 
	 */
    public void testInvalidParameters() {
        System.out.println("begin testInvalidParameters:");
        boolean registered = false;
        InvalidUserPKCException exception = null;
        try {
            registered = um.register(null, null);
        }
        catch (InvalidUserPKCException e) {
            exception = e;
        } catch (UserAlreadyExistsException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertFalse(registered);
        assertNotNull(
                "Registering with an invalid certificate results in an InvalidUserPKCException",
                exception);
        System.out.println("end testInvalidParameters.");
    }
}

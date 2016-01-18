package org.sec2.android;

import java.io.IOException;
import java.util.List;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.managers.IGroupManager;
import org.sec2.managers.IUserManager;
import org.sec2.managers.beans.Group;
import org.sec2.managers.beans.User;
import org.sec2.saml.client.SAMLClientBootstrap;
import org.sec2.saml.client.managers.GroupManagerImpl;
import org.sec2.saml.client.managers.UserManagerImpl;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * The usermanager to test.
     */
    private IUserManager userManager;
    
    /**
     * The groupmanager to test.
     */
    private IGroupManager groupManager;

    /**
     * Bootstraps.
     *
     * @throws Exception whatever goes wrong during super.setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SAMLClientBootstrap.bootstrap();
        userManager = UserManagerImpl.getInstance();
        groupManager = GroupManagerImpl.getInstance();
    }

    /**
     * Tests getting all known users.
     */
    public void testGetRegisteredUser() {
        User u = userManager.getRegisteredUser();
        assertNotNull(u);
    }
    
    /**
     * Tests getting all known users.
     */
    public void testGetKnownUsers() {
        List<byte[]> list = null;
        try {
            list = userManager.getKnownUsers();
        } catch (ExMiddlewareException e) {
            fail(e.toString());
        } catch (IOException e) {
            fail(e.toString());
        }
        assertNotNull(list);
    }
    
    /**
     * Tests getting a group.
     */
    public void testGetGroup() {
        Group g = null;
        try {
            g = groupManager.getGroup("user@sec2.org");
        } catch (ExMiddlewareException e) {
            fail(e.toString());
        } catch (IOException e) {
            fail(e.toString());
        }
        assertNotNull(g);
    }
}

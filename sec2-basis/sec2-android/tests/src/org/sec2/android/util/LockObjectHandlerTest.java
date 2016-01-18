package org.sec2.android.util;

import android.test.AndroidTestCase;

/**
 * This JUnit-Class tests the methods of class LockObjectHandler.
 * 
 * @author nike
 */
public final class LockObjectHandlerTest extends AndroidTestCase
{
    /**
     * This method tests the setLockObject()-method.
     */
    public void testSetLockObject()
    {
        try
        {
            LockObjectHandler.setLockObject("1", null);
        }
        catch(NullPointerException npe)
        {
            assertTrue(true);
        }
        catch(Exception e)
        {
            fail();
        }
        assertTrue(LockObjectHandler.setLockObject("2", new Object()));
        assertFalse(LockObjectHandler.setLockObject("2", new Object()));
    }

    /**
     * This method tests the getLockObject()-method.
     */
    public void testGetLockObject()
    {
        final Object lockObject = new Object();

        LockObjectHandler.setLockObject("3", lockObject);
        assertTrue(lockObject == LockObjectHandler.getLockObject("3"));
    }

    /**
     * This method tests the removeLockObject()-method.
     */
    public void testRemoveLockObject()
    {
        final Object lockObject = new Object();

        LockObjectHandler.setLockObject("4", lockObject);
        assertTrue(lockObject == LockObjectHandler.removeLockObject("4"));
        assertTrue(null == LockObjectHandler.getLockObject("4"));
    }
}

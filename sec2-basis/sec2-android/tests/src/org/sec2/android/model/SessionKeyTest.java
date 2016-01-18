package org.sec2.android.model;

import android.test.AndroidTestCase;

/**
 * This JUnit-Class tests the methods of class SessionKey.
 * 
 * @author nike
 */
public final class SessionKeyTest extends AndroidTestCase
{
    /**
     * This method tests the methods getSessionToken() and setSessionToken().
     */
    public void testSetAndGetSessionToken()
    {
        SessionKey key = new SessionKey();

        key.setSessionToken("test");
        assertEquals("test", key.getSessionToken());
    }
}

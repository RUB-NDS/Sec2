package org.sec2.android;

import android.test.AndroidTestCase;

/**
 * This JUnit-Class tests the methods of class Sec2Middleware.
 * 
 * @author nike
 */
public final class Sec2MiddlewareTest extends AndroidTestCase
{
    /**
     * This method tests the getSec2Middleware()-method.
     */
    public void testGetSec2Middleware()
    {
        assertNotNull(Sec2Middleware.getSec2Middleware());
    }

    /**
     * This method tests the startMiddlewareServer()- and
     * stopMiddlewareServer()-method, if a new thread was
     * started and stopped without exception.
     */
    public void testStartAndStopMiddlewareServer()
    {
        Sec2Middleware middleware = Sec2Middleware.getSec2Middleware();

        try
        {
            middleware.startMiddlewareServer(null, "localhost", 17183);
            middleware.stopMiddlewareServer();
            assertTrue(true);
        }
        catch(Exception e)
        {
            fail(e.toString() + ": " + e.getMessage());
        }
    }
}

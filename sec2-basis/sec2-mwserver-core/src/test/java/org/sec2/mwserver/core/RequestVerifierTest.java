package org.sec2.mwserver.core;

import java.io.BufferedReader;
import java.io.StringReader;
import java.text.MessageFormat;

import junit.framework.TestCase;

import org.sec2.persistence.PersistenceManagerContainer;

/**
 * This JUnit-Class tests the methods of class RequestVerifier.
 * 
 * @author nike
 */
public final class RequestVerifierTest extends TestCase
{
    IncomingRequest request = null;

    @Override
    public void setUp()
    {
        final String formattedRequest = MessageFormat.format(
                TestRequests.GET_GROUP, "1353588820109-1353588820109",
                "forRtZKXelcQxJclBA3LFzUtxzsDx3Zd6cRKL8HWlOij7KS02tNL+50"
                        + "Ef8AAlfzn/R/xGY88tooyxSMKyQfwHg==");
        try
        {
            request = IncomingRequest.analyseIncomingRequest(
                    new BufferedReader(new StringReader(formattedRequest)));
        }
        catch(Exception e)
        {
            //Do nothing here
        }
    }

    /**
     * Tests, if verifying a valid REST-request succeeds without any
     * errors.
     */
    public void testVerifyRequest()
    {
        boolean result = true;

        try
        {
            RequestVerifier.verifyRequest(request, false);
        }
        catch(Exception e)
        {
            result = false;
        }

        assertTrue(result);
    }

    /**
     * Tests, if verifying a valid REST-request with a valid signature succeeds
     * without any errors.
     */
    public void testVerifySignature()
    {
        boolean result = true;

        PersistenceManagerContainer.setPersistenceManager(new TestDbManager());
        try
        {
            RequestVerifier.verifySignature(request);
        }
        catch(Exception e)
        {
            result = false;
        }

        assertTrue(result);
    }
}

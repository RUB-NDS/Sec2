package org.sec2.mwserver.core;

import java.io.BufferedReader;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import junit.framework.TestCase;

/**
 * This JUnit-Class tests the methods of class IncomingRequest.
 * 
 * @author nike
 */
public final class IncomingRequestTest extends TestCase
{
    private IncomingRequest getRequest = null;
    private IncomingRequest putRequest = null;
    private IncomingRequest deleteRequest = null;
    private String formatedPutHeaders = null;
    private boolean getRequestSuccess = true;
    private boolean putRequestSuccess = true;
    private boolean deleteRequestSuccess = true;
    private int contentLength = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp()
    {
        final String request = MessageFormat.format(TestRequests.REGISTER,
                "1342517723327-1342517723327");
        try
        {
            getRequest = IncomingRequest.analyseIncomingRequest(
                    new BufferedReader(new StringReader(request)));
        }
        catch(Exception e)
        {
            getRequestSuccess = false;
        }
        try
        {
            contentLength = TestRequests.CONTENT.getBytes("UTF-8").length;
            formatedPutHeaders = MessageFormat.format(TestRequests.PUT_REQUEST,
                    contentLength);
            putRequest = IncomingRequest.analyseIncomingRequest(
                    new BufferedReader(new StringReader(formatedPutHeaders
                            + "\n\n" + TestRequests.CONTENT)));
        }
        catch(Exception e)
        {
            putRequestSuccess = false;
        }
        try
        {
            deleteRequest = IncomingRequest.analyseIncomingRequest(
                    new BufferedReader(new StringReader(
                            TestRequests.DELETE_REQUEST)));
        }
        catch(Exception e)
        {
            deleteRequestSuccess = false;
        }
    }

    //==== Test GET-Request

    /**
     * Tests, if analysing a GET-request succeeded without any errors.
     */
    public void testAnalyseGetRequest()
    {
        assertTrue(getRequestSuccess);
    }

    /**
     * Tests, if method getContent() returns NULL after analysing a
     * GET-request.
     */
    public void testGetContentGetRequest()
    {
        assertNull(getRequest.getContent());
    }

    /**
     * Tests, if all headers of request REGISTER are available after analysing
     * the request.
     */
    public void testGetHeadersGetRequest()
    {
        LinkedHashMap<String, String> headers = null;

        assertTrue(getRequest.getHeaders().size() >= 7);
        headers = new LinkedHashMap<String, String>();
        headers.put("Location", "REST");
        headers.put("x-sec2-appName", "test");
        headers.put("x-sec2-nonce", "123456789");
        headers.put("x-sec2-timestamp", "1342517723327-1342517723327");
        headers.put("x-sec2-socketport", "50001");
        headers.put("Host", "localhost");
        headers.put("Connection", "Keep-Alive");

        for(Entry<String, String> entry : headers.entrySet())
        {
            assertTrue(getRequest.getHeaders().containsKey(entry.getKey()));
            assertEquals(getRequest.getHeaders().get(entry.getKey()),
                    entry.getValue());
        }
    }

    /**
     * Tests if method getMethod() returns HttpMethod.GET after analysing a
     * GET-request.
     */
    public void testGetMethodGetRequest()
    {
        assertEquals(HttpMethod.GET, getRequest.getMethod());
    }

    /**
     * Tests if method getOriginalRequest() returns the original GET-request
     * TestRequests.REGISTER after analysing the request.
     */
    public void testGetOriginalRequestGetRequest()
    {
        final String originalRequest = MessageFormat.format(
                TestRequests.REGISTER, "1342517723327-1342517723327");

        assertEquals(originalRequest, getRequest.getOriginalRequest());
    }

    /**
     * Tests if method getRequestedResource() returns the requested resource of
     * TestRequests.REGISTER after analysing the request.
     */
    public void testGetRequestedResourceGetRequest()
    {
        assertEquals("/register", getRequest.getRequestedRessource());
    }

    //==== Test PUT-Request

    /**
     * Tests, if analysing a PUT-request succeeded without any errors.
     */
    public void testAnalysePutRequest()
    {
        assertTrue(putRequestSuccess);
    }

    /**
     * Tests, if method getContent() returns TestRequests.CONTENT after
     * analysing the PUT-request.
     */
    public void testGetContentPutRequest()
    {
        assertEquals(TestRequests.CONTENT.trim(),
                putRequest.getContent().trim());
    }

    /**
     * Tests, if all headers of request TestRequests.PUT_REQUEST are available
     * after analysing the request.
     */
    public void testGetHeadersPutRequest()
    {
        LinkedHashMap<String, String> headers = null;

        assertTrue(putRequest.getHeaders().size() >= 4);
        headers = new LinkedHashMap<String, String>();
        headers.put("Content-Length", "" + contentLength);
        headers.put("Content-Type", "text/html; charset = UTF-8");
        headers.put("Host", "localhost");
        headers.put("Connection", "Keep-Alive");

        for(Entry<String, String> entry : headers.entrySet())
        {
            assertTrue(putRequest.getHeaders().containsKey(entry.getKey()));
            assertEquals(putRequest.getHeaders().get(entry.getKey()),
                    entry.getValue());
        }
    }

    /**
     * Tests if method getMethod() returns HttpMethod.PUT after analysing a
     * PUT-request.
     */
    public void testGetMethodPutRequest()
    {
        assertEquals(HttpMethod.PUT, putRequest.getMethod());
    }

    /**
     * Tests if method getOriginalRequest() returns the original PUT-request
     * TestRequests.PUT_REQUEST + TestRequests.CONTENT after analysing the
     * request.
     */
    public void testGetOriginalRequestPutRequest()
    {
        assertEquals(formatedPutHeaders + "\n" + TestRequests.CONTENT,
                putRequest.getOriginalRequest());
    }

    /**
     * Tests if method getRequestedResource() returns the requested resource of
     * TestRequests.PUT_REQUEST after analysing the request.
     */
    public void testGetRequestedResourcePutRequest()
    {
        assertEquals("test.html", putRequest.getRequestedRessource());
    }

    //==== Test DELETE-request

    /**
     * Tests, if analysing a DELETE-request succeeded without any errors.
     */
    public void testAnalyseDeleteRequest()
    {
        assertTrue(deleteRequestSuccess);
    }

    /**
     * Tests, if method getContent() returns NULL after analysing a
     * DELETE-request.
     */
    public void testGetContentDeleteRequest()
    {
        assertNull(deleteRequest.getContent());
    }

    /**
     * Tests, if all headers of request TestRequests.DELETE_REQUEST are
     * available after analysing the request.
     */
    public void testGetHeadersDeleteRequest()
    {
        LinkedHashMap<String, String> headers = null;

        assertTrue(deleteRequest.getHeaders().size() >= 2);
        headers = new LinkedHashMap<String, String>();
        headers.put("Host", "localhost");
        headers.put("Connection", "Keep-Alive");

        for(Entry<String, String> entry : headers.entrySet())
        {
            assertTrue(deleteRequest.getHeaders().containsKey(entry.getKey()));
            assertEquals(deleteRequest.getHeaders().get(entry.getKey()),
                    entry.getValue());
        }
    }

    /**
     * Tests if method getMethod() returns HttpMethod.DELETE after analysing a
     * DELETE-request.
     */
    public void testGetMethodDeleteRequest()
    {
        assertEquals(HttpMethod.DELETE, deleteRequest.getMethod());
    }

    /**
     * Tests if method getOriginalRequest() returns the original DELETE-request
     * TestRequests.DELETE_REQUEST after analysing the request.
     */
    public void testGetOriginalRequestDeleteRequest()
    {
        assertEquals(TestRequests.DELETE_REQUEST,
                deleteRequest.getOriginalRequest());
    }

    /**
     * Tests if method getRequestedResource() returns the requested resource of
     * TestRequests.DELETE_REQUEST after analysing the request.
     */
    public void testGetRequestedResourceDeleteRequest()
    {
        assertEquals("test.html", deleteRequest.getRequestedRessource());
    }
}

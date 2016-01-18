package org.sec2.mwserver.core;

import java.io.BufferedReader;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import junit.framework.TestCase;

/**
 * This JUnit-Class tests the methods of class IncomingResponse.
 * 
 * @author nike
 */
public final class IncomingResponseTest extends TestCase
{
    private IncomingResponse responseWithContent = null;
    private IncomingResponse response = null;
    private String headersWithContent = null;
    private String headers = null;
    private boolean responseWithContentSuccess = true;
    private boolean responseSuccess = true;
    private int contentLength = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp()
    {
        try
        {
            contentLength = TestResponses.RESPONSE_CONTENT_XML
                    .getBytes("UTF-8").length;
            headersWithContent = MessageFormat.format(
                    TestResponses.RESPONSE_HEADER, contentLength);
            responseWithContent = IncomingResponse.analyseIncomingResponse(
                    new BufferedReader(new StringReader(
                            headersWithContent + "\n"
                                    + TestResponses.RESPONSE_CONTENT_XML)));
        }
        catch(Exception e)
        {
            responseWithContentSuccess = false;
        }
        try
        {
            headers = MessageFormat.format(TestResponses.RESPONSE_HEADER, 0);
            response = IncomingResponse.analyseIncomingResponse(
                    new BufferedReader(new StringReader(headers)));
        }
        catch(Exception e)
        {
            responseSuccess = false;
        }
    }

    //==== Test HTTP-response with content

    /**
     * Tests, if analysing a HTTP-response with content succeeded without any
     * errors.
     */
    public void testAnalyseResponseWithContent()
    {
        assertTrue(responseWithContentSuccess);
    }

    /**
     * Tests, if method getContent() returns TestResponses.RESPONSE_CONTENT_XML
     * after analysing the HTTP-response with content.
     */
    public void testGetContentResponseWithContent()
    {
        assertEquals(TestResponses.RESPONSE_CONTENT_XML.trim(),
                responseWithContent.getContent().trim());
    }

    /**
     * Tests, if method getFullStatusLine() returns HTTP/1.1 200 OK after
     * analysing the HTTP-response with content.
     */
    public void testGetFullStatusLineResponseWithContent()
    {
        assertEquals("HTTP/1.1 200 OK",
                responseWithContent.getFullStatusLine().trim());
    }

    /**
     * Tests, if all headers of response TestResponses.RESPONSE_HEADER are
     * available after analysing the response with content.
     */
    public void testGetHeadersResponseWithContent()
    {
        LinkedHashMap<String, String> headers = null;

        assertTrue(responseWithContent.getHeaders().size() >= 3);
        headers = new LinkedHashMap<String, String>();
        headers.put("Content-Length", "" + contentLength);
        headers.put("Content-Type", "application/xml; charset=UTF-8");
        headers.put("Connection", "close");

        for(Entry<String, String> entry : headers.entrySet())
        {
            assertTrue(responseWithContent.getHeaders().containsKey(
                    entry.getKey()));
            assertEquals(responseWithContent.getHeaders().get(entry.getKey()),
                    entry.getValue());
        }
    }

    /**
     * Tests if method getOriginalResponse() returns the original HTTP-response
     * TestResponses.RESPONSE_HEADER and TestResponses.RESPONSE_CONTENT_XML
     * after analysing the response with content.
     */
    public void testGetOriginalResponseResponseWithContent()
    {
        assertEquals((headersWithContent
                + TestResponses.RESPONSE_CONTENT_XML).trim(),
                responseWithContent.getOriginalResponse().trim());
    }

    /**
     * Tests if method getStatusCode() returns 200 after analysing a
     * HTTP-response with headers TestResponses.RESPONSE_HEADER and content.
     */
    public void testGetStatusCodeResponseWithContent()
    {
        assertEquals(200, responseWithContent.getStatusCode());
    }

    /**
     * Tests if method getStatusInfo() returns OK after analysing a
     * HTTP-response with headers TestResponses.RESPONSE_HEADER and content.
     */
    public void testGetStatusInfoResponseWithContent()
    {
        assertEquals("OK", responseWithContent.getStatusInfo());
    }

    //==== Test HTTP-response without content

    /**
     * Tests, if analysing a HTTP-response without content succeeded without
     * any errors.
     */
    public void testAnalyseHttpResponse()
    {
        assertTrue(responseSuccess);
    }

    /**
     * Tests, if method getContent() returns NULL after analysing the
     * HTTP-response without content.
     */
    public void testGetContentResponse()
    {
        assertNull(response.getContent());
    }

    /**
     * Tests, if method getFullStatusLine() returns HTTP/1.1 200 OK after
     * analysing the HTTP-response without content.
     */
    public void testGetFullStatusLineResponse()
    {
        assertEquals("HTTP/1.1 200 OK", response.getFullStatusLine().trim());
    }

    /**
     * Tests, if all headers of response TestResponses.RESPONSE_HEADER are
     * available after analysing the response without content.
     */
    public void testGetHeadersResponse()
    {
        LinkedHashMap<String, String> headers = null;

        assertTrue(response.getHeaders().size() >= 3);
        headers = new LinkedHashMap<String, String>();
        headers.put("Content-Length", "0");
        headers.put("Content-Type", "application/xml; charset=UTF-8");
        headers.put("Connection", "close");

        for(Entry<String, String> entry : headers.entrySet())
        {
            assertTrue(response.getHeaders().containsKey(entry.getKey()));
            assertEquals(response.getHeaders().get(entry.getKey()),
                    entry.getValue());
        }
    }

    /**
     * Tests if method getOriginalResponse() returns the original HTTP-response
     * TestResponses.RESPONSE_HEADER and TestResponses.RESPONSE_CONTENT_XML
     * after analysing the response without content.
     */
    public void testGetOriginalResponseResponse()
    {
        assertEquals(headers.trim(), response.getOriginalResponse().trim());
    }

    /**
     * Tests if method getStatusCode() returns 200 after analysing a
     * HTTP-response with headers TestResponses.RESPONSE_HEADER and without
     * content.
     */
    public void testGetStatusCodeResponse()
    {
        assertEquals(200, response.getStatusCode());
    }

    /**
     * Tests if method getStatusInfo() returns OK after analysing a
     * HTTP-response with headers TestResponses.RESPONSE_HEADER and without
     * content.
     */
    public void testGetStatusInfoResponse()
    {
        assertEquals("OK", response.getStatusInfo());
    }
}

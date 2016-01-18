package org.sec2.mwserver.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This class extends the Socket-class for testing-purposes
 * @author nike
 *
 */
public final class TestClientSocket extends Socket
{
    private final String inputStreamText;
    private final ByteArrayOutputStream os = new ByteArrayOutputStream();

    /**
     * The constructor for this class. Expects a text for the inputStream.
     * 
     * @param inputStreamText - The text for the inputstream
     * 
     * @throws UnknownHostException
     * @throws IOException
     */
    public TestClientSocket(String inputStreamText)
            throws UnknownHostException, IOException
            {
        super("localhost", 1);
        this.inputStreamText = inputStreamText;
            }

    /* (non-Javadoc)
     * @see java.net.SocketImpl#getInputStream()
     */
    @Override
    public InputStream getInputStream() throws IOException
    {
        return new ByteArrayInputStream(inputStreamText.getBytes("Utf-8"));
    }

    /* (non-Javadoc)
     * @see java.net.SocketImpl#getOutputStream()
     */
    @Override
    public OutputStream getOutputStream() throws IOException
    {
        return os;
    }

    public String getOutputStreamContent()
    {
        try
        {
            return os.toString("UTF-8");
        }
        catch(UnsupportedEncodingException uee)
        {
            return os.toString();
        }
    }
}

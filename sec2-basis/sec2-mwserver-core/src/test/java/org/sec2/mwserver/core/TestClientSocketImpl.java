package org.sec2.mwserver.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.text.MessageFormat;

/**
 * This is a Socket-implementation for testing-purposes
 * @author nike
 *
 */
public final class TestClientSocketImpl extends SocketImpl
{
    
    public static String RESPONSE_CONTEXT = TestResponses.RESPONSE_CONTENT_XML;
    
    /* (non-Javadoc)
     * @see java.net.SocketOptions#getOption(int)
     */
    @Override
    public Object getOption(int arg0) throws SocketException
    {
        //Nothing to do here
        return null;
    }

    /* (non-Javadoc)
     * @see java.net.SocketOptions#setOption(int, java.lang.Object)
     */
    @Override
    public void setOption(int arg0, Object arg1) throws SocketException
    {
        //Nothing to do here
    }

    /* (non-Javadoc)
     * @see java.net.SocketImpl#accept(java.net.SocketImpl)
     */
    @Override
    protected void accept(SocketImpl arg0) throws IOException
    {
        //Nothing to do here
    }

    /* (non-Javadoc)
     * @see java.net.SocketImpl#available()
     */
    @Override
    protected int available() throws IOException
    {
        //Nothing to do here
        return 0;
    }

    /* (non-Javadoc)
     * @see java.net.SocketImpl#bind(java.net.InetAddress, int)
     */
    @Override
    protected void bind(InetAddress arg0, int arg1) throws IOException
    {
        //Nothing to do here
    }

    /* (non-Javadoc)
     * @see java.net.SocketImpl#close()
     */
    @Override
    protected void close() throws IOException
    {
        //Nothing to do here
    }

    /* (non-Javadoc)
     * @see java.net.SocketImpl#connect(java.lang.String, int)
     */
    @Override
    protected void connect(String arg0, int arg1) throws IOException
    {
        //Nothing to do here
    }

    /* (non-Javadoc)
     * @see java.net.SocketImpl#connect(java.net.InetAddress, int)
     */
    @Override
    protected void connect(InetAddress arg0, int arg1) throws IOException
    {
        //Nothing to do here
    }

    /* (non-Javadoc)
     * @see java.net.SocketImpl#connect(java.net.SocketAddress, int)
     */
    @Override
    protected void connect(SocketAddress arg0, int arg1) throws IOException
    {
        //Nothing to do here
    }

    /* (non-Javadoc)
     * @see java.net.SocketImpl#create(boolean)
     */
    @Override
    protected void create(boolean arg0) throws IOException
    {
        //Nothing to do here
    }

    /* (non-Javadoc)
     * @see java.net.SocketImpl#getInputStream()
     */
    @Override
    public InputStream getInputStream() throws IOException
    {
        StringBuilder sb = new StringBuilder();

        sb.append(MessageFormat.format(TestResponses.RESPONSE_HEADER,
                RESPONSE_CONTEXT.getBytes("UTF-8").length));
        sb.append("\n");
        sb.append(RESPONSE_CONTEXT);
        return new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
    }

    /* (non-Javadoc)
     * @see java.net.SocketImpl#getOutputStream()
     */
    @Override
    public OutputStream getOutputStream() throws IOException
    {
        return new ByteArrayOutputStream();
    }

    /* (non-Javadoc)
     * @see java.net.SocketImpl#listen(int)
     */
    @Override
    protected void listen(int arg0) throws IOException
    {
        //Nothing to do here
    }

    /* (non-Javadoc)
     * @see java.net.SocketImpl#sendUrgentData(int)
     */
    @Override
    protected void sendUrgentData(int arg0) throws IOException
    {
        //Nothing to do here
    }
}

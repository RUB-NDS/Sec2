package org.sec2.mwserver.core;

import java.net.SocketImpl;
import java.net.SocketImplFactory;

/**
 * This class implements a SocketFactory that returns a Socket-implementation
 * for testing-purposes
 * 
 * @author nike
 */
public final class TestSocketFactory implements SocketImplFactory
{
    /* (non-Javadoc)
     * @see java.net.SocketImplFactory#createSocketImpl()
     */
    @Override
    public SocketImpl createSocketImpl()
    {
        return new TestClientSocketImpl();
    }
}

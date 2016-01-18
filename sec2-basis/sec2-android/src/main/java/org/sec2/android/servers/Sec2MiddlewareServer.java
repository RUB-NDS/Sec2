/*
 * Copyright 2011 Sec2 Consortium
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit written
 * permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://www.sec2.org
 */
package org.sec2.android.servers;

import android.content.Context;
import de.adesso.mobile.android.sec2.mwadapter.logging.LogHelper;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.sec2.android.servers.rest.RestFunctionExecutor;
import org.sec2.android.util.CryptoUtils;
import org.sec2.exceptions.ExAbstractMiddlewareException;
import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.logging.LogLevel;
import org.sec2.mwserver.core.ConnectionHandler;
import org.sec2.mwserver.core.exceptions.ExServerConnectionException;
import org.sec2.mwserver.core.exceptions.ExServerException;

/**
 * This class implements the middleware-server.
 *
 * @author  Juraj Somorovsky - juraj.somorovsky@rub.de
 * @author  Nike Schüßler - nike.schuessler@rub.de
 * @version 1.0
 *
 * May 12, 2011
 */
public class Sec2MiddlewareServer implements Runnable
{
    private static final Class<?> CLAZZ = Sec2MiddlewareServer.class;
    /**
     * Server socket for incomming communication.
     */
    private ServerSocket serverSocket;
    /**
     * List of threads that process the incomming communication and XML server.
     */
    private List<ConnectionHandler> connectionHandlers;
    /**
     * This variable indicates that the server is running.
     */
    private boolean running;
    /* The context in which the middleware-server is running*/
    private final Context context;
    /** timeout for the socket connection. */
    private static final int SOCKET_TIMEOUT = 5000;

    /**
     * A class representing the Sec2 middleware server. By default, the Sec2
     * Middleware should include two instances of these servers: one for the
     * incoming and one for the outgoing communication.
     *
     * @param context The context
     * @param address Server-address
     * @param port Server port
     * @throws ExServerException Thrown when an IOException has occurred
     */
    Sec2MiddlewareServer(final Context context, final String address,
            final int port) throws ExServerException
            {
        try
        {
            serverSocket = new ServerSocket();
            if (address.equals("*"))
            {
                serverSocket.bind(new InetSocketAddress(port));
            }
            else
            {
                serverSocket.bind(new InetSocketAddress(address, port));
            }
            serverSocket.setSoTimeout(SOCKET_TIMEOUT);
            connectionHandlers = new LinkedList<ConnectionHandler>();
            this.context = context;
        }
        catch (final IOException ioe) {
            throw new ExServerException(ioe.getLocalizedMessage(),
                    ioe, LogLevel.PROBLEM);
        }
            }

    @Override
    public void run()
    {
        LogHelper.logD(CLAZZ, "Middleware Main Thread is Runnig");
        running = true;
        while (running) {
            try {
                try {
                    final Socket incomingSocket = serverSocket.accept();
                    LogHelper.logD(CLAZZ, "Connection accepted");

                    // handler thread start
                    final ConnectionHandler ch = new ConnectionHandler(
                            incomingSocket, new RestFunctionExecutor(context),
                            new CryptoUtils());
                    final Thread t = new Thread(ch);
                    t.start();
                    connectionHandlers.add(ch);
                }
                catch (final InterruptedIOException ie) {
                    // this ensures that every <SOCKET_TIMEOUT> ms the
                    // server tries to connect to a new incoming socket
                }

                // check the running XML processors and the possible exceptions
                // TO-DO iterate over SocketHandlers
                for (final Iterator<ConnectionHandler> cIter =
                        connectionHandlers.iterator(); cIter.hasNext();) {
                    final ConnectionHandler ch = cIter.next();
                    if (!ch.isRunning()) {
                        cIter.remove();

                        final ExServerConnectionException expe =
                                ch.getExServerConnectionException();
                        if (expe != null) {
                            this.handleServerException(expe);
                        }
                    }
                }
            }
            catch (final IOException ioe) {
                final ExMiddlewareException eme = new ExMiddlewareException(
                        ioe.getLocalizedMessage(), ioe, LogLevel.DEBUG);
                handleServerException(eme);
            }
        }

    }

    /**
     * Stop the server and its thread.
     *
     * @throws ExServerException Thrown when an IOException has occurred
     */
    public final void stop() throws ExServerException {
        running = false;
        try {
            serverSocket.close();
        }
        catch (final IOException ioe) {
            throw new ExServerException(ioe.getLocalizedMessage(),
                    ioe, LogLevel.EMERGENCY);
        }
    }

    /**
     * TO-DO: handle the exception thrown by the ConnectionHandler or by
     * connection problems
     *
     * TO-DO: handle the exceptions coming from the XMLProcessors during the
     * message processing, e.g. by the Future object:
     * http://recursor.blogspot.com/2007/03/mini-executorservice-future-how-to-
     * for.html
     *
     * @param ame
     */
    private void handleServerException(final ExAbstractMiddlewareException ame)
    {
        if (ame.getLogLevel() != LogLevel.DEBUG) {
            LogHelper.logE(CLAZZ,
                    "An error during the xml processing happened");
            LogHelper.logE(CLAZZ,ame);
        }
    }
}

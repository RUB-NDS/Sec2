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
package org.sec2.desktop.servers;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.sec2.desktop.servers.rest.RestFunctionExecutor;
import org.sec2.desktop.util.CryptoUtils;
import org.sec2.exceptions.ExAbstractMiddlewareException;
import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.logging.LogLevel;
import org.sec2.mwserver.core.ConnectionHandler;
import org.sec2.mwserver.core.exceptions.ExServerConnectionException;
import org.sec2.mwserver.core.exceptions.ExServerException;

/**
 * <DESCRIPTION>
 * @author  Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.1
 *
 * May 12, 2011
 */
public class Sec2MiddlewareServer implements Runnable {

    /**
     * Server socket for incomming communication.
     */
    private ServerSocket serverSocket;
    /**
     * List of threads that process the incomming communication and XML server
     */
    private List<ConnectionHandler> connectionHandlers;
    /**
     * This variable indicates that the server is running
     */
    private boolean running;
    /** timeout for the socket connection */
    private static final int SOCKET_TIMEOUT = 5000;

    /**
     * A class representing the Sec2 middleware server. By default, the Sec2
     * Middleware should include two instances of these servers: one for the
     * incoming and one for the outgoing communication.
     *
     * @param serverAddress Server address
     * @param serverPort Server port
     * @throws IOException
     */
    Sec2MiddlewareServer(final String address, final int port)
            throws ExServerException {
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(port));
            serverSocket.setSoTimeout(SOCKET_TIMEOUT);
            connectionHandlers = new LinkedList<ConnectionHandler>();
        }
        catch (final IOException ioe) {
            throw new ExServerException(ioe.getLocalizedMessage(),
                    ioe, LogLevel.PROBLEM);
        }
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                try {
                    final Socket incomingSocket = serverSocket.accept();
                    System.out.println("Connection accepted");

                    // handler thread start
                    final ConnectionHandler ch = new ConnectionHandler(incomingSocket, new RestFunctionExecutor(), new CryptoUtils());
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
                for (final Iterator<ConnectionHandler> cIter = connectionHandlers.iterator();
                        cIter.hasNext();) {
                    final ConnectionHandler ch = cIter.next();
                    if (!ch.isRunning()) {
                        cIter.remove();

                        final ExServerConnectionException expe =
                                ch.getExServerConnectionException();
                        if(expe != null) {
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
     * @throws ExServerException
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
     * TO-DO: handle the exceptions comming from the XMLProcessors during the
     * message processing, e.g. by the Future object:
     * http://recursor.blogspot.com/2007/03/mini-executorservice-future-how-to-for.html
     *
     * @param ame
     */
    private void handleServerException(final ExAbstractMiddlewareException ame) {
        if (ame.getLogLevel() != LogLevel.DEBUG) {
            System.err.println("An error during the xml processing happened");
            ame.printStackTrace();
        }
    }
}

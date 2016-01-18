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
package org.sec2.android;

import android.content.Context;
import de.adesso.mobile.android.sec2.mwadapter.logging.LogHelper;
import org.sec2.android.servers.Sec2MiddlewareServer;
import org.sec2.android.servers.Sec2MiddlewareServerFactory;
import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.mwserver.core.exceptions.ExServerException;

/**
 * This class represents the Sec2-Middleware. It is implemented as a Singleton.
 * It includes two Servers, that run in their own threads. The incoming server
 * handles the data coming from the cloud and cares for their decryption. The
 * outgoing server handles the encryption of the files going out to the cloud.
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.1
 */
public class Sec2Middleware
{
    private static final Class<?> CLAZZ = Sec2Middleware.class;
    /**
     * server.
     */
    private Sec2MiddlewareServer server;
    /**
     * Singleton object.
     */
    private static Sec2Middleware sec2Middleware = null;

    /**
     * Private constructor.
     */
    private Sec2Middleware() {
    }

    /**
     *
     * @return the singleton object of the Sec2 Middleware
     */
    public static Sec2Middleware getSec2Middleware() {
        if (sec2Middleware == null) {
            sec2Middleware = new Sec2Middleware();
        }
        return sec2Middleware;
    }

    /**
     * Creates the Sec2 Middleware Server using the
     * Sec2MiddlewareServerFactory. Then it starts these server and runs it
     * on the given port. The Server is running in its own thread.
     *
     * @param context - The context of the calling application/service
     * @param address server address
     * @param port port on which the server is running
     * communication
     * @throws ExMiddlewareException
     */
    public void startMiddlewareServer(final Context context,
            final String address, final int port) throws ExMiddlewareException
            {
        try
        {
            LogHelper.logV(CLAZZ, "Starting Middleware Server: " + address
                    + ":" + port);
            final Sec2MiddlewareServerFactory factory =
                    Sec2MiddlewareServerFactory
                    .getSec2MiddlewareServerFactory();
            server = factory.createSec2MiddlewareServer(context, address,
                    port);
            new Thread(server).start();
        } catch (final ExMiddlewareException eme) {
            this.stopMiddlewareServer();
            throw eme;
        }
            }

    /**
     * Stops the Sec2 Middleware.
     */
    public void stopMiddlewareServer()
    {
        try
        {
            LogHelper.logV(CLAZZ, "Stopping Middleware Server...");
            server.stop();
        }
        catch (final ExServerException ex)
        {
            LogHelper.logE(CLAZZ, ex);
        }
    }
}

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
package org.sec2.desktop;

import java.security.Provider;
import java.security.Security;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.desktop.servers.Sec2MiddlewareServer;
import org.sec2.desktop.servers.Sec2MiddlewareServerFactory;
import org.sec2.logging.LogLevel;
//import org.sec2.securityprovider.mobileclient.MobileClientProvider;
import org.sec2.mwserver.core.exceptions.ExServerException;

/**
 * This clas represents the Sec2 Middleware. It is implemented as a Singleton.
 * It includes two Servers, that run in their own threads. The incoming server
 * handles the data coming from the cloud and cares for their decryption. The
 * outgoing server handles the encryption of the files going out to the cloud.
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.1
 */
public class Sec2Middleware {

    /**
     * server
     */
    private Sec2MiddlewareServer server;
    /**
     * Singleton object
     */
    private static Sec2Middleware sec2Middleware = null;

    /**
     * Private constructor
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

//    /**
//     * activates the smart card with a given pin. Afterwards, it overwrites the
//     * pin bytes
//     *
//     * @param pin
//     */
//    public void activateSmartCard(final byte[] pin) {
//        Provider provider = MobileClientProvider.getInstance(pin);
//        Random r = new Random();
//        r.nextBytes(pin);
//        Security.insertProviderAt(provider, 1);
//    }

    /**
     * Creates the Sec2 Middleware Server using the 
     * Sec2MiddlewareServerFactory. Then it starts these server and runs it
     * on the given port. The Server is running in its own thread.
     *
     * @param address server address
     * @param port port on which the server is running
     * communication
     * @throws ExMiddlewareException
     */
    public void startMiddlewareServer(final String address, final int port)
            throws ExMiddlewareException {
        try {
            Logger.getLogger(Sec2Middleware.class.getName()).log(Level.FINEST,
                    "Starting Middleware Server: " + address + ":" + port);
            Sec2MiddlewareServerFactory factory =
                    Sec2MiddlewareServerFactory.getSec2MiddlewareServerFactory();
            server = factory.createSec2MiddlewareServer(address, port);
            new Thread(server).start();
        } catch (ExMiddlewareException eme) {
            this.stopMiddlewareServer();
            throw eme;
        }
    }

    /**
     * Stops the Sec2 Middleware
     */
    public void stopMiddlewareServer() {
        try {
            Logger.getLogger(Sec2Middleware.class.getName()).log(Level.FINEST,
                    "Stopping Middleware Server ");

            server.stop();
        } catch (ExServerException ex) {
            Logger.getLogger(Sec2Middleware.class.getName()).log(Level.SEVERE,
                    null, ex);
        }
    }
}

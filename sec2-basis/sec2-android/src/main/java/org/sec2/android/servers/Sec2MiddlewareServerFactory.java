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
import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.mwserver.core.exceptions.ExServerException;

/**
 * Sec2 Middleware Server factory used for creating the servers.
 * @author  Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.1
 *
 * May 13, 2011
 */
public class Sec2MiddlewareServerFactory {

    /**
     * Private static singleton.
     */
    private static Sec2MiddlewareServerFactory factory;

    /**
     * Private Factory constructor.
     */
    private Sec2MiddlewareServerFactory() {
    }

    /**
     * Returns the Sec2MiddlewareServerFactory instance.
     *
     * @return An instance of this class
     */
    public static Sec2MiddlewareServerFactory getSec2MiddlewareServerFactory()
    {
        if (factory == null) {
            factory = new Sec2MiddlewareServerFactory();
        }
        return factory;
    }

    /**
     * Creates an instance of class "Sec2MiddlewareServer".
     *
     * @param context - The context of the calling application/service
     * @param address - The address of the server
     * @param port - The port on which the server listens
     *
     * @return An instance of class "Sec2MiddlewareServer"
     *
     * @throws ExMiddlewareException Wraps a thrown exception
     */
    public Sec2MiddlewareServer createSec2MiddlewareServer(
            final Context context, final String address, final int port)
                    throws ExMiddlewareException {
        try {
            return new Sec2MiddlewareServer(context, address, port);
        }
        catch (final ExServerException ese) {
            throw new ExMiddlewareException(ese.getLocalizedMessage(),
                    ese.getWrappedException(), ese.getLogLevel());
        }
    }
}

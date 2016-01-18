package org.sec2.mwserver.core.exceptions;

import java.io.IOException;

/**
 * This exception may be thrown, if an exception has occured while handling
 * a REST-request.
 *
 * @author nike
 *
 */
public class HandleRequestException extends IOException
{
    private static final long serialVersionUID = -7015510987645605409L;

    /**
     * Constructor of this exception. It passes the error-message to the
     * super-constructor.
     *
     * @param message The error-message.
     */
    public HandleRequestException(final String message)
    {
        super(message);
    }
}

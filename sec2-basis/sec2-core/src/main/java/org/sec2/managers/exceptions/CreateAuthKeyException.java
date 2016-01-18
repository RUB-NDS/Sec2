package org.sec2.managers.exceptions;

import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.logging.LogLevel;

public class CreateAuthKeyException extends ExMiddlewareException
{
    /**
     * Constructor for a new, wrapped/unwrapped exception.
     *
     * @param message           Reason for this exception or <code>null</code>
     *                          if the default message should be used.
     * @param exception         Wrapped exception which caused the problem, if
     *                          any or <code>null</code> if there is no
     *                          exception to wrap.
     * @param loglevel          Log level for the generated message or
     *                          <code>null</code> if this issue should not be
     *                          logged.
     */
    public CreateAuthKeyException(final String message,
            final Exception exception, final LogLevel loglevel)
    {
        super(message, exception, loglevel);

    }

    /**
     * The default constructor will use predefined values of this 
     * exception and doesn't wrap another exception.
     */
    public CreateAuthKeyException()
    {
        super(DEFAULT_MESSAGE, null, DEFAULT_LOGLEVEL);
    }
}

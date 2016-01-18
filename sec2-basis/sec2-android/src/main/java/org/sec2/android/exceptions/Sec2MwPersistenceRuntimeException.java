package org.sec2.android.exceptions;

/**
 * This exception can be thrown if a runtime exception has occured during
 * operations in the persistence-layer of the Sec2-middleware.
 *
 * @author nike
 */
public class Sec2MwPersistenceRuntimeException extends RuntimeException
{
    private static final long serialVersionUID = 5064053851308660885L;

    /**
     * Constructs an object of this class with the passed message.
     *
     * @param message - The message of this exception
     */
    public Sec2MwPersistenceRuntimeException(final String message)
    {
        super(message);
    }

    /**
     * Constructs an object of this class with the passed message and cause.
     *
     * @param message - The message of this exception
     * @param cause - The cause of this exception
     */
    public Sec2MwPersistenceRuntimeException(final String message,
            final Throwable cause)
    {
        super(message, cause);
    }
}

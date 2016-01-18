package org.sec2.android.exceptions;

/**
 * This exception may be thrown, if an exception in context of the execution of
 * the Sec2-middleware-service has occured.
 *
 * @author nike
 */
public class Sec2MwServiceRuntimeException extends RuntimeException
{
    private static final long serialVersionUID = -7409101632614021081L;

    /**
     * Default and only constructor of this exception.
     *
     * @param cause - The cause for this exception
     */
    public Sec2MwServiceRuntimeException(final Throwable cause)
    {
        super(cause);
    }
}

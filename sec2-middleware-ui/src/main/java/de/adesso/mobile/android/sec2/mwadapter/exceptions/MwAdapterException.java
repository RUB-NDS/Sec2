package de.adesso.mobile.android.sec2.mwadapter.exceptions;

/**
 * This is a wrapper class for exceptions to pass them to the application. This class was introduced to reduce the number of different
 * exceptions the application must catch. But it can be used for common MwAdapter exceptions, too.
 * 
 * @author schuessler
 *
 */
public class MwAdapterException extends Exception
{
    private static final long serialVersionUID = -3894973799471795093L;

    private Exception exception = null;

    /**
     * Constructor for wrapping-purposes. The constructor expects an exception
     * that shall be wrapped by this exception.
     *
     * @param exception - The exception to wrap
     */
    public MwAdapterException(final Exception exception)
    {
        this.exception = exception;
    }

    /**
     * Constructor for constucting an MwAdapterException with the passed
     * error message.
     * 
     * @param message - The error message
     */
    public MwAdapterException(final String message)
    {
        super(message);
    }

    /**
     * Returns the wrapped exception. If no exception was wrapped, NULL is
     * returned.
     * 
     * @return The wrapped exception
     */
    public Exception getException()
    {
        return (exception != null ? exception : this);
    }

    /**
     * Returns the error message of the wrapped exception if an exception was
     * wrapped. Otherwise it returns the error message of this exception.
     * 
     * @return Return either the message of the wrapped exception or the
     *  message of this exception
     */
    @Override
    public String getMessage()
    {
        return (exception != null ? exception.getMessage() : super.getMessage());
    }
}

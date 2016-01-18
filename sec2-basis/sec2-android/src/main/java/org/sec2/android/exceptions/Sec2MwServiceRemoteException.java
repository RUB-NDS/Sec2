package org.sec2.android.exceptions;

import android.os.RemoteException;

/**
 * This exception extends Android's RemoteException in order to add an
 * error-message to the exception. This is, because at API-level 10 it is not
 * possible, to add a message to the exception.
 *
 * @author nike
 */
public class Sec2MwServiceRemoteException extends RemoteException
{
    private static final long serialVersionUID = 4638402893849879597L;
    private String message = "";

    /**
     * The standard-constructor, identical to that of the superclass.
     */
    public Sec2MwServiceRemoteException()
    {
        super();
    }

    /**
     * Constructor expecting a message as error-message.
     *
     * @param message - The error-message
     */
    public Sec2MwServiceRemoteException(final String message)
    {
        this.message = message;
    }

    @Override
    public String getLocalizedMessage()
    {
        return message;
    }

    @Override
    public String getMessage()
    {
        return message;
    }
}

package de.adesso.mobile.android.sec2.mwadapter.exceptions;

/**
 * This exception is thrown, if an exception has occured while parsing an
 * XML-file.
 *
 * @author nike
 */
public class XMLParseException extends Exception
{
    private static final long serialVersionUID = -8386117435146570149L;

    /**
     * The constructor for this exception, expecting an error message.
     *
     * @param message - The error message
     */
    public XMLParseException(String message)
    {
        super(message);
    }
}

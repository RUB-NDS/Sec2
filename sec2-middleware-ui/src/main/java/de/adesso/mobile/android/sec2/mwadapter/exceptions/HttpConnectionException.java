package de.adesso.mobile.android.sec2.mwadapter.exceptions;

/**
 * Exception for HTTP connection problems between the app and the middleware.
 *
 * @author nike
 */
public class HttpConnectionException extends Exception
{
    private static final long serialVersionUID = 5260092003937267281L;

    private final int statusCode;
    private final String statusReason;
    private final String message;

    /**
     * The constructor for this exceptions. It expects the HTTP-status-code and
     * the status-reason as they are defined in RFC 2616. In addition, it
     * expects a describing error message.
     *
     * @param statusCode - The HTTP-status-code
     * @param statusReason - The HTTP-status-reason
     * @param message - The error message
     */
    public HttpConnectionException(int statusCode, String statusReason, String message)
    {
        this.statusCode = statusCode;
        this.statusReason = statusReason;
        this.message = message;
    }

    /**
     * Returns the HTTP-status-code as defined in RFC 2616
     *
     * @return The HTTP-status-code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the HTTP-status-reason as defined in RFC 2616
     *
     * @return The HTTP-status-reason
     */
    public String getStatusReason() {
        return statusReason;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

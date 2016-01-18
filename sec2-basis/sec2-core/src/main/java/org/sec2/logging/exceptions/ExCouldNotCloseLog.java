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

package org.sec2.logging.exceptions;

import org.sec2.logging.LogLevel;

/**
 * Exception for problems during closing a log channel.
 * @author  Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 * 04.01.2011
 */
public class ExCouldNotCloseLog extends ALoggingException {
    /**
     * Default message for the exception.
     */
    public static final String DEFAULT_MESSAGE = "Could not close log channel.";

    /**
     * Default log level for the exception.
     */
    public static final LogLevel DEFAULT_LOGLEVEL = LogLevel.PROBLEM;

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
    public ExCouldNotCloseLog(final String message,
            final Exception exception, final LogLevel loglevel) {
        super(DEFAULT_MESSAGE, exception, DEFAULT_LOGLEVEL);
        if (message != null) {
            this.setMessage(message);
        }
        if (loglevel != null) {
            this.setLogLevel(loglevel);
        }
    }

    /**
     * The default constructor will use predefined values of this 
     * exception and doesn't wrap another exception.
     */
    public ExCouldNotCloseLog() {
        super(DEFAULT_MESSAGE, null, DEFAULT_LOGLEVEL);
    }
}

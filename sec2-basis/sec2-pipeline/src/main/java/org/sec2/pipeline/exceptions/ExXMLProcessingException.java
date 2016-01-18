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
package org.sec2.pipeline.exceptions;

import org.sec2.exceptions.ExAbstractException;
import org.sec2.logging.LogLevel;

/**
 * Exception thrown by the XML processors
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.1
 *
 * Apr 7, 2011
 */
public class ExXMLProcessingException extends ExAbstractException {

    /**
     * Default message for the exception.
     */
    public static final String DEFAULT_MESSAGE = "Problems during XML parsing.";

    /**
     * Constructor for a new, wrapped/unwrapped exception.
     *
     * @param message Reason for this exception or <code>null</code> if the
     * default message should be used.
     * @param exception Wrapped exception which caused the problem, if any
     * or <code>null</code> if there is no exception to wrap.
     * @param loglevel Log level for the generated message or <code>null</code>
     * if this issue should not be logged.
     */
    public ExXMLProcessingException(final String message,
            final Exception exception, final LogLevel loglevel) {
        super(message, exception, loglevel);

    }

    /**
     * The default constructor will use predefined values of this 
     * exception and doesn't wrap another exception.
     */
    public ExXMLProcessingException() {
        super(DEFAULT_MESSAGE, null, DEFAULT_LOGLEVEL);
    }
}

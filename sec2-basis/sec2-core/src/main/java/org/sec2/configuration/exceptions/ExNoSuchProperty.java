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
package org.sec2.configuration.exceptions;

import org.sec2.logging.LogLevel;

/**
 * Exception if no configuration property with the specified key is available.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1 10.06.2013
 */
public class ExNoSuchProperty extends AConfigurationException {

    /**
     * Default message for the exception.
     */
    public static final String DEFAULT_MESSAGE = "No such configuration "
            + "property.";
    /**
     * Default log level for the exception.
     */
    public static final LogLevel DEFAULT_LOGLEVEL = LogLevel.PROBLEM;

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
    public ExNoSuchProperty(final String message,
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
    public ExNoSuchProperty() {
        super(DEFAULT_MESSAGE, null, DEFAULT_LOGLEVEL);
    }
}

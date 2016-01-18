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

import org.sec2.exceptions.ACommonException;
import org.sec2.logging.LogLevel;

/**
 * Marker Exception to subsum all configuration exceptions.
 * @author  Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 * May 19, 2011
 *
 */
public abstract class AConfigurationException extends ACommonException {

/**
     * Constructor for a new, wrapped/unwrapped configuration exception.
     *
     * @param message           Reason for this exception or <code>null</code>
     *                          if the default message should be used.
     * @param wrappedException  Wrapped exception which caused the problem, if
     *                          any or <code>null</code> if there is no
     *                          exception to wrap.
     * @param logLevel          Log level for the generated message or
     *                          <code>null</code> if this issue should not be
     *                          logged.
     */
    protected AConfigurationException(final String message,
            final Exception wrappedException, final LogLevel logLevel) {
        super(message, wrappedException, logLevel);
    }
}

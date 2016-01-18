/*
 * Copyright 2012 Ruhr-University Bochum, Chair for Network and Data Security
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://nds.rub.de/research/projects/sec2/
 */
package org.sec2.saml.client.exceptions;

import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.logging.LogLevel;
import org.sec2.saml.exceptions.AbstractSelfLoggingException;
import org.sec2.saml.exceptions.SeverityLevel;

/**
 * Wraps an {@link AbstractSelfLoggingException} to appear as a
 * {@link ExMiddlewareException}.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * December 04, 2012
 */
public class SAML2MiddlewareProxyException extends ExMiddlewareException {

    /**
     * Constructor.
     * @param wrappedEx The exception to wrap
     */
    public SAML2MiddlewareProxyException(
            final AbstractSelfLoggingException wrappedEx) {
        super(checkWrappedException(wrappedEx).getLocalizedMessage(), wrappedEx,
                translateLevel(wrappedEx.getSeverity()));
    }

    /**
     * Translates the logging levels used in SAML libraries into Sec2 LogLevels.
     * @param in The original level
     * @return The translated level
     */
    private static LogLevel translateLevel(final SeverityLevel in) {
        switch(in) {
            case TRACE: return LogLevel.TRACE;
            case DEBUG: return LogLevel.DEBUG;
            case INFO:  return LogLevel.STATUS;
            case WARN:  return LogLevel.ATTENTION;
            case ERROR: return LogLevel.EMERGENCY;
            default:    return LogLevel.PROBLEM;
        }
    }

    /**
     * Used to check if the wrapped exception is null.
     * Method allows checking before calling supertype constructor.
     * @param wrappedEx The exception to wrap
     * @return The exception to wrap
     */
    private static AbstractSelfLoggingException checkWrappedException(
            final AbstractSelfLoggingException wrappedEx) {
        if (wrappedEx == null) {
            throw new IllegalArgumentException("SAML2MiddlewareProxyException "
                    + "is not intended to be used as standalone exception");
        }
        return wrappedEx;
    }
}

/*
 * Copyright 2013 Ruhr-University Bochum, Chair for Network and Data Security
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

import org.sec2.saml.exceptions.AbstractSelfLoggingException;
import org.sec2.saml.exceptions.SeverityLevel;
import org.slf4j.Marker;

/**
 * An exception telling that the user is not registered at the keyserver.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * September 05, 2013
 */
public class UnregisteredUserException extends AbstractSelfLoggingException {

    /**
     * By default, UnregisteredUserExceptions are an error.
     */
    private static final SeverityLevel DEFAULT_SEVERITY = SeverityLevel.ERROR;

    /**
     * Constructs an instance of <code>UnregisteredUserException</code>
     * with the specified detail message.
     */
    public UnregisteredUserException() {
        this(null, null, null, DEFAULT_SEVERITY, null);
    }

    /**
     * Constructs an instance of <code>UnregisteredUserException</code>.
     * @param pPrefix a prefix that is put before each log message
     * @param pMessage the detail message.
     * @param pSeverity the severity label of the log message
     * @param pCause the cause of this exception.
     * @param pMarker the marker of the log message
     */
    protected UnregisteredUserException(final String pPrefix,
            final String pMessage, final Throwable pCause,
            final SeverityLevel pSeverity, final Marker pMarker) {
        super(pPrefix, pMessage, pCause, pSeverity, pMarker);
    }
}

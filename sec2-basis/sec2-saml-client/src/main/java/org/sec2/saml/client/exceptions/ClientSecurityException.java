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


import org.sec2.saml.exceptions.SAMLEngineException;
import org.sec2.saml.exceptions.SeverityLevel;
import org.slf4j.Marker;

/**
 * An exception telling that a response from the keyserver cannot be processed
 * because it's content is unexpected or not trusted.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * December 07, 2012
 */
public class ClientSecurityException extends SAMLEngineException {

    /**
     * A prefix that is logged before each message.
     */
    private static final String PREFIX = "Security violation";

    /**
     * By default, SAMLEngineSecurityExceptions are an error.
     */
    private static final SeverityLevel DEFAULT_SEVERITY = SeverityLevel.ERROR;

    /**
     * Constructs an instance of <code>ClientSecurityException</code>
     * with the specified detail message.
     * @param msg the detail message.
     */
    public ClientSecurityException(final String msg) {
        this(msg, null, DEFAULT_SEVERITY, null);
    }

    /**
     * Constructs an instance of <code>ClientSecurityException</code>
     * with a cause.
     * @param cause the cause of this exception.
     */
    public ClientSecurityException(final Throwable cause) {
        this(cause.getLocalizedMessage(), cause, DEFAULT_SEVERITY, null);
    }

    /**
     * Constructs an instance of <code>ClientSecurityException</code>
     * with the specified detail message and a cause.
     * @param msg the detail message.
     * @param cause the cause of this exception.
     */
    public ClientSecurityException(final String msg, final Exception cause) {
        this(msg, cause, DEFAULT_SEVERITY, null);
    }

    /**
     * Constructs an instance of <code>ClientSecurityException</code>.
     * @param pMessage the detail message.
     * @param pSeverity the severity label of the log message
     * @param pCause the cause of this exception.
     * @param pMarker the marker of the log message
     */
    public ClientSecurityException(final String pMessage,
            final Throwable pCause, final SeverityLevel pSeverity,
            final Marker pMarker) {
        this(PREFIX, pMessage, pCause, pSeverity, pMarker);
    }

    /**
     * Constructs an instance of <code>ClientSecurityException</code>.
     * @param pPrefix a prefix that is put before each log message
     * @param pMessage the detail message.
     * @param pSeverity the severity label of the log message
     * @param pCause the cause of this exception.
     * @param pMarker the marker of the log message
     */
    protected ClientSecurityException(final String pPrefix,
            final String pMessage, final Throwable pCause,
            final SeverityLevel pSeverity, final Marker pMarker) {
        super(pPrefix, pMessage, pCause, pSeverity, pMarker);
    }
}

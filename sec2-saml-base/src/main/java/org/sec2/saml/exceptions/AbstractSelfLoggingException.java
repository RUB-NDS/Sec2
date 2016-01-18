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
package org.sec2.saml.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 * Abstract prototype of exceptions that can log themselfes via SLF4J.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * Dezember 02, 2012
 */
public class AbstractSelfLoggingException extends Exception {

    /**
     * The log of this class (means: the concrete subclass).
     */
    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * No severity level means error-level.
     */
    private static final SeverityLevel DEFAULT_SEVERITY = SeverityLevel.ERROR;

    /**
     * A prefix that is put before each log message.
     */
    private final String prefix;

    /**
     * the severity label of the log message.
     */
    private final SeverityLevel severity;

    /**
     * the marker of the log message.
     */
    private final Marker marker;

    /**
     * Constructs an instance of <code>AbstractSelfLoggingException</code>.
     * @param pPrefix a prefix that is put before each log message
     * @param pMessage the detail message.
     * @param pSeverity the severity label of the log message
     * @param pCause the cause of this exception.
     * @param pMarker the marker of the log message
     */
    protected AbstractSelfLoggingException(final String pPrefix,
            final String pMessage, final Throwable pCause,
            final SeverityLevel pSeverity, final Marker pMarker) {
        super(pMessage, pCause);
        this.prefix = pPrefix;
        if (pSeverity == null) {
            this.severity = DEFAULT_SEVERITY;
        } else {
            this.severity = pSeverity;
        }
        this.marker = pMarker;
    }

    /**
     * @return a string representation of this exception.
     */
    @Override
    public String toString() {
        String localPrefix = this.getClass().getName();
        if (this.prefix != null && !this.prefix.isEmpty()) {
            localPrefix = this.prefix;
        }
        String message = getLocalizedMessage();
        if (message != null) {
            return localPrefix + ": " + message;
        } else {
            return localPrefix;
        }
    }

    /**
     * Logs this exception.
     * Can be exposed for public by a subclass, but can also be used after the
     * log message has been extended.
     */
    protected final void internalLog() {
        this.logMessage(this.toString());
    }

    /**
     * Sends a log message to the logger.
     *
     * @param message the log message
     */
    protected final void logMessage(final String message) {
        if (this.getCause() == null) {
            switch(this.getSeverity()) {
                case TRACE: log.trace(marker, message); break;
                case DEBUG: log.debug(marker, message); break;
                case INFO:  log.info(marker, message); break;
                case WARN:  log.warn(marker, message); break;
                case ERROR: log.error(marker, message); break;
                default:    log.info(marker, "You extended the "
                        + "SeverityLevel enum without handling the new "
                        + "level(s)"); break;
            }
        } else { // There is a cause
            Throwable t = this.getCause();
            switch(this.getSeverity()) {
                case TRACE: log.trace(marker, message, t); break;
                case DEBUG: log.debug(marker, message, t); break;
                case INFO:  log.info(marker, message, t); break;
                case WARN:  log.warn(marker, message, t); break;
                case ERROR: log.error(marker, message, t); break;
                default:    log.info(marker, "You extended the "
                        + "SeverityLevel enum without handling the new "
                        + "level(s)"); break;
            }
        }
    }

    /**
     * @return the severity
     */
    public SeverityLevel getSeverity() {
        return severity;
    }
}

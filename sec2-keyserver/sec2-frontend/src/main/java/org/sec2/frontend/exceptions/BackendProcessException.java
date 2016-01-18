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
package org.sec2.frontend.exceptions;

import org.sec2.saml.exceptions.SeverityLevel;
import org.slf4j.Marker;

/**
 * An exception telling that the keyserver failed to process a request in the
 * backend.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 26, 2013
 */
public class BackendProcessException extends AbstractKeyserverException {

    /**
     * A prefix that is logged before each message.
     */
    private static final String PREFIX = "Backend failure";
    /**
     * By default, BackendProcessExceptions are an error.
     */
    private static final SeverityLevel DEFAULT_SEVERITY = SeverityLevel.ERROR;
    /**
     * The impact of the exception.
     */
    private Impact impact;
    /**
     * The client's ID.
     */
    private String clientID;
    /**
     * The request's ID.
     */
    private String requestID;

    /**
     * Constructs an instance of
     * <code>BackendProcessException</code> with the specified detail message.
     *
     * @param msg the detail message.
     * @param pClientID The client's ID.
     * @param pRequestID The request's ID.
     * @param pImpact the impact of the exception
     */
    public BackendProcessException(final String msg, final Impact pImpact,
            final String pClientID, final String pRequestID) {
        this(msg, null, DEFAULT_SEVERITY, null, pImpact, pClientID,
                pRequestID);
    }

    /**
     * Constructs an instance of
     * <code>BackendProcessException</code> with a cause.
     *
     * @param cause the cause of this exception.
     * @param pImpact the impact of the exception
     * @param pClientID The client's ID.
     * @param pRequestID The request's ID.
     */
    public BackendProcessException(final Throwable cause,
            final Impact pImpact, final String pClientID,
            final String pRequestID) {
        this(cause.getLocalizedMessage(), cause, DEFAULT_SEVERITY, null,
                pImpact, pClientID, pRequestID);
    }

    /**
     * Constructs an instance of
     * <code>BackendProcessException</code> with the specified detail message
     * and a cause.
     *
     * @param msg the detail message.
     * @param cause the cause of this exception.
     * @param pImpact the impact of the exception
     * @param pClientID The client's ID.
     * @param pRequestID The request's ID.
     */
    public BackendProcessException(final String msg, final Exception cause,
            final Impact pImpact, final String pClientID,
            final String pRequestID) {
        this(msg, cause, DEFAULT_SEVERITY, null, pImpact, pClientID,
                pRequestID);
    }

    /**
     * Constructs an instance of
     * <code>BackendProcessException</code>.
     *
     * @param pMessage the detail message.
     * @param pSeverity the severity label of the log message
     * @param pCause the cause of this exception.
     * @param pMarker the marker of the log message
     * @param pImpact the impact of the exception
     * @param pClientID The client's ID.
     * @param pRequestID The request's ID.
     */
    public BackendProcessException(final String pMessage,
            final Throwable pCause, final SeverityLevel pSeverity,
            final Marker pMarker, final Impact pImpact, final String pClientID,
            final String pRequestID) {
        this(PREFIX, pMessage, pCause, pSeverity, pMarker, pImpact, pClientID,
                pRequestID);
    }

    /**
     * Constructs an instance of
     * <code>BackendProcessException</code>.
     *
     * @param pPrefix a prefix that is put before each log message
     * @param pMessage the detail message.
     * @param pSeverity the severity label of the log message
     * @param pCause the cause of this exception.
     * @param pMarker the marker of the log message
     * @param pImpact the impact of the exception
     * @param pClientID The client's ID.
     * @param pRequestID The request's ID.
     */
    protected BackendProcessException(final String pPrefix,
            final String pMessage, final Throwable pCause,
            final SeverityLevel pSeverity, final Marker pMarker,
            final Impact pImpact, final String pClientID,
            final String pRequestID) {
        super(pPrefix, pMessage, pCause, pSeverity, pMarker);
        this.impact = pImpact;
        this.clientID = pClientID;
        this.requestID = pRequestID;
    }

    /**
     * @return the impact
     */
    public Impact getImpact() {
        return impact;
    }

    /**
     * @return the clientID
     */
    public String getClientID() {
        return clientID;
    }

    /**
     * @return the requestID
     */
    public String getRequestID() {
        return requestID;
    }

    /**
     * Describes the category of error in the backend.
     */
    public static enum Impact {

        /**
         * Something went wrong in processing (e.g. database or HSM connection
         * lost).
         */
        PROCESSING_ERROR,
        /**
         * Some data the client provided was invalid (e.g. invalid certificate).
         */
        INVALID_INPUT,
        /**
         * Something could not be found (e.g. a user, a group).
         */
        NOT_FOUND;
    }
}

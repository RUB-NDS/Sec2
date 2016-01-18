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
package org.sec2.managers.exceptions;

/**
 * An exception telling that an object has been modified on both
 * client and keyserver.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * July 31, 2012
 */
public class SyncConflictException extends Exception {

    /**
     * The default message of an instance of
     * <code>SyncConflictException</code>.
     */
    private static final String DEFAULT_MESSAGE =
              "Object has been modified on both client and keyserver!";

    /**
     * Creates a new instance of <code>SyncConflictException</code>
     * with a default message.
     */
    public SyncConflictException() {
        this(DEFAULT_MESSAGE);
    }

    /**
     * Constructs an instance of <code>SyncConflictException</code>
     * with the specified detail message.
     * @param msg the detail message.
     */
    public SyncConflictException(final String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>SyncConflictException</code>
     * with the specified detail message and a cause.
     * @param msg the detail message.
     * @param cause the cause of this exception.
     */
    public SyncConflictException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs an instance of <code>SyncConflictException</code>
     * with a default message and a cause.
     * @param cause the cause of this exception.
     */
    public SyncConflictException(final Throwable cause) {
        this(DEFAULT_MESSAGE, cause);
    }
}

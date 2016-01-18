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
 * An exception telling that the instance cannot be used because it is out of
 * sync with the keyserver.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * February 27, 2013
 */
public class OutOfSyncException extends Exception {

    /**
     * The default message of an instance of
     * <code>OutOfSyncException</code>.
     */
    private static final String DEFAULT_MESSAGE =
              "Instance cannot be used because it is out of "
            + "sync with the keyserver! Sync it before using it!";

    /**
     * The item that was accessed when the exception was thrown.
     */
    private final Object unsyncedObject;

    /**
     * Creates a new instance of <code>OutOfSyncException</code>
     * with a default message.
     * @param pUnsyncedObject The object that was accessed when the exception
     *          was thrown
     */
    public OutOfSyncException(final Object pUnsyncedObject) {
        this(DEFAULT_MESSAGE, pUnsyncedObject);
    }

    /**
     * Constructs an instance of <code>OutOfSyncException</code>
     * with the specified detail message.
     * @param msg the detail message.
     * @param pUnsyncedObject The object that was accessed when the exception
     *          was thrown
     */
    public OutOfSyncException(final String msg, final Object pUnsyncedObject) {
        super(msg);
        this.unsyncedObject = pUnsyncedObject;
    }

    /**
     * Constructs an instance of <code>OutOfSyncException</code>
     * with the specified detail message and a cause.
     * @param msg the detail message.
     * @param cause the cause of this exception.
     * @param pUnsyncedObject The object that was accessed when the exception
     *          was thrown
     */
    public OutOfSyncException(final String msg, final Throwable cause,
            final Object pUnsyncedObject) {
        super(msg, cause);
        this.unsyncedObject = pUnsyncedObject;
    }

    /**
     * Constructs an instance of <code>OutOfSyncException</code>
     * with a default message and a cause.
     * @param cause the cause of this exception.
     * @param pUnsyncedObject The object that was accessed when the exception
     *          was thrown
     */
    public OutOfSyncException(final Throwable cause,
            final Object pUnsyncedObject) {
        this(DEFAULT_MESSAGE, cause, pUnsyncedObject);
    }

    /**
     * @return the unsyncedObject
     */
    public Object getUnsyncedObject() {
        return unsyncedObject;
    }
}

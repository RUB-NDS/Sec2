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
package org.sec2.exceptions;

import static org.sec2.exceptions.ExAbstractMiddlewareException.DEFAULT_LOGLEVEL;

/**
 * An exception telling that something went wrong initializing a module.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 1, 2013
 */
public class BootstrapException extends ExMiddlewareException {

    /**
     * The default message of an instance of
     * <code>BootstrapException</code>.
     */
    private static final String DEFAULT_MESSAGE =
              "Something went wrong initializing a module!";

    /**
     * A separator between a message and a cause-exception.
     */
    protected static final String SEPARATOR = " -- ";

    /**
     * Creates a new instance of <code>BootstrapException</code>
     * with a default message.
     */
    public BootstrapException() {
        this(DEFAULT_MESSAGE);
    }

    /**
     * Constructs an instance of <code>BootstrapException</code>
     * with the specified detail message.
     * @param msg the detail message.
     */
    public BootstrapException(final String msg) {
        super(msg, null, DEFAULT_LOGLEVEL);
    }

    /**
     * Constructs an instance of <code>BootstrapException</code>
     * with the specified detail message and a cause.
     * @param msg the detail message.
     * @param cause the cause of this exception.
     */
    public BootstrapException(final String msg, final Exception cause) {
        super(msg + SEPARATOR + cause.toString(), cause, DEFAULT_LOGLEVEL);
    }

    /**
     * Constructs an instance of <code>BootstrapException</code>
     * with a default message and a cause.
     * @param cause the cause of this exception.
     */
    public BootstrapException(final Exception cause) {
        this(cause.getMessage(), cause);
    }
}

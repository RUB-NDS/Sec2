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
package org.sec2.exceptions;

/**
 * An exception telling that it is impossible to determine the entity that uses
 * the SAML module.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 05, 2012
 */
public class EntityUnknownException extends ExMiddlewareException {

    /**
     * The default message of an instance of
     * <code>EntityUnknownException</code>.
     */
    private static final String DEFAULT_MESSAGE =
              "It is not possible to determine the registered entity!";

    /**
     * A separator between a message and a cause-exception.
     */
    protected static final String SEPARATOR = " -- ";

    /**
     * Creates a new instance of <code>EntityUnknownException</code>
     * with a default message.
     */
    public EntityUnknownException() {
        this(DEFAULT_MESSAGE);
    }

    /**
     * Constructs an instance of <code>EntityUnknownException</code>
     * with the specified detail message.
     * @param msg the detail message.
     */
    public EntityUnknownException(final String msg) {
        super(msg, null, DEFAULT_LOGLEVEL);
    }

    /**
     * Constructs an instance of <code>EntityUnknownException</code>
     * with the specified detail message and a cause.
     * @param msg the detail message.
     * @param cause the cause of this exception.
     */
    public EntityUnknownException(final String msg,
            final Exception cause) {
        super(msg + SEPARATOR + cause.toString(), cause, DEFAULT_LOGLEVEL);
    }

    /**
     * Constructs an instance of <code>EntityUnknownException</code>
     * with a default message and a cause.
     * @param cause the cause of this exception.
     */
    public EntityUnknownException(final Exception cause) {
        this(cause.getMessage(), cause);
    }
}

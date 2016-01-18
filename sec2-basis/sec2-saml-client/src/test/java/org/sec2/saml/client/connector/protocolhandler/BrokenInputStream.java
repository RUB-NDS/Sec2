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
package org.sec2.saml.client.connector.protocolhandler;

import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream that is broken, allowing only N bytes to be read before
 * throwing an IOException. Data is read from an underlying stream,
 * much like FilterInputStream. Only the single-byte-read is implemented;
 * reads of byte arrays are handled by InputStream's default methods.
 *
 * Adapted from
 *  http://www.javaworld.com/javaworld/jw-07-2002/jw-0719-networkunittest.html
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 06, 2012
 */
public class BrokenInputStream extends InputStream {

    /**
     * By default, break after 5 read() operations.
     */
    private static final long DEFAULT_COUNTER = 5;

    /**
     * The stream that handles the real work.
     */
    private InputStream underlyingIS;

    /**
     * Counter that tells when to be broken.
     */
    private long bytesLeft;

    /**
     * Construct an input stream that reads from the provided input
     * stream, but throws an IOException after maxToRead bytes.
     *
     * @param is The stream that handles the real work
     * @param maxToRead counter that tells how many read()
     *          operations are left before the stream breaks
     */
    public BrokenInputStream(final InputStream is, final long maxToRead) {
        this.underlyingIS = is;
        this.bytesLeft = maxToRead;
    }

    /**
     * Construct an input stream that reads from the provided input
     * stream, but throws an IOException after 5 bytes.
     * @param is The stream that handles the real work
     */
    public BrokenInputStream(final InputStream is) {
        this(is, DEFAULT_COUNTER);
    }

    /**
     * Read a single byte from the underlying input stream, or throw
     * an exception if maxToRead bytes have already been read.
     * @return A byte from the the underlying input stream
     * @throws IOException if maxToRead bytes have already been read
     */
    @Override
    public synchronized int read() throws IOException {
        if (bytesLeft == 0) {
            throw new IOException("Simulated fault on BrokenInputStream read");
        }
        bytesLeft--;
        return underlyingIS.read();
    }
}

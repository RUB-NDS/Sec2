/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.pipeline.handlers.helpers;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Counter output stream reasponsible for counting of the characters sent over
 * the stream (useful for counting overall data to be encrypted and computing
 * the padding value in case of block ciphers)
 *
 * This class utilizes the decorator pattern.
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.1
 */
public final class CounterOutputStream extends FilterOutputStream {

    /**
     * counter
     */
    private int counter;

    /**
     * Default constructor
     *
     * @param os
     */
    public CounterOutputStream(final OutputStream os) {
        super(os);
        counter = 0;
    }

    @Override
    public void write(final byte[] b) throws IOException {
        counter += b.length;
        out.write(b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len)
            throws IOException {
        counter += len;
        out.write(b, off, len);
    }

    @Override
    public void write(final int b) throws IOException {
        counter++;
        out.write(b);
    }

    /**
     *
     * @return number of currently counted bytes sent over the stream
     */
    public int getCounter() {
        return counter;
    }
}

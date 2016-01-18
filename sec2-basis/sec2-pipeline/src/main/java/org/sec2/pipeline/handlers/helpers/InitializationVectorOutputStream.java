/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.pipeline.handlers.helpers;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Class not used, kept for future (maybe useful).
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.1
 */
public final class InitializationVectorOutputStream extends FilterOutputStream {

    /**
     * iv size
     */
    private static final int IV_SIZE = 16;
    /**
     * initialized boolean
     */
    private boolean initialized;
    /**
     * array storing the IV
     */
    private ArrayList<Byte> initializationVector;

    /**
     *
     */
    public InitializationVectorOutputStream(final OutputStream os) {
        super(os);
        initializationVector = new ArrayList<Byte>(IV_SIZE);
    }

    @Override
    public void write(final byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    @Override
    public void write(final byte[] b, final int off, final int len)
            throws IOException {
        int i = 0;
        while (!initialized && i < len) {
            putByte(b[i]);
            i++;
        }

        out.write(b, off + i, len);
    }

    @Override
    public void write(final int b) throws IOException {
        if (!initialized) {
            putByte((byte) b);
        } else {
            out.write(b);
        }
    }

    private void putByte(final byte b) {
        if (!initialized) {
            initializationVector.add(b);
            if (initializationVector.size() == IV_SIZE) {
                initialized = true;
            }
        }
    }

    public boolean isInitialized() {
        return initialized;
    }
}

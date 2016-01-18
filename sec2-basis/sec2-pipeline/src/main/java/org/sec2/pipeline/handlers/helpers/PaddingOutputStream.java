/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.pipeline.handlers.helpers;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * This class buffers the last block. When the close method comes, it takes the
 * last block and extracts padding.
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.1
 */
public final class PaddingOutputStream extends FilterOutputStream {

    /**
     * algorithm block size
     */
    private static final int BLOCK_SIZE = 16;
    /**
     * last block buffered for padding purposes
     */
    private byte[] lastBlock = null;

    /**
     * Default constructor
     *
     * @param os
     */
    public PaddingOutputStream(OutputStream os) {
        super(os);
    }

    @Override
    public void write(final byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) 
            throws IOException {
        // if length of the data is larger than the block size...
        if (len > BLOCK_SIZE) {
            if (lastBlock != null) {
                out.write(lastBlock);
            }
            lastBlock = Arrays.copyOfRange(b, off + len - BLOCK_SIZE, off + len);
            out.write(b, off, len - BLOCK_SIZE);
        } else {
            // otherwise use the write(b) function
            for (int i = 0; i < len; i++) {
                this.write(b[i + off]);
            }
        }
    }

    @Override
    public void close() throws IOException {
        // compute the padding length 
        int length = lastBlock.length - lastBlock[lastBlock.length - 1];
        // write out the last block without padding bytes
        out.write(lastBlock, 0, length);
        out.flush();
        out.close();
    }
}

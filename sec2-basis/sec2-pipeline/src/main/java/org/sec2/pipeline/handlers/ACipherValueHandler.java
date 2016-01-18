/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.pipeline.handlers;

import java.io.PipedInputStream;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import org.sec2.extern.org.apache.commons.codec.binary.Base64OutputStream;
import org.sec2.pipeline.datatypes.XMLSecurityConstants;
import org.sec2.pipeline.exceptions.ExXMLProcessingException;

/**
 * Abstract class for CipherValue handlers
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.1 September 24, 2012
 */
abstract class ACipherValueHandler extends AbstractHandler {

    /**
     * Size of the pipe
     */
    // TODO check this size
    static int PIPE_SIZE = 1024;
    /**
     * pipedReader reading input stream from the pipe and deserializing it into
     * events
     */
    ASec2PipedReader sec2PipedReader;
    /**
     * Piped input stream used by the sec2PipedReader
     */
    PipedInputStream in;
    /**
     * cipher output stream
     */
    CipherOutputStream cipherOutputStream;
    /**
     * base64 output stream
     */
    Base64OutputStream base64OutputStream;
    /**
     * cipher used in the cipherOutputStream
     */
    Cipher cipher;
    /**
     * algorithm
     */
    XMLSecurityConstants.Algorithm algorithm;
    /**
     * symmetric document key used for data decryption / encryption
     */
    byte[] documentKey;

    /**
     * Default constructor
     *
     * @param algorithm
     * @param documentKey
     * @throws ExXMLProcessingException
     */
    public ACipherValueHandler(final XMLSecurityConstants.Algorithm algorithm,
            final byte[] documentKey) throws ExXMLProcessingException {
        super();
        this.algorithm = algorithm;
        this.documentKey = documentKey;
    }

    /**
     * create pipe for decryption / encryption processing
     */
    abstract void createPipe() throws ExXMLProcessingException;

    /**
     * Class used as a thread to convert an InputStream to an Outputstream
     */
    abstract class ASec2PipedReader extends Thread {

        /**
         * Piped Input Stream
         */
        PipedInputStream pipedInputStream;
        /**
         * Exception can be returned if something goes wrong
         */
        ExXMLProcessingException exception;

        /**
         * Constructor
         *
         * @param pin input stream
         * @param h next handler used to send the events
         */
        ASec2PipedReader(final PipedInputStream pin) {
            pipedInputStream = pin;
        }

        /**
         *
         * @return Exception created during pipe reading
         */
        public ExXMLProcessingException getException() {
            return exception;
        }
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.pipeline.handlers;

import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.sec2.extern.org.apache.commons.codec.binary.Base64;
import org.sec2.extern.org.apache.commons.codec.binary.Base64OutputStream;
import org.sec2.extern.javax.xml.namespace.QName;
import org.sec2.extern.javax.xml.stream.XMLEventReader;
import org.sec2.extern.javax.xml.stream.XMLInputFactory;
import org.sec2.extern.javax.xml.stream.XMLStreamException;
import org.sec2.extern.javax.xml.stream.events.XMLEvent;
import org.sec2.logging.LogLevel;
import org.sec2.pipeline.datatypes.DataType;
import org.sec2.pipeline.datatypes.XMLSecurityConstants;
import org.sec2.pipeline.exceptions.ExXMLProcessingException;
import org.sec2.pipeline.handlers.helpers.PaddingOutputStream;
import org.sec2.pipeline.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.jce.provider.BouncyCastleProvider;

/**
 * Class responsible for CipherValue element decryption with a given document
 * key.
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.2 September 24, 2012
 */
final class CipherValueDecryptionHandler extends ACipherValueHandler {

    /**
     * todo get this value from the KeyProcessor
     */
    private static int IV_SIZE = 16;

    /**
     * Get an SLF4J Logger.
     *
     * @return a Logger instance
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(CipherValueDecryptionHandler.class);
    }

    /**
     * XML document structure level
     */
    private boolean initialized;

    /**
     * Default constructor
     *
     * @param algorithm
     * @param documentKey
     *
     * @throws ExXMLProcessingException
     */
    public CipherValueDecryptionHandler(final XMLSecurityConstants.Algorithm algorithm,
      final byte[] documentKey)
      throws ExXMLProcessingException {
        super(algorithm, documentKey);
    }

    @Override
    public void characters(final XMLEvent event)
      throws ExXMLProcessingException {
        try {
            if (!initialized) {
                byte[] eventBytes = Base64.decodeBase64(event.asCharacters()
                  .getData());
                byte[] iv = Arrays.copyOf(eventBytes, IV_SIZE);

                this.initializeDecryption(iv);
                this.createPipe();
                sec2PipedReader.start();
                cipherOutputStream.write(eventBytes, iv.length,
                  (eventBytes.length - iv.length));

                initialized = true;
            } else {
                base64OutputStream.write(event.asCharacters().getData()
                  .getBytes());
            }
        } catch (IOException ex) {
            throw new ExXMLProcessingException(ex.getLocalizedMessage(),
              ex, LogLevel.PROBLEM);
        }
    }

    @Override
    public void endElement(final XMLEvent event)
      throws ExXMLProcessingException {
        QName qName = event.asEndElement().getName();
        if (Utils.checkElement(DataType.ELEMENT.CIPHER_VALUE, qName)) {
            this.finalizeDecryption();
            super.endElement(event);
        }
    }

    @Override
    void createPipe() throws ExXMLProcessingException {
        in = new PipedInputStream(PIPE_SIZE);
        try {
            // create a pipedoutputstream
            PipedOutputStream pos = new PipedOutputStream(in);
            if (!algorithm.isStreaming) {
                // wrapp it with PaddinOutputStream, which extracts padding byte
                PaddingOutputStream padd = new PaddingOutputStream(pos);
                // wrapp it with cipher. thus, each data is decrypted
                cipherOutputStream = new CipherOutputStream(padd, cipher);
            } else {
                // wrapp it with cipher. thus, each data is decrypted
                cipherOutputStream = new CipherOutputStream(pos, cipher);
            }
            // wrapp it with base64 outputstream
            base64OutputStream = new Base64OutputStream(cipherOutputStream,
              false, 0, null);

            sec2PipedReader = new Sec2PipedReader(in);
        } catch (IOException ex) {
            throw new ExXMLProcessingException("PipedWriter initialization "
              + "problem", ex, LogLevel.PROBLEM);
        }
    }

    /**
     * Initializes Cipher according to a given algorithm and a given documentKey
     *
     * @param iv initialization vector
     *
     * @throws ExXMLProcessingException
     */
    void initializeDecryption(final byte[] iv) throws
      ExXMLProcessingException {

        SecretKey skeySpec = new SecretKeySpec(documentKey, "AES");
        IvParameterSpec ivspec = new IvParameterSpec(iv);

        try {
            cipher = Cipher.getInstance(algorithm.javaURI, new BouncyCastleProvider());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivspec);

        } catch (NoSuchAlgorithmException ex) {
            throw new ExXMLProcessingException(ex.getLocalizedMessage(),
              ex, LogLevel.PROBLEM);
        } catch (NoSuchPaddingException ex) {
            throw new ExXMLProcessingException(ex.getLocalizedMessage(),
              ex, LogLevel.PROBLEM);
        } catch (InvalidKeyException ex) {
            throw new ExXMLProcessingException(ex.getLocalizedMessage(),
              ex, LogLevel.PROBLEM);
        } catch (InvalidAlgorithmParameterException ex) {
            throw new ExXMLProcessingException(ex.getLocalizedMessage(),
              ex, LogLevel.PROBLEM);
        }
    }

    /**
     * Finalizes decryption processing
     *
     * @throws ExXMLProcessingException
     */
    void finalizeDecryption() throws ExXMLProcessingException {
        try {
            base64OutputStream.flush();
            base64OutputStream.close();
        } catch (IOException ex) {
            throw new ExXMLProcessingException(ex.getLocalizedMessage(),
              ex, LogLevel.PROBLEM);
        }
        try {
            // wait until the second thread reads all the events
            // and sends it to the next handler
            sec2PipedReader.join(1000);
        } catch (InterruptedException ie) {
            getLogger().debug(ie.getLocalizedMessage());
        }
        if (sec2PipedReader.getException() != null) {
            throw sec2PipedReader.getException();
        }
    }

    /**
     * Class used as a thread to convert an InputStream to an Outputstream
     */
    class Sec2PipedReader extends ASec2PipedReader {

        /**
         * Constructor
         *
         * @param pin input stream
         * @param h   next handler used to send the events
         */
        Sec2PipedReader(final PipedInputStream pin) {
            super(pin);
        }

        @Override
        public void run() {
            try {
                XMLEventReader eventReader = XMLInputFactory.newInstance().
                  createXMLEventReader(pipedInputStream);
                while (eventReader.hasNext()) {
                    XMLEvent event = eventReader.nextEvent();
                    switch (event.getEventType()) {
                        case XMLEvent.START_ELEMENT:
                            nextHandler.startElement(event);
                            break;
                        case XMLEvent.CHARACTERS:
                            nextHandler.characters(event);
                            break;
                        case XMLEvent.END_ELEMENT:
                            nextHandler.endElement(event);
                            break;
                        default:
                            break;
                    }
                }

            } catch (XMLStreamException ex) {
                exception = new ExXMLProcessingException(
                  ex.getLocalizedMessage(), ex, LogLevel.PROBLEM);
            } catch (ExXMLProcessingException ex) {
                exception = ex;
            }
        }
    }
}

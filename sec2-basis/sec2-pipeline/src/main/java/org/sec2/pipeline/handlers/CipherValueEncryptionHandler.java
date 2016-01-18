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
import org.sec2.extern.org.apache.commons.codec.binary.Base64OutputStream;
import org.sec2.core.XMLConstants;
import org.sec2.extern.javax.xml.stream.XMLEventWriter;
import org.sec2.extern.javax.xml.stream.XMLOutputFactory;
import org.sec2.extern.javax.xml.stream.XMLStreamException;
import org.sec2.extern.javax.xml.stream.events.Characters;
import org.sec2.extern.javax.xml.stream.events.XMLEvent;
import org.sec2.logging.LogLevel;
import org.sec2.pipeline.datatypes.XMLSecurityConstants;
import org.sec2.pipeline.exceptions.ExXMLProcessingException;
import org.sec2.pipeline.handlers.helpers.CounterOutputStream;
import org.sec2.pipeline.utils.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.jce.provider.BouncyCastleProvider;

/**
 * CipherValue encryption handler
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.2 September 24, 2012
 */
final class CipherValueEncryptionHandler extends ACipherValueHandler {

    /**
     * Get an SLF4J Logger.
     *
     * @return a Logger instance
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(CipherValueEncryptionHandler.class);
    }
    /**
     * XML document structure level
     */
    private int encryptionLevel;
    /**
     * signalizing if the encryption was finalized
     */
    private boolean finalized;
    /**
     * output writer to the encryption stream
     */
    private XMLEventWriter encryptionEventWriter;
    /**
     * Counts the bytes (for padding purposes)
     */
    private CounterOutputStream counterOutputStream;
    private byte[] iv;
    private HashMap<String, String> namespacePrefixes;

    /**
     * Default constructor
     *
     * It takes care of the whole object and cipher and output streams
     * initialization
     *
     * @param algorithm
     * @param documentKey
     *
     * @throws ExXMLProcessingException
     */
    public CipherValueEncryptionHandler(XMLSecurityConstants.Algorithm algorithm,
            byte[] documentKey)
            throws ExXMLProcessingException {
        super(algorithm, documentKey);
        encryptionLevel = 0;
        this.initializeEncryption();
        this.createPipe();
        namespacePrefixes = new HashMap<String, String>();
        namespacePrefixes.put(XMLSecurityConstants.XMLENC_PREFIX,
                XMLSecurityConstants.XMLENC_NS);
        namespacePrefixes.put(XMLSecurityConstants.XMLSIG_PREFIX,
                XMLSecurityConstants.XMLSIG_NS);
        namespacePrefixes.put(XMLConstants.SEC2_PREFIX, XMLConstants.SEC2_NS);
    }

    @Override
    public void startElement(final XMLEvent event) throws ExXMLProcessingException {
        // start a new thread
        try {
            if (encryptionLevel == 0) {
                // set IV
                sec2PipedReader.start();
                base64OutputStream.write(iv);
            }
            encryptionEventWriter.add(event);
            encryptionLevel++;

        } catch (IOException ex) {
            throw new ExXMLProcessingException(ex.getLocalizedMessage(),
                    ex, LogLevel.PROBLEM);
        } catch (XMLStreamException ex) {
            throw new ExXMLProcessingException(ex.getLocalizedMessage(),
                    ex, LogLevel.PROBLEM);
        }
    }

    @Override
    public void characters(final XMLEvent event) throws ExXMLProcessingException {
        try {
            encryptionEventWriter.add(event);
        } catch (XMLStreamException ex) {
            throw new ExXMLProcessingException(ex.getLocalizedMessage(),
                    ex, LogLevel.PROBLEM);
        }
    }

    @Override
    public void endElement(final XMLEvent event) throws ExXMLProcessingException {
        try {
            encryptionLevel--;
            encryptionEventWriter.add(event);
            if (encryptionLevel == 0) {
                // add padding bytes and close the outputstream
                finalizeEncryption();
                try {
                    // wait until the second thread reads all the events
                    // and sends it to the next handler
                    sec2PipedReader.join(100);
                } catch (InterruptedException ie) {
                    getLogger().debug(ie.getLocalizedMessage());
                }
                if (sec2PipedReader.getException() != null) {
                    throw sec2PipedReader.getException();
                }
                finalized = true;
            }
        } catch (XMLStreamException ex) {
            throw new ExXMLProcessingException(ex.getLocalizedMessage(),
                    ex, LogLevel.PROBLEM);
        }
    }

    @Override
    void createPipe() throws ExXMLProcessingException {
        in = new PipedInputStream(PIPE_SIZE);
        try {
            // create a pipedoutputstream
            PipedOutputStream bos = new PipedOutputStream(in);
            // wrapp it with base64 outputstream
            base64OutputStream = new Base64OutputStream(bos, true, 0, null);
            // wrapp it with cipher. thus, each data is encrypted, base64 enc
            // and written to the pipe
            cipherOutputStream = new CipherOutputStream(base64OutputStream, cipher);
            // count the bytes that are beiing encrypted
            counterOutputStream = new CounterOutputStream(cipherOutputStream);
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
            encryptionEventWriter = factory.createXMLEventWriter(counterOutputStream);
//            encryptionEventWriter.setPrefix("a", "b");
            sec2PipedReader = new Sec2PipedReader(in);
        } catch (XMLStreamException ex) {
            throw new ExXMLProcessingException("PipedWriter initialization "
                    + "problem", ex, LogLevel.PROBLEM);
        } catch (IOException ex) {
            throw new ExXMLProcessingException("PipedWriter initialization "
                    + "problem", ex, LogLevel.PROBLEM);
        }
    }

    public boolean isFinalized() {
        return finalized;
    }

    /**
     * Initializes decryption cipher with a given algorithm and document key
     *
     * @param algorithm
     *
     * @throws ExXMLProcessingException
     */
    void initializeEncryption() throws ExXMLProcessingException {

        SecretKey skeySpec = new SecretKeySpec(documentKey, "AES");
        RandomUtils rnd = RandomUtils.getInstance();
        try {
            iv = rnd.generateIV(algorithm.javaURI);
        } catch (IllegalArgumentException iae) {
            throw new ExXMLProcessingException(iae.getLocalizedMessage(), iae, LogLevel.PROBLEM);
        }
        IvParameterSpec ivspec = new IvParameterSpec(iv);

        try {
            cipher = Cipher.getInstance(algorithm.javaURI, new BouncyCastleProvider());
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivspec);

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
     * Finalizes Encryption processing
     *
     * @throws ExXMLProcessingException
     */
    void finalizeEncryption() throws ExXMLProcessingException {
        try {
            encryptionEventWriter.flush();
            // add padding bytes
            if (!algorithm.isStreaming) {
                int counter = counterOutputStream.getCounter();
                int cipherBlockSize = cipher.getBlockSize();
                int paddingBytesNumber = cipherBlockSize
                        - (counter % cipherBlockSize);
                if (paddingBytesNumber == 0) {
                    paddingBytesNumber = cipherBlockSize;
                }

                byte paddingBytes[] = new byte[paddingBytesNumber];
                Random random = new Random();
                for (int i = 0; i < paddingBytesNumber - 1; i++) {
                    int r = random.nextInt(256);
                    paddingBytes[i] = (byte) r;
                }
                paddingBytes[paddingBytesNumber - 1] = (byte) paddingBytesNumber;
                cipherOutputStream.write(paddingBytes);
                encryptionEventWriter.flush();
            }
            encryptionEventWriter.close();
            try {
                // basically, this should not be necessary as the
                // EncryptionEventWriter should close also the underlying streams.
                // however, this is not the case (bug?) !!! we do it explicitly
                counterOutputStream.close();
            } catch (IOException e) {
                getLogger().debug("closing the counter outputstream failed");
            }
        } catch (XMLStreamException ex) {
            throw new ExXMLProcessingException(ex.getLocalizedMessage(),
                    ex, LogLevel.PROBLEM);
        } catch (IOException ex) {
            throw new ExXMLProcessingException(ex.getLocalizedMessage(),
                    ex, LogLevel.PROBLEM);
        }
    }

    /**
     * Class used as a thread to convert an InputStream to an Outputstream
     */
    private class Sec2PipedReader extends ASec2PipedReader {

        /**
         * Constructor
         *
         * @param pin input stream
         */
        Sec2PipedReader(final PipedInputStream pin) {
            super(pin);
        }

        @Override
        public void run() {
            try {
                char[] buffer;
                buffer = new char[PIPE_SIZE];
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        pipedInputStream));
                int read;
                while (!finalized || br.ready()) {
                    read = br.read(buffer, 0, buffer.length);
                    if (read != -1) {
                        Characters c = eventFactory.createCharacters(
                                new String(buffer, 0, read));
                        nextHandler.characters(c);
                    }
                }

            } catch (ExXMLProcessingException ex) {
                exception = ex;
            } catch (IOException ex) {
                exception = new ExXMLProcessingException(
                        ex.getLocalizedMessage(), exception, LogLevel.DEBUG);
            }
        }
    }
}

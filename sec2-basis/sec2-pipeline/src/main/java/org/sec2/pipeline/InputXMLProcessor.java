/*
 * Copyright 2011 Sec2 Consortium
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit written
 * permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://www.sec2.org
 */
package org.sec2.pipeline;

import java.io.Reader;
import java.io.Writer;
import org.sec2.logging.LogLevel;
import org.sec2.managers.exceptions.KeyManagerException;
import org.sec2.pipeline.exceptions.ExXMLProcessingException;
import org.sec2.pipeline.handlers.AbstractHandler;
import org.sec2.pipeline.handlers.EncryptedDataDecryptionHandler;
import org.sec2.pipeline.handlers.SerializationHandler;

/**
 * XML Processor responsible for processing the incoming communication from the
 * cloud, i.e. it takes the input stream and decrypts the EncryptedData elements
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.1
 *
 * Apr 7, 2011
 */
public final class InputXMLProcessor extends XMLProcessor {

    /**
     * This constructor takes input stream, which is processed by the xml stream
     * reader. The processed stream is then forwarded using a newly created
     * socket to the (addr, port) destination.
     *
     * @param r
     * @param w
     * @throws ExXMLProcessingException
     */
    public InputXMLProcessor(final Reader r, final Writer w)
            throws ExXMLProcessingException {
        super(r, w);
    }

    @Override
    public final void createHandlerChain() throws ExXMLProcessingException {
        try {
            AbstractHandler ah = new AbstractHandler() {
            };
            EncryptedDataDecryptionHandler dh = new EncryptedDataDecryptionHandler();
            SerializationHandler sh = new SerializationHandler(eventWriter);
            dh.insertNextHandler(sh);
            ah.insertNextHandler(dh);

            firstHandler = ah;
        } catch (KeyManagerException ex) {
            throw new ExXMLProcessingException(ex.getLocalizedMessage(), ex, LogLevel.PROBLEM);
        }
    }
}

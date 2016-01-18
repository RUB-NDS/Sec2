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
package org.sec2.pipeline.handlers;

import java.util.LinkedList;
import java.util.List;
import org.sec2.extern.javax.xml.namespace.QName;
import org.sec2.extern.javax.xml.stream.events.Attribute;
import org.sec2.extern.javax.xml.stream.events.XMLEvent;
import org.sec2.managers.IDocumentKeyManager;
import org.sec2.managers.exceptions.KeyManagerException;
import org.sec2.managers.factories.KeyManagerFactory;
import org.sec2.pipeline.datatypes.DataType;
import org.sec2.pipeline.datatypes.XMLSecurityConstants;
import org.sec2.pipeline.exceptions.ExXMLProcessingException;
import org.sec2.pipeline.utils.Utils;
import org.sec2.token.keys.DocumentKey;
import org.sec2.token.keys.ExtendedDocumentKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EncryptedData decryption handler used for decrypting EncryptedData elements
 * coming from the cloud.
 *
 * This class is responsible for parsing and processing the EncryptedData
 * fields. It uses two helper handlers: CipherValueDecryptionHandler (for
 * processing and decrypting cipher value elements) and KeyInfoProcessingHandler
 * (for processing and decrypting document keys).
 *
 * Both handlers are inserted before this handler in the pipe. It is possible to
 * insert more CipherValueDecryptionHandlers into the pipe. This ensures that
 * nested encrypted data could be processed (output of one
 * CipherValueDecryptionHandler is used as an input for the next
 * CipherValueDecryptionHandler.)
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.2
 *
 * September 24, 2012
 */
public final class EncryptedDataDecryptionHandler extends AbstractHandler {

    /**
     * Get an SLF4J Logger.
     *
     * @return a Logger instance
     */
    Logger log = LoggerFactory.getLogger(EncryptedDataDecryptionHandler.class);
    /**
     * Processing state during EncryptedData processing
     */
    private ProcessingState processingState = ProcessingState.NONE;
    /**
     * encryption method
     */
    private XMLSecurityConstants.Algorithm encryptionMethod;
    /**
     * currently used document key
     */
    private byte[] documentKey;
    /**
     * A direct reference to the previous handler processing encrypted keys:
     * KeyInfoProcessingHandler (only during the KEY_INFO processing state)
     */
    private KeyInfoProcessingHandler encryptedKeyHandler;
    /**
     * DocumentKeyManager instance
     */
    private IDocumentKeyManager documentKeyManager;
    private List<XMLEvent> eventCacheList;

    /**
     * Default constructor
     */
    public EncryptedDataDecryptionHandler() throws KeyManagerException {
        super();
        documentKeyManager = KeyManagerFactory.getDocumentKeyManager();
        eventCacheList = new LinkedList<XMLEvent>();
    }

    @Override
    public void startElement(final XMLEvent event)
            throws ExXMLProcessingException {
        QName qName = event.asStartElement().getName();
        if (qName != null) {
            switch (processingState) {
                case CIPHER_VALUE:
                case NONE:
                    if (Utils.checkElement(DataType.ELEMENT.ENCRYPTED_DATA, qName)) {
                        log.debug("EncryptedData element found");
                        processingState = ProcessingState.ENCRYPTED_DATA;
                        eventCacheList.add(event);
                    } else {
                        super.startElement(event);
                    }
                    break;
                case ENCRYPTED_DATA:
                    if (Utils.checkElement(DataType.ELEMENT.KEY_INFO, qName)) {
                        log.debug("KeyInfo element found, creating new "
                                + "KeyInfoProcessingHandler");
                        processingState = ProcessingState.KEY_INFO;
                        encryptedKeyHandler = new KeyInfoProcessingHandler();
                        this.insertPreviousHandler(encryptedKeyHandler);
                        eventCacheList.add(event);
                    } else if (Utils.checkElement(
                            DataType.ELEMENT.ENCRYPTION_METHOD, qName)) {
                        Attribute a = event.asStartElement().getAttributeByName(
                                new QName(XMLSecurityConstants.XMLENC_NS,
                                DataType.ELEMENT.ENCRYPTION_METHOD.getAttribute()));
                        encryptionMethod = XMLSecurityConstants.Algorithm.
                                fromString(a.getValue());
                        eventCacheList.add(event);
                        eventCacheList.add(createEndElement(DataType.ELEMENT.ENCRYPTION_METHOD));
                    } else if (Utils.checkElement(
                            DataType.ELEMENT.CIPHER_VALUE, qName)) {
                        processingState = ProcessingState.CIPHER_VALUE;
                        log.debug("CipherValue element found, creating new "
                                + "CipherValueDecryptionHandler");
                        CipherValueDecryptionHandler edh =
                                new CipherValueDecryptionHandler(
                                encryptionMethod, documentKey);
                        this.insertPreviousHandler(edh);
                    }
                    break;
                default:
            }
        }
    }

    @Override
    public void endElement(final XMLEvent event)
            throws ExXMLProcessingException {

        QName qName = event.asEndElement().getName();
        switch (processingState) {
            case ENCRYPTED_DATA:
                if (Utils.checkElement(DataType.ELEMENT.ENCRYPTED_DATA,
                        qName)) {
                    // maybe there is still a different decryption handler
                    if (isDecrypting()) {
                        processingState = ProcessingState.CIPHER_VALUE;
                    } else {
                        processingState = ProcessingState.NONE;
                    }
                }
                break;
            case CIPHER_VALUE:
                if (Utils.checkElement(DataType.ELEMENT.CIPHER_VALUE, qName)) {
                    this.removePreviousHandler();

                    processingState = ProcessingState.ENCRYPTED_DATA;
                } else {
                    super.endElement(event);
                }
                break;
            default:
                super.endElement(event);
        }

    }

    @Override
    public void keyInfoProcessingFinished() throws ExXMLProcessingException {
        log.debug("KeyInfo processing finished, removing the "
                + "KeyInfoProcessingHandler");
        processingState = ProcessingState.ENCRYPTED_DATA;
        this.removePreviousHandler();
        List<DocumentKey> encryptedKeys =
                encryptedKeyHandler.getEncryptedDocumentKeys();
        try {
            ExtendedDocumentKey edk =
                    documentKeyManager.getDecryptedDocumentKey(encryptedKeys);
            documentKey = edk.getDecryptedDocumentKey().getKey().getBytes();
        } catch (KeyManagerException kme) {
            // no document key, we have no rights to decrypt it
            // push back the events
            log.debug("Cannot decrypt the document key: "
                    + kme.getLocalizedMessage(), kme);
            for (XMLEvent e : eventCacheList) {
                nextHandler.handleEvent(e);
            }
            for (XMLEvent e : encryptedKeyHandler.getEventCacheList()) {
                nextHandler.handleEvent(e);
            }
            eventCacheList.clear();
            documentKey = null;
            processingState = ProcessingState.NONE;
        }
    }

    /**
     * @return true if the pipe is currently processing a cipher value. This is
     * found out according to the previous handler.
     */
    private boolean isDecrypting() {
        boolean ret = false;
        if (previousHandler != null) {
            if (previousHandler instanceof CipherValueDecryptionHandler) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * TODO remove this method, only for testing purposes
     *
     * @param dkm
     */
    public void setDocumentKeyManager(IDocumentKeyManager dkm) {
        this.documentKeyManager = dkm;
    }
}
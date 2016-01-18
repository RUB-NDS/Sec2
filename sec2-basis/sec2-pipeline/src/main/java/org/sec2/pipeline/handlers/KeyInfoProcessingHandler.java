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
import org.sec2.extern.org.apache.commons.codec.binary.Base64;
import org.sec2.extern.javax.xml.stream.events.StartElement;
import org.sec2.extern.javax.xml.stream.events.XMLEvent;
import org.sec2.pipeline.datatypes.DataType;
import org.sec2.pipeline.datatypes.EncryptedKey;
import org.sec2.pipeline.exceptions.ExXMLProcessingException;
import org.sec2.pipeline.utils.Utils;
import org.sec2.token.keys.DocumentKey;
import org.sec2.token.keys.ExtendedDocumentKey;

/**
 * Handler responsible for KeyInfo parsing within the EncryptedData element
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.2
 *
 * September 24, 2012
 */
public final class KeyInfoProcessingHandler extends AbstractHandler {

    private String documentKeyName;
    /**
     * list of encrypted keys
     */
    private List<EncryptedKey> encryptedKeys;
    /**
     * currently processed encrypted key
     */
    private EncryptedKey currentKey;
    /**
     * current processing state
     */
    private ProcessingState processingState;
    private List<XMLEvent> eventCacheList;

    public KeyInfoProcessingHandler() {
        super();
        processingState = ProcessingState.KEY_INFO;
        encryptedKeys = new LinkedList<EncryptedKey>();
        eventCacheList = new LinkedList<XMLEvent>();
    }

    @Override
    public void startElement(final XMLEvent event)
            throws ExXMLProcessingException {

        eventCacheList.add(event);
        StartElement se = event.asStartElement();
        switch (processingState) {
            case KEY_INFO:
                if (Utils.checkElement(DataType.ELEMENT.ENCRYPTED_KEY,
                        se.getName())) {
                    currentKey = new EncryptedKey();
                    processingState = ProcessingState.ENCRYPTED_KEY;
                } else if (Utils.checkElement(DataType.ELEMENT.KEY_NAME,
                        se.getName())) {
                    processingState = ProcessingState.DOCUMENT_KEY_NAME;
                }
                break;
            case ENCRYPTED_KEY:
                if (Utils.checkElement(DataType.ELEMENT.KEY_NAME,
                        se.getName())) {
                    processingState = ProcessingState.GROUP_KEY_NAME;
                } else if (Utils.checkElement(DataType.ELEMENT.CIPHER_VALUE,
                        se.getName())) {
                    processingState = ProcessingState.CIPHER_VALUE;
                }
                break;
            default:
        }
    }

    @Override
    public void characters(final XMLEvent event)
            throws ExXMLProcessingException {
        eventCacheList.add(event);
        switch (processingState) {
            case DOCUMENT_KEY_NAME:
                documentKeyName = event.asCharacters().getData();
                break;
            case GROUP_KEY_NAME:
                currentKey.setGroupKeyId(event.asCharacters().getData());
                currentKey.setKeyId(event.asCharacters().getData()
                        + ExtendedDocumentKey.GROUP_SEPARATOR + documentKeyName);
                break;
            case CIPHER_VALUE:
                currentKey.setCipherValue(event.asCharacters().getData());
                break;
        }
    }

    @Override
    public void endElement(final XMLEvent event)
            throws ExXMLProcessingException {

        eventCacheList.add(event);
        switch (processingState) {
            case DOCUMENT_KEY_NAME:
                processingState = ProcessingState.KEY_INFO;
                break;
            case GROUP_KEY_NAME:
            case CIPHER_VALUE:
                processingState = ProcessingState.ENCRYPTED_KEY;
                break;
            case ENCRYPTED_KEY:
                if (Utils.checkElement(DataType.ELEMENT.ENCRYPTED_KEY,
                        event.asEndElement().getName())) {
                    processingState = ProcessingState.KEY_INFO;
                    encryptedKeys.add(currentKey);
                }
                break;
            case KEY_INFO:
                // send the element event to the EncryptedDataDecryptionHandler,
                // so that it would know KeyInfo element is finished
                if (Utils.checkElement(DataType.ELEMENT.KEY_INFO,
                        event.asEndElement().getName())) {
                    super.keyInfoProcessingFinished();
                }
                break;
            default:
        }
    }

    public List<XMLEvent> getEventCacheList() {
        return eventCacheList;
    }

    public List<DocumentKey> getEncryptedDocumentKeys() {
        List<DocumentKey> documentKeys = new LinkedList<DocumentKey>();
        for (EncryptedKey ek : encryptedKeys) {
            byte[] cv = Base64.decodeBase64(ek.getCipherValue());
            DocumentKey dk = new DocumentKey(cv, true, ek.getKeyId());
            documentKeys.add(dk);
        }
        return documentKeys;
    }

    public String getDocumentKeyName() {
        return documentKeyName;
    }

    public void generateKeyInfoEvents(ExtendedDocumentKey extendedDocumentKey)
            throws ExXMLProcessingException {

        // key info with the document key name
        nextHandler.startElement(createStartElement(DataType.ELEMENT.KEY_INFO));
        nextHandler.startElement(createStartElement(DataType.ELEMENT.KEY_NAME));
        nextHandler.characters(eventFactory.createCharacters(
                extendedDocumentKey.getKeyIdWithNonce()));
        nextHandler.endElement(createEndElement(DataType.ELEMENT.KEY_NAME));

        // iterate over all the encrypted keys and insert them into the key info
        // structure
        for (DocumentKey dk : extendedDocumentKey.getEncryptedDocumentKeys()) {
            EncryptedKey ek = new EncryptedKey(dk);

            nextHandler.startElement(createStartElement(DataType.ELEMENT.ENCRYPTED_KEY));

            // key info with the group key name
            nextHandler.startElement(createStartElement(DataType.ELEMENT.KEY_INFO));
            nextHandler.startElement(createStartElement(DataType.ELEMENT.KEY_NAME));
            nextHandler.characters(eventFactory.createCharacters(ek.getGroupKeyId()));
            nextHandler.endElement(createEndElement(DataType.ELEMENT.KEY_NAME));
            nextHandler.endElement(createEndElement(DataType.ELEMENT.KEY_INFO));

            // CipherData and CipherValue
            nextHandler.startElement(createStartElement(DataType.ELEMENT.CIPHER_DATA));
            nextHandler.startElement(createStartElement(DataType.ELEMENT.CIPHER_VALUE));

            nextHandler.characters(eventFactory.createCharacters(ek.getCipherValue()));

            nextHandler.endElement(createEndElement(DataType.ELEMENT.CIPHER_VALUE));
            nextHandler.endElement(createEndElement(DataType.ELEMENT.CIPHER_DATA));
            nextHandler.endElement(createEndElement(DataType.ELEMENT.ENCRYPTED_KEY));
        }

        nextHandler.endElement(createEndElement(DataType.ELEMENT.KEY_INFO));;
    }
}
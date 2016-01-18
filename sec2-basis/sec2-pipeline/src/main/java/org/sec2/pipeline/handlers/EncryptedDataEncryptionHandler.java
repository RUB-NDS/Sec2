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

import java.util.*;
import org.sec2.core.XMLConstants;
import org.sec2.extern.javax.xml.stream.events.Attribute;
import org.sec2.extern.javax.xml.stream.events.Namespace;
import org.sec2.extern.javax.xml.stream.events.StartElement;
import org.sec2.extern.javax.xml.stream.events.XMLEvent;
import org.sec2.logging.LogLevel;
import org.sec2.managers.IDocumentKeyManager;
import org.sec2.managers.exceptions.KeyManagerException;
import org.sec2.managers.factories.KeyManagerFactory;
import org.sec2.pipeline.datatypes.DataType;
import org.sec2.pipeline.datatypes.XMLSecurityConstants;
import org.sec2.pipeline.exceptions.ExXMLProcessingException;
import org.sec2.pipeline.utils.Utils;
import org.sec2.token.keys.ExtendedDocumentKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EncryptedData encryption handler used for encrypting plaintext elements and
 * storing them in an EncryptedData structure
 *
 * This class is responsible for parsing and processing plaintext data. If it
 * finds annotated elements (with sec2:Groups and sec2:Algorithm parameters), it
 * extracts their data and proceeds with encryption. For encryption purposes it
 * utilizes the CipherValueEncryptionHandler. This handler is inserted as a next
 * handler into the processing pipe and it proceeds with encryption
 * independently. It is possible to insert more CipherValueEncryptionHandler
 * modules into the pipe so that nested encryptions are possible.
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.2
 *
 * September 24, 2012
 */
public final class EncryptedDataEncryptionHandler extends AbstractHandler {

    /**
     * Get an SLF4J Logger.
     *
     * @return a Logger instance
     */
    Logger log = LoggerFactory.getLogger(EncryptedDataEncryptionHandler.class);
    /**
     * DocumentKeyManager instance
     */
    private IDocumentKeyManager documentKeyManager;
    /**
     * extended DocumentKey with the information about the encrypted group keys
     * and containing the decrypted document key
     */
    private ExtendedDocumentKey extendedDocumentKey;
//    /**
//     * list of namespace prefixes seen in the document
//     */
//    private Set<String> namespacePrefixes;

    /**
     * Default constructor
     */
    public EncryptedDataEncryptionHandler() throws KeyManagerException {
        super();
        documentKeyManager = KeyManagerFactory.getDocumentKeyManager();
//        namespacePrefixes = new HashSet<String>();
    }

    @Override
    public void startElement(final XMLEvent event)
            throws ExXMLProcessingException {
        StartElement se = event.asStartElement();
//        Iterator<Namespace> namespaces = se.getNamespaces();
//        while (namespaces.hasNext()) {
//            Namespace ns = namespaces.next();
//            namespacePrefixes.add(ns.getPrefix());
//        }
        Iterator<Attribute> iter = se.getAttributes();
        // search for a sec2:groups attribute
        String groups = Utils.getAttribute(XMLConstants.SEC2_NS,
                XMLConstants.SEC2_GROUPS, iter);
        if (groups != null) {
            try {
                log.debug("Groups found, using document key manager to get the "
                        + "document key");
                extendedDocumentKey = documentKeyManager.getDecryptedDocumentKeyForGroups(groups);
                iter = event.asStartElement().getAttributes();
                // Search for the algorithm attribute. If not found, use the default
                // algorithm.
                String algorithm = Utils.getAttribute(XMLConstants.SEC2_NS,
                        XMLConstants.SEC2_ALGORITHM, iter);
                if (algorithm == null) {
                    algorithm = XMLConstants.DEFAULT_ALGORITHM;
                }
                XMLSecurityConstants.Algorithm a = XMLSecurityConstants.Algorithm.
                        fromString(algorithm);

                nextHandler.startElement(createStartElement(
                        DataType.ELEMENT.ENCRYPTED_DATA));

                // enc method
                Attribute att = eventFactory.createAttribute(
                        DataType.ELEMENT.ENCRYPTION_METHOD.getNamespacePrefix(),
                        DataType.ELEMENT.ENCRYPTION_METHOD.getNamespace(),
                        DataType.ELEMENT.ENCRYPTION_METHOD.getAttribute(),
                        a.xmlURI);
                List<Attribute> attributeList = Collections.singletonList(att);
                List<Namespace> namespaceList = new LinkedList<Namespace>();
                StartElement em = createStartElementWithAttributes(
                        DataType.ELEMENT.ENCRYPTION_METHOD,
                        attributeList.iterator(), namespaceList.iterator());

                nextHandler.startElement(em);
                nextHandler.endElement(createEndElement(DataType.ELEMENT.ENCRYPTION_METHOD));

                // key info
                KeyInfoProcessingHandler kiph = new KeyInfoProcessingHandler();
                this.insertNextHandler(kiph);
                kiph.generateKeyInfoEvents(extendedDocumentKey);
                this.removeNextHandler();

                // CipherData and CipherValue
                nextHandler.startElement(createStartElement(DataType.ELEMENT.CIPHER_DATA));
                nextHandler.startElement(createStartElement(DataType.ELEMENT.CIPHER_VALUE));

                // CipherValueEncryptionHandler inserted on the next position
                // so that all the next events will be encrypted
                this.insertNextHandler(new CipherValueEncryptionHandler(a,
                        extendedDocumentKey.getDecryptedDocumentKey().getKey().getBytes()));
            } catch (KeyManagerException ex) {
                // the user has probably no rights to encrypt a file for the specfied group
                throw new ExXMLProcessingException(ex.getLocalizedMessage(), ex, LogLevel.PROBLEM);
            }
        }
        super.startElement(event);
    }

    @Override
    public void endElement(final XMLEvent event) throws ExXMLProcessingException {
        super.endElement(event);
        if (nextHandler instanceof CipherValueEncryptionHandler) {
            if (((CipherValueEncryptionHandler) nextHandler).isFinalized()) {
                this.removeNextHandler();
                nextHandler.endElement(createEndElement(DataType.ELEMENT.CIPHER_VALUE));
                nextHandler.endElement(createEndElement(DataType.ELEMENT.CIPHER_DATA));
                nextHandler.endElement(createEndElement(DataType.ELEMENT.ENCRYPTED_DATA));
            }
        }
    }

    /**
     * todo for testing purposes, remove this method
     *
     * @param dkm
     */
    public void setDocumentKeyManager(IDocumentKeyManager dkm) {
        this.documentKeyManager = dkm;
    }
}
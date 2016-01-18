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

import java.util.Iterator;
import org.sec2.extern.javax.xml.stream.XMLEventFactory;
import org.sec2.extern.javax.xml.stream.events.Attribute;
import org.sec2.extern.javax.xml.stream.events.EndElement;
import org.sec2.extern.javax.xml.stream.events.Namespace;
import org.sec2.extern.javax.xml.stream.events.StartElement;
import org.sec2.extern.javax.xml.stream.events.XMLEvent;
import org.sec2.pipeline.datatypes.DataType;
import org.sec2.pipeline.exceptions.ExXMLProcessingException;

/**
 * This is an Abstract Handler for processing basic events in the pipe. It is
 * responsible for passing events to the next pipe handlers, inserting or
 * removing given handlers
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.2
 *
 * September 24, 2012
 */
public abstract class AbstractHandler {

    /**
     * An ENUM defining processing state during data processing
     */
    enum ProcessingState {

        NONE, ENCRYPTED_DATA, ENCRYPTED_KEY, KEY_INFO, DOCUMENT_KEY_NAME,
        GROUP_KEY_NAME, CIPHER_VALUE;
    }
    /**
     * next handler in the pipe
     */
    AbstractHandler nextHandler;
    /**
     * previous handler in the pipe
     */
    AbstractHandler previousHandler;
    /**
     * event factory for constructing events
     */
    XMLEventFactory eventFactory;

    /**
     * default constructor
     */
    public AbstractHandler() {
        this.eventFactory = XMLEventFactory.newInstance();
    }

    /**
     * Inserts next handler into the pipe.
     *
     * @param newHandler
     */
    public void insertNextHandler(final AbstractHandler newHandler) {
        if (this.nextHandler != null) {
            newHandler.nextHandler = this.nextHandler;
            this.nextHandler.previousHandler = newHandler;
        }

        this.nextHandler = newHandler;
        newHandler.previousHandler = this;
    }

    /**
     * Inserts a new handler into the pipe before the current handler
     *
     * @param newHandler
     */
    public void insertPreviousHandler(final AbstractHandler newHandler) {
        if (this.previousHandler != null) {
            this.previousHandler.nextHandler = newHandler;
            newHandler.previousHandler = this.previousHandler;
        }

        this.previousHandler = newHandler;
        newHandler.nextHandler = this;
    }

    /**
     * Removes the next handler from the pipe.
     */
    public void removeNextHandler() {
        if (this.nextHandler != null) {
            this.nextHandler = this.nextHandler.nextHandler;
            this.nextHandler.previousHandler = this;
        }
    }

    /**
     * Removes the previous handler from the pipe.
     */
    public void removePreviousHandler() {
        if (this.previousHandler != null) {
            this.previousHandler = this.previousHandler.previousHandler;
            this.previousHandler.nextHandler = this;
        }
    }

    /**
     * StartElement event
     *
     * The concrete class has to implement this method if it wants to use each
     * incoming startElement event
     *
     * @param event
     * @throws ExXMLProcessingException Returned if the XML data is invalid or
     * if the underlying logic threw an exception (e.g. the document key could
     * not be found etc.)
     */
    public void startElement(final XMLEvent event) throws ExXMLProcessingException {
        if (nextHandler != null) {
            nextHandler.startElement(event);
        }
    }

    /**
     * characters event
     *
     * The concrete class has to implement this method if it wants to use each
     * incoming characters event
     *
     * @param event
     * @throws ExXMLProcessingException Returned if the XML data is invalid or
     * if the underlying logic threw an exception (e.g. the document key could
     * not be found etc.)
     */
    public void characters(final XMLEvent event) throws ExXMLProcessingException {
        if (nextHandler != null) {
            nextHandler.characters(event);
        }
    }

    /**
     * endElement event
     *
     * The concrete class has to implement this method if it wants to use each
     * incoming endElement event
     *
     * @param event
     * @throws ExXMLProcessingException Returned if the XML data is invalid or
     * if the underlying logic threw an exception (e.g. the document key could
     * not be found etc.)
     */
    public void endElement(final XMLEvent event) throws ExXMLProcessingException {
        if (nextHandler != null) {
            nextHandler.endElement(event);
        }
    }

    /**
     * Handle an event different from startElement, characters and endElement
     *
     * @param event
     * @throws ExXMLProcessingException Returned if the XML data is invalid or
     * if the underlying logic threw an exception (e.g. the document key could
     * not be found etc.)
     */
    public void handleEvent(final XMLEvent event) throws ExXMLProcessingException {
        if (nextHandler != null) {
            nextHandler.handleEvent(event);
        }
    }

    /**
     * Signalizes that KeyInfo processing is finished...necessary for the
     * EncryptedData Handlers
     */
    public void keyInfoProcessingFinished() throws ExXMLProcessingException {
        if (nextHandler != null) {
            nextHandler.keyInfoProcessingFinished();
        }
    }

    public EndElement createEndElement(DataType.ELEMENT element) {
        return eventFactory.createEndElement(
                element.getNamespacePrefix(), element.getNamespace(),
                element.getElementName());
    }

    public StartElement createStartElement(DataType.ELEMENT element) {
        return eventFactory.createStartElement(
                element.getNamespacePrefix(), element.getNamespace(),
                element.getElementName());
    }

    public StartElement createStartElementWithAttributes(
            DataType.ELEMENT element, Iterator<Attribute> attributeIterator,
            Iterator<Namespace> namespaceIterator) {
        return eventFactory.createStartElement(
                element.getNamespacePrefix(), element.getNamespace(),
                element.getElementName(), attributeIterator,
                namespaceIterator);
    }
}

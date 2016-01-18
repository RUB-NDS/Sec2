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

import org.sec2.extern.javax.xml.stream.XMLEventWriter;
import org.sec2.extern.javax.xml.stream.XMLStreamException;
import org.sec2.extern.javax.xml.stream.events.XMLEvent;
import org.sec2.logging.LogLevel;
import org.sec2.pipeline.exceptions.ExXMLProcessingException;

/**
 * This is the last event handler in the pipe. It just takes each event and
 * writes it to the output using an XMLEventWriter.
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.1
 *
 * May 13, 2011
 */
public final class SerializationHandler extends AbstractHandler {

    /**
     * XMLEventWriter
     */
    private XMLEventWriter eventWriter;

    /**
     *
     * @param ew xml event writer
     */
    public SerializationHandler(final XMLEventWriter ew) {
        super();
        eventWriter = ew;
    }

    @Override
    public void startElement(final XMLEvent event)
            throws ExXMLProcessingException {
        try {
            eventWriter.add(event);
        } catch (XMLStreamException ex) {
            throw new ExXMLProcessingException(ex.getLocalizedMessage(),
                    ex, LogLevel.PROBLEM);
        }
    }

    @Override
    public void characters(final XMLEvent event) throws ExXMLProcessingException {
        try {
            eventWriter.add(event);
        } catch (XMLStreamException ex) {
            throw new ExXMLProcessingException(ex.getLocalizedMessage(),
                    ex, LogLevel.PROBLEM);
        }
    }

    @Override
    public void endElement(final XMLEvent event) 
            throws ExXMLProcessingException {
        try {
            eventWriter.add(event);
        } catch (XMLStreamException ex) {
            throw new ExXMLProcessingException(ex.getLocalizedMessage(),
                    ex, LogLevel.PROBLEM);
        }
    }

    @Override
    public void handleEvent(final XMLEvent event)
            throws ExXMLProcessingException {
        try {
            eventWriter.add(event);
        } catch (XMLStreamException ex) {
            throw new ExXMLProcessingException(ex.getLocalizedMessage(),
                    ex, LogLevel.PROBLEM);
        }
    }
}

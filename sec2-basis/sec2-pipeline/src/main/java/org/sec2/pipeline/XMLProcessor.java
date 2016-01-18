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
import org.sec2.extern.javax.xml.stream.FactoryConfigurationError;
import org.sec2.extern.javax.xml.stream.XMLEventReader;
import org.sec2.extern.javax.xml.stream.XMLEventWriter;
import org.sec2.extern.javax.xml.stream.XMLInputFactory;
import org.sec2.extern.javax.xml.stream.XMLOutputFactory;
import org.sec2.extern.javax.xml.stream.XMLStreamException;
import org.sec2.extern.javax.xml.stream.events.XMLEvent;
import org.sec2.logging.LogLevel;
import org.sec2.pipeline.exceptions.ExXMLProcessingException;
import org.sec2.pipeline.handlers.AbstractHandler;

/**
 * XML Processor for Input and Output Stream processing
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.1
 *
 * Apr 7, 2011
 */
public abstract class XMLProcessor {

    /**
     * Reads the events from the InputStream.
     */
    XMLEventReader eventReader;
    /**
     * Writes the newly created events to the OutputStream.
     */
    XMLEventWriter eventWriter;
    /**
     * field holding the first handler in the event processing pipe
     */
    AbstractHandler firstHandler;

    /**
     * This constructor takes input stream, which is processed by the xml stream
     * reader. The processed stream is then forwarded using a newly created
     * socket to the (addr, port) destination.
     *
     * @param r
     * @param w
     * @throws ExXMLProcessingException
     */
    XMLProcessor(final Reader r, final Writer w)
            throws ExXMLProcessingException {
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            eventReader = inputFactory.createXMLEventReader(r);

            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
            eventWriter = outputFactory.createXMLEventWriter(w);
        } catch (FactoryConfigurationError fce) {
            throw new ExXMLProcessingException(fce.getLocalizedMessage(), null,
                    LogLevel.PROBLEM);
        } catch (XMLStreamException xse) {
            throw new ExXMLProcessingException(xse.getLocalizedMessage(), xse,
                    LogLevel.PROBLEM);
        }
    }

    /**
     * Creates the handler chain for the stream processing
     */
    abstract void createHandlerChain() throws ExXMLProcessingException;

    /**
     * Processes the input stream using the newly created chain.
     *
     * @throws ExXMLProcessingException
     */
    public final void processXMLStream() throws ExXMLProcessingException {

        try {
            int level = -1;
            while (eventReader.hasNext()) {
                if (level == 0) {
                    break;
                }
                XMLEvent event = eventReader.nextEvent();
                switch (event.getEventType()) {
                    case XMLEvent.START_ELEMENT:
                        // check if the it is the first start element
                        if (level == -1) {
                            // set the level to 0
                            level = 0;
                        }
                        level++;
                        firstHandler.startElement(event);
                        break;
                    case XMLEvent.CHARACTERS:
                        firstHandler.characters(event);
                        break;
                    case XMLEvent.END_ELEMENT:
                        level--;
                        firstHandler.endElement(event);
                        break;
                    default:
                        break;
                }
            }
        } catch (XMLStreamException xse) {
            throw new ExXMLProcessingException(xse.getLocalizedMessage(), xse,
                    LogLevel.PROBLEM);
        } finally {
            try {
                eventReader.close();
                eventWriter.close();
            } catch (XMLStreamException xse) {
                throw new ExXMLProcessingException(xse.getLocalizedMessage(), 
                        xse, LogLevel.PROBLEM);
            }
        }
    }
}

/*
 * Copyright 2013 Ruhr-University Bochum, Chair for Network and Data Security
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://nds.rub.de/research/projects/sec2/
 */
package org.sec2.frontend.processors;

import org.opensaml.xml.util.Base64;
import org.sec2.frontend.exceptions.BackendProcessException;
import org.sec2.saml.engine.XSElementGenerator;
import org.sec2.saml.xml.Sec2RequestMessage;
import org.sec2.saml.xml.Sec2ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract prototype of a processor.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @param <Q> The type of Sec2RequestMessage to process
 * @param <R> The type of Sec2ResponseMessage to return
 * @version 0.1
 *
 * July 26, 2013
 */
abstract class AbstractSec2MessageProcessor<Q extends Sec2RequestMessage,
                R extends Sec2ResponseMessage>
                implements ISec2RequestMessageProcessor {

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * @return the logger
     */
    protected final Logger getLogger() {
        return logger;
    }

    /**
     * Generator for basic XML types like xs:string, xs:integer, etc.
     */
    private XSElementGenerator xsGenerator = new XSElementGenerator();

    /** {@inheritDoc} */
    @Override
    public final Sec2ResponseMessage process(final BackendJob job)
            throws BackendProcessException {
        // throws ClassCastException if the dispatcher has a wrong mapping
        Q request = (Q) job.getSec2Object();
        return this.process(request, Base64.decode(job.getClientID()),
                job.getRequestID());
    }

    /**
     * Processes a BackendJob with a specific Sec2RequestMessage in it.
     *
     * @param sec2message the specific Sec2RequestMessage
     * @param clientID The client's ID
     * @param requestID The request's ID
     * @return the result of the operation or null if the processing was
     *          successful, but has no return value
     * @throws BackendProcessException if something in the backend processing
     *          failed
     */
    abstract R process(final Q sec2message,
            final byte[] clientID, final String requestID)
            throws BackendProcessException;

    /**
     * @return the xsGenerator
     */
    protected final XSElementGenerator getXsGenerator() {
        return xsGenerator;
    }
}

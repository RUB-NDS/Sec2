/*
 * Copyright 2012 Ruhr-University Bochum, Chair for Network and Data Security
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

import org.sec2.frontend.exceptions.BackendProcessException;
import org.sec2.saml.xml.Sec2ResponseMessage;

/**
 * Declares a process-function for BackendJobs.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 22, 2012
 */
public interface ISec2RequestMessageProcessor {

    /**
     * Processes a BackendJob.
     * @param job the BackendJob to process
     * @return the result of the operation or null if the processing was
     *          successful, but has no return value
     * @throws BackendProcessException if something in the backend processing
     *          failed
     */
    Sec2ResponseMessage process(final BackendJob job)
            throws BackendProcessException;
}

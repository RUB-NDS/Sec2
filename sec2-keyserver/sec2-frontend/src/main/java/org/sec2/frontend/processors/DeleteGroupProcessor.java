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

import org.opensaml.xml.util.Base64;
import org.sec2.backend.exceptions.PermissionException;
import org.sec2.frontend.BackendHolder;
import org.sec2.frontend.exceptions.BackendProcessException;
import org.sec2.saml.xml.DeleteGroup;
import org.sec2.saml.xml.Sec2ResponseMessage;

/**
 * A processor for DeleteGroup requests.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * May 29, 2013
 */
class DeleteGroupProcessor
                extends AbstractSec2MessageProcessor<DeleteGroup,
                        Sec2ResponseMessage> {

    /** {@inheritDoc} */
    @Override
    public final Sec2ResponseMessage process(final DeleteGroup sec2message,
            final byte[] clientID, final String requestID)
            throws BackendProcessException {
        try {
            BackendHolder.getBackend().deleteGroup(clientID,
                    sec2message.getValue());
        } catch (PermissionException e) {
            throw new BackendProcessException(e,
                    BackendProcessException.Impact.PROCESSING_ERROR,
                    Base64.encodeBytes(clientID), requestID);
        }

        this.getLogger().debug("User '{}' deleted group with name {}",
                Base64.encodeBytes(clientID), sec2message.getValue());

        return null;
    }
}

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
import org.sec2.backend.IGroupInfo;
import org.sec2.backend.exceptions.PermissionException;
import org.sec2.backend.exceptions.UserNotFoundException;
import org.sec2.frontend.BackendHolder;
import org.sec2.frontend.exceptions.BackendProcessException;
import org.sec2.saml.xml.GetGroup;
import org.sec2.saml.xml.GroupInfo;

/**
 * A processor for GetGroup requests.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * July 18, 2013
 */
class GetGroupProcessor extends
        AbstractGroupInfoRespondingProcessor<GetGroup> {

    /** {@inheritDoc} */
    @Override
    public final GroupInfo process(final GetGroup sec2message,
            final byte[] clientID, final String requestID)
            throws BackendProcessException {
        IGroupInfo group;
        try {
            group = BackendHolder.getBackend().getGroupInfo(clientID,
                    sec2message.getValue());
        } catch (UserNotFoundException e) {
            throw new BackendProcessException(e,
                    BackendProcessException.Impact.NOT_FOUND,
                    Base64.encodeBytes(clientID), requestID);
        } catch (PermissionException e) {
            throw new BackendProcessException(e,
                    BackendProcessException.Impact.PROCESSING_ERROR,
                    Base64.encodeBytes(clientID), requestID);
        }

        this.getLogger().debug("User '{}' requested info about group with name "
                + "'{}'", Base64.encodeBytes(clientID), group.getGroupName());

        return this.createGroupInfo(group);
    }
}

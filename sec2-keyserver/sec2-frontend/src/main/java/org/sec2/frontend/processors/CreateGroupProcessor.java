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

import java.util.Arrays;
import org.opensaml.xml.util.Base64;
import org.sec2.backend.IGroupInfo;
import org.sec2.backend.exceptions.GroupAlreadyExistsException;
import org.sec2.backend.exceptions.InvalidGroupNameException;
import org.sec2.backend.exceptions.PermissionException;
import org.sec2.backend.exceptions.UserNotFoundException;
import org.sec2.frontend.BackendHolder;
import org.sec2.frontend.exceptions.BackendProcessException;
import org.sec2.saml.xml.*;

/**
 * A processor for CreateGroup requests.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 28, 2012
 */
class CreateGroupProcessor extends
        AbstractGroupInfoRespondingProcessor<CreateGroup> {

    /** {@inheritDoc} */
    @Override
    public final GroupInfo process(final CreateGroup sec2message,
            final byte[] clientID, final String requestID)
            throws BackendProcessException {
        // check pre-conditions
        String clientBase64 = Base64.encodeBytes(clientID);
        if (!Arrays.equals(Base64.decode(
                sec2message.getGroupOwnerID().getValue()), clientID)) {
            throw new BackendProcessException("A new group's owner has to be"
                    + " the client who issued the request to create "
                    + "the new group",
                    BackendProcessException.Impact.INVALID_INPUT,
                    Base64.encodeBytes(clientID), requestID);
        }

        IGroupInfo newGroup;
        try {
            newGroup = BackendHolder.getBackend().createGroup(clientID,
                sec2message.getGroupName().getValue());
        } catch (GroupAlreadyExistsException e) {
            throw new BackendProcessException(e,
                    BackendProcessException.Impact.INVALID_INPUT,
                    Base64.encodeBytes(clientID), requestID);
        } catch (UserNotFoundException e) {
            throw new BackendProcessException(e,
                    BackendProcessException.Impact.NOT_FOUND,
                    Base64.encodeBytes(clientID), requestID);
        } catch (InvalidGroupNameException e) {
            throw new BackendProcessException(e,
                    BackendProcessException.Impact.INVALID_INPUT,
                    Base64.encodeBytes(clientID), requestID);
        } catch (PermissionException e) {
            throw new BackendProcessException(e,
                    BackendProcessException.Impact.PROCESSING_ERROR,
                    Base64.encodeBytes(clientID), requestID);
        }

        this.getLogger().debug("User '{}' created a new group with name '{}'",
                clientBase64, newGroup.getGroupName());

        return this.createGroupInfo(newGroup);
    }
}

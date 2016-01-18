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

import org.opensaml.xml.schema.XSBase64Binary;
import org.opensaml.xml.util.Base64;
import org.sec2.backend.IGroupInfo;
import org.sec2.backend.IUserInfo;
import org.sec2.backend.exceptions.PermissionException;
import org.sec2.backend.exceptions.UserNotFoundException;
import org.sec2.frontend.BackendHolder;
import org.sec2.frontend.exceptions.BackendProcessException;
import org.sec2.saml.xml.AddUsersToGroup;
import org.sec2.saml.xml.GroupInfo;

/**
 * A processor for AddUsersToGroup requests.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * July 26, 2013
 */
class AddUsersToGroupProcessor extends
        AbstractGroupInfoRespondingProcessor<AddUsersToGroup> {

    /** {@inheritDoc} */
    @Override
    public final GroupInfo process(final AddUsersToGroup sec2message,
            final byte[] clientID, final String requestID)
            throws BackendProcessException {
        IGroupInfo group;
        int count = 0;
        try {
            group = BackendHolder.getBackend().getGroupInfo(clientID,
                    sec2message.getGroupName().getValue());
            for (XSBase64Binary userID : sec2message.getUsers()) {
                IUserInfo user = BackendHolder.getBackend().getUserInfo(
                        Base64.decode(userID.getValue()));
                if (!group.getMembers().contains(user)) {
                    group.getMembers().add(user);
                    count++;
                } else {
                    this.getLogger().debug("User '{}' wants to add user '{}' "
                            + "to group '{}', "
                            + "but this user is already part of the group.",
                            Base64.encodeBytes(clientID),
                            Base64.encodeBytes(user.getId()),
                            group.getGroupName());
                }
            }
            group = BackendHolder.getBackend().modifyGroup(clientID, group);
        } catch (UserNotFoundException e) {
            throw new BackendProcessException(e,
                    BackendProcessException.Impact.NOT_FOUND,
                    Base64.encodeBytes(clientID), requestID);
        } catch (PermissionException e) {
            throw new BackendProcessException(e,
                    BackendProcessException.Impact.PROCESSING_ERROR,
                    Base64.encodeBytes(clientID), requestID);
        }

        this.getLogger().debug("User '{}' added {} user(s) to group '{}'",
                Base64.encodeBytes(clientID), count,
                group.getGroupName());

        return this.createGroupInfo(group);
    }
}

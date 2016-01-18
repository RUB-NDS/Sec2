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

import java.util.Arrays;
import org.opensaml.xml.schema.XSBase64Binary;
import org.opensaml.xml.util.Base64;
import org.sec2.backend.IGroupInfo;
import org.sec2.backend.IUserInfo;
import org.sec2.backend.exceptions.PermissionException;
import org.sec2.backend.exceptions.UserNotFoundException;
import org.sec2.frontend.BackendHolder;
import org.sec2.frontend.exceptions.BackendProcessException;
import org.sec2.saml.xml.GroupInfo;
import org.sec2.saml.xml.RemoveUsersFromGroup;

/**
 * A processor for RemoveUsersFromGroup requests.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * July 22, 2013
 */
class RemoveUsersFromGroupProcessor extends
        AbstractGroupInfoRespondingProcessor<RemoveUsersFromGroup> {

    /**
     * {@inheritDoc}
     */
    @Override
    public final GroupInfo process(
            final RemoveUsersFromGroup sec2message, final byte[] clientID,
            final String requestID) throws BackendProcessException {
        IGroupInfo group;
        int count = 0;
        try {
            group = BackendHolder.getBackend().getGroupInfo(clientID,
                    sec2message.getGroupName().getValue());
            for (XSBase64Binary userID : sec2message.getUsers()) {
                IUserInfo user = BackendHolder.getBackend().getUserInfo(
                        Base64.decode(userID.getValue()));
                if (group.getMembers().contains(user)) {
                    if (Arrays.equals(user.getId(), clientID)) {
                        this.getLogger().error("User '{}' cannot remove "
                                + "himself from group '{}'.",
                                Base64.encodeBytes(clientID),
                                group.getGroupName());
                        throw new BackendProcessException(
                                "User tries to remove himself from group",
                                BackendProcessException.Impact.INVALID_INPUT,
                                Base64.encodeBytes(clientID), requestID);
                    }
                    group.getMembers().remove(user);
                    count++;
                } else {
                    this.getLogger().debug("User '{}' wants to remove user "
                            + "'{}' from group "
                            + "'{}', but this user is not part of the group.",
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

        this.getLogger().debug("User '{}' removed {} user(s) from group '{}'",
                Base64.encodeBytes(clientID), count, group.getGroupName());

        return this.createGroupInfo(group);
    }
}

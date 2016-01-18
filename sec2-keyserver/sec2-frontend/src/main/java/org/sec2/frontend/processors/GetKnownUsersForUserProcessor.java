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

import java.util.List;
import org.opensaml.xml.util.Base64;
import org.sec2.backend.IUserInfo;
import org.sec2.frontend.BackendHolder;
import org.sec2.frontend.exceptions.BackendProcessException;
import org.sec2.saml.xml.GetKnownUsersForUser;
import org.sec2.saml.xml.UserList;

/**
 * A processor for GetKnownUsersForUser requests.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * July 16, 2012
 */
class GetKnownUsersForUserProcessor extends
        AbstractUserListRespondingProcessor<GetKnownUsersForUser> {

    /** {@inheritDoc} */
    @Override
    public final UserList process(
            final GetKnownUsersForUser sec2message, final byte[] clientID,
            final String requestID) throws BackendProcessException {
        List<IUserInfo> knownUsers =
                BackendHolder.getBackend().getAllKnownUsers(clientID);

        this.getLogger().debug("User '{}' requested a list of known users",
                Base64.encodeBytes(clientID));

        return this.createUserList(knownUsers);
    }
}

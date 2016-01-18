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
import org.sec2.backend.IUserInfo;
import org.sec2.backend.exceptions.UserNotFoundException;
import org.sec2.frontend.BackendHolder;
import org.sec2.frontend.exceptions.BackendProcessException;
import org.sec2.saml.xml.GetUserInfoByID;
import org.sec2.saml.xml.UserInfo;

/**
 * A processor for GetUserInfoByID requests.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * July 22, 2013
 */
class GetUserInfoByIDProcessor extends
        AbstractUserInfoRespondingProcessor<GetUserInfoByID> {

    /**
     * {@inheritDoc}
     */
    @Override
    public final UserInfo process(final GetUserInfoByID sec2message,
            final byte[] clientID, final String requestID)
            throws BackendProcessException {
        IUserInfo userInfo;
        try {
            userInfo = BackendHolder.getBackend().getUserInfo(
                    Base64.decode(sec2message.getValue()));
        } catch (UserNotFoundException e) {
            this.getLogger().debug("User '{}' requested information about user "
                    + "'{}', but this user was not found",
                    Base64.encodeBytes(clientID), sec2message.getValue());
            throw new BackendProcessException(e,
                    BackendProcessException.Impact.NOT_FOUND,
                    Base64.encodeBytes(clientID), requestID);
        }

        checkUser(userInfo, clientID, requestID);

        this.getLogger().debug("User '{}' requested information about user "
                + "'{}'", Base64.encodeBytes(clientID),
                Base64.encodeBytes(userInfo.getId()));

        return this.createUserInfo(userInfo);
    }
}

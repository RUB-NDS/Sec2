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

import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.util.Base64;
import org.sec2.backend.IUserInfo;
import org.sec2.frontend.exceptions.BackendProcessException;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.engine.XMLHelper;
import org.sec2.saml.xml.EmailAddress;
import org.sec2.saml.xml.GetUserInfoByID;
import org.sec2.saml.xml.Sec2RequestMessage;
import org.sec2.saml.xml.UserID;
import org.sec2.saml.xml.UserInfo;

/**
 * Abstract prototype of a processor that returns a UserInfo object.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @param <T> The type of Sec2RequestMessage to process
 * @version 0.1
 *
 * November 28, 2012
 */
abstract class AbstractUserInfoRespondingProcessor<T extends Sec2RequestMessage>
    extends AbstractSec2MessageProcessor<T, UserInfo> {

    /**
     * Creates an XML UserInfo from a backend IUserInfo object.
     *
     * @param backendObject The backend IUserInfo
     * @return an XML UserInfo
     */
    protected final UserInfo createUserInfo(final IUserInfo backendObject) {
        // create response payload
        UserInfo payload = SAMLEngine.getXMLBuilder(UserInfo.class,
                UserInfo.DEFAULT_ELEMENT_NAME).buildObject(
                UserInfo.DEFAULT_ELEMENT_NAME);

        // set user ID
        payload.setUserID(this.getXsGenerator().buildXSBase64Binary(
                UserID.DEFAULT_ELEMENT_NAME));
        payload.getUserID().setValue(Base64.encodeBytes(backendObject.getId()));
        payload.setEmailAddress(SAMLEngine.getXMLBuilder(EmailAddress.class,
                EmailAddress.DEFAULT_ELEMENT_NAME).buildObject(
                EmailAddress.DEFAULT_ELEMENT_NAME));
        payload.getEmailAddress().setValue(backendObject.getEmailAddress());

        if (backendObject.getEmailAddress().isEmpty()) {
            this.getLogger().error(
                    "email empty", new Throwable());
        }

        // saves performance if logging is disabled
        if (this.getLogger().isTraceEnabled()) {
            try {
                this.getLogger().trace("UserInfo content: {}",
                        XMLHelper.getXMLString(payload));
            } catch (MarshallingException e) {
                // Can't fix this here. But the exception will occur again when
                // the payload is encrypted und will be handled there
                this.getLogger().error(
                        "Generated response payload could not be marshalled");
            }
        }
        return payload;
    }

    /**
     * Checks if the user can be returned to the client.
     *
     * @param userInfo the user's info
     * @param clientID The client's ID.
     * @param requestID The request's ID.
     * @throws BackendProcessException if the user cannot be returned to the
     * client.
     */
    protected void checkUser(final IUserInfo userInfo,
            final byte[] clientID, final String requestID)
            throws BackendProcessException {
        if (!userInfo.isConfirmed()) {
            this.getLogger().debug("User '{}' requested information about user "
                    + "'{}', but this user is not yet confirmed",
                    Base64.encodeBytes(clientID),
                    Base64.encodeBytes(userInfo.getId()));
            throw new BackendProcessException("User is unconfirmed",
                    BackendProcessException.Impact.NOT_FOUND,
                    Base64.encodeBytes(clientID), requestID);
        }
    }
}

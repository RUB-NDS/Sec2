/*
 * Copyright 2011 Ruhr-University Bochum, Chair for Network and Data Security
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://nds.rub.de/research/projects/sec2/
 */
package org.sec2.managers;

import java.io.IOException;
import java.util.List;
import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.managers.beans.User;

/**
 * Manager Interface used to manage users: view, get and modify current user.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.3
 *
 * August 01, 2012
 */
public interface IUserManager {

    /**
     * Returns the user who is logged into the middleware (and whose smartcard
     * is inserted etc.).
     *
     * @return the user who is currently logged in
     */
    User getRegisteredUser();

    /**
     * Returns a list with user-IDs of all users which are known to the user who
     * is logged into the middleware (and whose smartcard is inserted etc.).
     *
     * @return a list with user-IDs of all users which are known to the
     * registered user
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     * @throws KeyserverConnectionLostException if no connection to the
     * keyserver is available.
     */
    List<byte[]> getKnownUsers() throws ExMiddlewareException, IOException;

    /**
     * Returns the user with the passed user ID or null if no user was found.
     *
     * @param userID The user ID of the user to be returned
     * @return The user with the passed ID
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     * @throws KeyserverConnectionLostException if no connection to the
     * keyserver is available.
     */
    User getUser(byte[] userID)
            throws ExMiddlewareException, IOException;

    /**
     * Returns the user with the passed email address or null if no user was
     * found.
     *
     * @param emailAddress The email address of the user to be returned
     * @return The user with the passed email address
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     * @throws KeyserverConnectionLostException if no connection to the
     * keyserver is available.
     */
    User getUser(String emailAddress)
            throws ExMiddlewareException, IOException;

    /**
     * Changes the email address of the user who is currently logged in.
     *
     * @param newEmailAddress The user's new email address
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     * @throws KeyserverConnectionLostException if no connection to the
     * keyserver is available.
     * @deprecated Not implemented yet
     */
    @Deprecated
    void changeEmailAddressOfRegisteredUser(String newEmailAddress)
            throws ExMiddlewareException, IOException;
}

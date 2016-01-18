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
package org.sec2.managers;

import java.io.IOException;
import org.sec2.exceptions.ExMiddlewareException;

/**
 * Manager Interface used to register users.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.3
 *
 * September 05, 2013
 */
public interface IUserRegistrationManager {

    /**
     * Confirms the current user's account at the keyserver.
     *
     * @param challenge the challenge the user got somehow
     * @throws ExMiddlewareException if something went wrong creating or parsing
     * a SAML message.
     * @throws IOException if something with the network connection breaks
     */
    void confirmUser(final String challenge)
            throws IOException, ExMiddlewareException;

    /**
     * Registeres the current user with a given email address.
     *
     * @param emailAddress the email address of the current user
     * @throws ExMiddlewareException if something went wrong creating or parsing
     * a SAML message.
     * @throws IOException if something with the network connection breaks
     */
    void registerUser(final String emailAddress)
            throws ExMiddlewareException, IOException;
}

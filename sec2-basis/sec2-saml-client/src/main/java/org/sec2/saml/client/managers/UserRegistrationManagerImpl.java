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
package org.sec2.saml.client.managers;

import java.io.IOException;
import org.sec2.managers.IUserRegistrationManager;
import org.sec2.saml.client.connector.KeyserverConnectorFactory;
import org.sec2.saml.client.exceptions.SAML2MiddlewareProxyException;
import org.sec2.saml.exceptions.SAMLEngineException;

/**
 * Manager used to register the current user.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.4
 *
 * September 05, 2013
 */
public class UserRegistrationManagerImpl implements IUserRegistrationManager {

    /**
     * Registeres the current user with a given email address.
     *
     * @param emailAddress the email address of the current user
     * @throws SAML2MiddlewareProxyException if something went wrong creating or
     * parsing a SAML message.
     * @throws IOException if something with the network connection breaks
     */
    @Override
    public synchronized void registerUser(final String emailAddress)
            throws SAML2MiddlewareProxyException, IOException {
        try {
            KeyserverConnectorFactory.getKeyserverConnector().
                    registerUser(emailAddress);
        } catch (SAMLEngineException ex) {
            throw new SAML2MiddlewareProxyException(ex);
        }
    }

    /**
     * Confirms the current user's account at the keyserver.
     *
     * @param challenge the challenge the user got somehow
     * @throws SAML2MiddlewareProxyException if something went wrong creating or
     * parsing a SAML message.
     * @throws IOException if something with the network connection breaks
     */
    @Override
    public synchronized void confirmUser(final String challenge)
            throws IOException, SAML2MiddlewareProxyException {
        try {
            KeyserverConnectorFactory.getKeyserverConnector().
                    confirmUser(challenge);
        } catch (SAMLEngineException ex) {
            throw new SAML2MiddlewareProxyException(ex);
        }
    }
}

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
package org.sec2.saml.client;

import java.io.IOException;
import org.sec2.exceptions.EntityUnknownException;
import org.sec2.managers.ManagerProvider;
import org.sec2.saml.client.exceptions.UnregisteredUserException;
import org.sec2.saml.client.managers.GroupManagerImpl;
import org.sec2.saml.client.managers.UserManagerImpl;
import org.sec2.saml.client.managers.UserRegistrationManagerImpl;
import org.sec2.saml.exceptions.SAMLEngineException;

/**
 * This class can be used to bootstrap the Sec2 SAML Client module with the
 * default configurations.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 1, 2013
 */
public class SAMLClientBootstrap {

    /**
     * Tells if the module has been initialized.
     */
    private static boolean initialized = false;

    /**
     * @return the initialized status
     */
    public static synchronized boolean isInitialized() {
        return initialized;
    }

    /**
     * Constructor.
     */
    protected SAMLClientBootstrap() {
    }

    /**
     * Initializes the module loading default configurations.
     *
     * @throws EntityUnknownException if the registered user cannot be
     * determined (on the local device)
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     * @throws IOException if the current user cannot be fetched from the
     * keyserver
     * @throws UnregisteredUserException if the user is not yet registered at
     * the keyserver
     */
    public static synchronized void bootstrap() throws EntityUnknownException,
            SAMLEngineException, IOException, UnregisteredUserException {
        if (!isInitialized()) {
            /*
             * It's important that the UserManager is bootstrapped first.
             * It has to recognize if the User is not yet registered at the
             * keyserver.
             */
            ManagerProvider.getInstance().setUserRegistrationManager(
                    new UserRegistrationManagerImpl());
            ManagerProvider.getInstance().setUserManager(
                    UserManagerImpl.getInstance());
            ManagerProvider.getInstance().setGroupManager(
                    GroupManagerImpl.getInstance());
            initialized = true;
        }
    }
}

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

import javax.crypto.SecretKey;

import org.sec2.managers.exceptions.CreateAuthKeyException;


/**
 * Manager Interface used to manage the app keys: create, delete, get existing
 *
 * @author  Juraj Somorovsky - juraj.somorovsky@rub.de
 * @date    May 3, 2012
 * @version 0.1
 *
 */
public interface IAppKeyManager
{
    /**
     * Creates a new symmeric key for app authentication
     * 
     * @param appName - The app name. The name must be unique. Otherwise the existing key for an app with the same name
     * is overwritten.
     * 
     * @return The symmetric key for app authentication
     */
    public SecretKey createRequestValidationKey(String appName) throws CreateAuthKeyException;

    /**
     * Returns the symmetric key for app authentication
     * 
     * @param appName - The app name for the requested key
     * 
     * @return The symmetric key for app authentication or NULL if no key was found for the passed name
     */
    public SecretKey getKeyForApp(String appName);

    /**
     * Deletes the symmetric key for app authentication for the app with the passed app name.
     * 
     * @param appName - The name of the app whose authentication key is to deleted
     * 
     * @return TRUE, if the key was successfully deleted or if no key was deleted, because no key was found for the passed
     *     app name. If no key was deleted, because an error has occured during the deletion process, FALSE is returned.
     */
    public boolean deleteKeyForApp(String appName);

    /**
     * Returns an array with the IDs of all apps, which are registered at the Sec2-middleware.
     * 
     * @return An array with the IDs of all apps, which are registered at the Sec2-middleware. Shouldn't return NULL, but an
     *  empty array, if no ID could be found.
     */
    public String[] getRegisteredAppIds();
}
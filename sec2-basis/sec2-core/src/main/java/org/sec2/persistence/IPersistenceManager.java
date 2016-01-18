package org.sec2.persistence;

import javax.crypto.SecretKey;

/**
 * This interface defines methods for data persistence.
 *
 * @author nike
 */
public interface IPersistenceManager
{
    /**
     * Saves the app authentication key the app's name, where the key belongs
     * to, in the database. If for the passed app's name a key already exists
     * in the database, the key in the database is updated with the new, passed
     * key.
     * 
     * @param key - The app authentication key
     * @param appName - The app's name
     * 
     * @return TRUE, if the key was successfully saved, FALSE otherwise
     */
    public boolean saveAppAuthKey(SecretKey key, String appName);

    /**
     * Returns the app authentication key for the passed app's name.
     * 
     * @param appName - The app's name for the requested key
     * 
     * @return The symmetric key for app authentication or NULL if no key was found for the passed name
     */
    public SecretKey getAppAuthKey(String appName);

    /**
     * Deletes the symmetric key for app authentication for the app with the passed app name.
     * 
     * @param appName - The name of the app whose authentication key is to deleted
     * 
     * @return TRUE, if the key was successfully deleted or if no key was deleted, because no key was found for the passed
     *     app name. If no key was deleted, because an error has occured during the deletion process, FALSE is returned.
     */
    public boolean deleteAppAuthKey(String appName);

    /**
     * Returns an array with the IDs of all apps, which are registered at the Sec2-middleware. Shouldn't return NULL, but an
     *  empty array, if no ID could be found.
     * 
     * @return An array with the IDs of all apps, which are registered at the Sec2-middleware.
     */
    public String[] getRegisteredAppIds();

    /**
     * Returns TRUE if the passed nonce could be found in the database.
     * Otherwise FALSE is returned.
     *
     * @return TRUE if the passed nonce could be found in the database.
     *  Otherwise FALSE.
     */
    public boolean isNonceInDb(String nonce);

    /**
     * Saves the passed timestamp and nonce in the database.
     *
     * @param timestamp - The timestamp to save
     * @param nonce - The nonce to save
     *
     * @return TRUE if the passed nonce could successfully be saved in the
     *  database. Otherwise FALSE.
     */
    public boolean saveNonceInDb(long timestamp, String nonce);

    /**
     * Deletes all nonces, which are older than the threshold, from the
     * database.
     * 
     * @param threshold - The threshold
     * 
     * @return TRUE if nonces were successfully deleted or if no nonce was
     *  deleted, because no nonce was older than the threshold. If no nonce was
     *  deleted, because an error has occured during the deletion process,
     *  FALSE is returned.
     */
    public boolean deleteOldNonceInDb(long threshold);
}
package org.sec2.mwserver.core;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.sec2.logging.LogLevel;
import org.sec2.managers.IAppKeyManager;
import org.sec2.managers.exceptions.CreateAuthKeyException;
import org.sec2.persistence.IPersistenceManager;
import org.sec2.persistence.IPersistenceManagerListener;
import org.sec2.persistence.PersistenceManagerContainer;

/**
 * Implementation of the interfaces IAppKeyManager and IPersistenceManagerListener.
 * 
 * @author schuessler
 *
 */
public final class AppKeyManagerImpl implements IAppKeyManager, IPersistenceManagerListener
{
    private IPersistenceManager dbManager = null;
    private static AppKeyManagerImpl instance = null;
    private static final String ALGORITHM = "HmacSHA512";

    private AppKeyManagerImpl()
    {
        getDbManager();
    }

    /**
     * Because this is a dummy key manager, it returns a dummy symmetric key for app authentication, which may be not
     * created in the most secure way.
     * 
     * @param appName - The app's name for which the key is created
     * 
     * @return The symmetric key for app authentication
     */
    @Override
    public synchronized SecretKey createRequestValidationKey(final String appName) throws CreateAuthKeyException
    {
        SecretKey key = null;

        try
        {
            key = KeyGenerator.getInstance(ALGORITHM).generateKey();
            getDbManager();
            if(!dbManager.saveAppAuthKey(key, appName))
                throw new CreateAuthKeyException("Fehler während der Schlüsselerzeugung!", null, LogLevel.PROBLEM);
        }
        catch(final NoSuchAlgorithmException nsae)
        {
            throw new CreateAuthKeyException("Fehler während der Schlüsselerzeugung!", nsae, LogLevel.PROBLEM);
        }

        return key;
    }

    /**
     * Returns the symmetric key for app authentication
     * 
     * @param appName - The app name for the requested key
     * 
     * @return The symmetric key for app authentication or NULL if no key was found for the passed name
     */
    @Override
    public SecretKey getKeyForApp(final String appName)
    {
        getDbManager();

        return dbManager.getAppAuthKey(appName);
    }

    /**
     * Returns an array with the IDs of all apps, which are registered at the Sec2-middleware.
     * 
     * @return An array with the IDs of all apps, which are registered at the Sec2-middleware.
     */
    @Override
    public String[] getRegisteredAppIds()
    {
        getDbManager();

        return dbManager.getRegisteredAppIds();
    }

    public static AppKeyManagerImpl getInstance()
    {
        if(instance == null)
        {
            instance = new AppKeyManagerImpl();
            PersistenceManagerContainer.addListener(instance);
        }

        return instance;
    }

    @Override
    public boolean deleteKeyForApp(final String appName)
    {
        getDbManager();

        return dbManager.deleteAppAuthKey(appName);
    }

    @Override
    public void onManagerChanged()
    {
        dbManager = PersistenceManagerContainer.getPersistenceManager();
    }

    private void getDbManager()
    {
        if(dbManager == null)
        {
            dbManager = PersistenceManagerContainer.getPersistenceManager();
            if(dbManager == null)
                throw new NullPointerException("Es konnte kein Persistence-Manager gefunden werden. Referenz ist NULL!");
        }
    }
}
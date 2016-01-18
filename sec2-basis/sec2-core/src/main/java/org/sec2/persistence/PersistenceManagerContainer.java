package org.sec2.persistence;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class provides a possibility to handle data persistence in a way regardless
 * whether the Sec2-middleware runs on desktop systems or Android systems. Through the
 * method "setPersistenceManager" a system specific implementation of the interface
 * "IPersistenceManager" can be set within the system specific moduls like "sec2-android"
 * or "sec2-desktop". Classes within the core modules, which need access to the
 * persistence manager can get access to the system specific implementation through the
 * method "getPersistenceManager".
 * This class provides also a listener mechanism, so that registered listener are notified
 * if a new persistence manager is set to this class though the method "setPersistenceManager()".
 * 
 * @author nike
 *
 */
public class PersistenceManagerContainer
{
    private static IPersistenceManager manager = null;
    private static ArrayList<IPersistenceManagerListener> listeners = new ArrayList<IPersistenceManagerListener>();

    /**
     * Sets a system specific implementation of the "IPersistenceManager" interface and
     * notifies all registered listener that a new manager was set. This method throws a
     * "NumberFormatException" if the passed reference is NULL.
     * 
     * @param manager - The manager to be set. Throws a "NumberFormatException" if NULL.
     */
    public static synchronized void setPersistenceManager(IPersistenceManager manager)
    {
        if(manager == null) throw new NullPointerException("The passed manager must not be NULL!");
        PersistenceManagerContainer.manager = manager;
        notifyListener();
    }

    /**
     * This method returns the instance of "IPersistenceManager", which was set before through
     * the method "setPersistenceManager()". The method may return NULL, if no instance was set
     * before.
     * A listener may be passed to this method. The listener will be registered and then be
     * notified in future if a new manager was set through the method "setPersistenceManager()".
     * 
     * @param listener - The listener to be registered
     * 
     * @return The manager
     */
    public static synchronized IPersistenceManager getPersistenceManager(IPersistenceManagerListener listener)
    {
        if(listener != null) listeners.add(listener);

        return manager;
    }

    /**
     * This method returns the instance of "IPersistenceManager", which was set before through
     * the method "setPersistenceManager()". The method may return NULL, if no instance was set
     * before.
     * 
     * @return The manager
     */
    public static synchronized IPersistenceManager getPersistenceManager()
    {
        return manager;
    }

    /**
     * This method registers the passed listener so that it will be notified in future if a
     * new manager was set through the method "setPersistenceManager()".
     * 
     * @param listener - The listener to be registered
     */
    public static void addListener(IPersistenceManagerListener listener)
    {
        if(listener != null) listeners.add(listener);
    }

    /**
     * This method removes the passed listener from the list of registered listeners.
     * 
     * @param listener - The listener to be removed
     * @return TRUE if the list of registered listeners contained the passed listener
     */
    public static boolean removeListener(IPersistenceManagerListener listener)
    {
        boolean success = false;

        if(listener != null) success = listeners.remove(listener);

        return success;
    }

    private static void notifyListener()
    {
        Iterator<IPersistenceManagerListener> iter = listeners.iterator();

        while(iter.hasNext()) iter.next().onManagerChanged();
    }
}

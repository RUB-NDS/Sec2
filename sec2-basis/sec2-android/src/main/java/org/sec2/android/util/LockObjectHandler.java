package org.sec2.android.util;

import java.util.Hashtable;

/**
 * This class handles lock objects, so that threads can synchronise themselves
 * against the same lock object, if direct exchange of the lock object between
 * those threads is not possible (like between an Android service and an
 * activity). The class implements the singleton-pattern.
 *
 * @author nike
 */
public class LockObjectHandler
{
    private static Hashtable<String, Object> lockObjects =
            new Hashtable<String, Object>();

    //Privater Constructor wegen dem Singleton-Pattern
    private LockObjectHandler(){}

    /**
     * This method adds a lock object to the handler. Both id and lock object
     * must not be NULL, otherwise a NullPointerException is thrown.
     *
     * @param id - The ID of the lock object
     * @param lockObject - The lock object
     *
     * @return TRUE, if the passed lock object could succesfully be added to
     *  the handler, otherwise FALSE
     */
    public static synchronized boolean setLockObject(final String id,
            final Object lockObject)
    {
        //Falls die ID schon existent ist, wird das neue lockObject nicht
        //gespeichert
        if (!lockObjects.containsKey(id))
        {
            lockObjects.put(id, lockObject);
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns the lock object which was stored under the passed ID. In case
     * the passed ID is NULL, a NullPointerException is thrown. If no lock
     * object can be found for the passed ID, NULL is returned.
     *
     * @param id - The ID of the lock object, which is to be returned
     *
     * @return The lock object for the passed ID or NULL, if no lock object for
     *  the passed ID was found
     */
    public static Object getLockObject(final String id)
    {
        return lockObjects.get(id);
    }

    /**
     * Removes the lock object, which was stored under the passed ID, from the
     * handler. In case the passed ID is NULL, a NullPointerException is
     * thrown. If no lock object can be found for the passed ID, NULL is
     * returned and no lock object is removed form the handler. This method
     * just removes the lock object from the handler, but the object may still
     * be used for synchronisation.
     *
     * @param id - The ID of the lock object, which is to be removed from and
     *  the handler
     *
     * @return The lock object, which was removed from the handler
     */
    public static synchronized Object removeLockObject(final String id)
    {
        return lockObjects.remove(id);
    }
}

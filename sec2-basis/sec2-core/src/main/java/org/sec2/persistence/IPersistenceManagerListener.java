package org.sec2.persistence;

/**
 * Objects, which want to be notified, if a new manager was set to the
 * "PersistenceManagerContainer" must implement this interface.
 * 
 * @author nike
 *
 */
public interface IPersistenceManagerListener
{
    /**
     * This method is called, if a new manager was set to the
     * "PersistenceManagerContainer".
     */
    public void onManagerChanged();
}

package org.sec2.mwserver.core.util;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.sec2.persistence.IPersistenceManager;
import org.sec2.persistence.PersistenceManagerContainer;

/**
 * This class starts and ends a task that deletes old nonces which were
 * persisted on middleware side at a fixed rate. The class follows the
 * singleton pattern.
 *
 * @author nike
 */
public class NonceCleaner
{
    private static final long INTERVAL = 10;
    private static boolean isStarted = false;
    private static ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor();

    //Prevent that objects of this class are created
    private NonceCleaner(){};

    /**
     * Starts the task. Returns TRUE if the task could successfully be
     * started. If FALSE is returned, then the task was already running.
     *
     * @return TRUE if the task could successfully be started. FALSE if the
     *  task was already running.
     */
    public static synchronized boolean startNonceCleanerTask()
    {
        if (!isStarted)
        {
            executor.scheduleAtFixedRate(new NonceCleanerTask(), 0, INTERVAL,
                    TimeUnit.MINUTES);
            isStarted = true;

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Stops the task. Initiates an orderly shutdown in which previously
     * submitted tasks are executed, but no new tasks will be accepted. Returns
     * TRUE if the task could successfully be stopped. If FALSE is returned,
     * then the task was already stopped.
     *
     * @return TRUE if the task could successfully be stopped. FALSE if the
     *  task was already stopped.
     */
    public static synchronized boolean stopNonceCleanerTask()
    {
        if (isStarted)
        {
            executor.shutdown();
            isStarted = false;

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns whether the task was started or not.
     *
     * @return TRUE if task was started, FALSE otherwise
     */
    public static boolean isStarted()
    {
        return isStarted;
    }

    private static final class NonceCleanerTask implements Runnable
    {
        private static final long MAX_AGE = 600000; //10 min.

        @Override
        public void run()
        {
            IPersistenceManager dbManager =
                    PersistenceManagerContainer.getPersistenceManager();
            Date now = new Date();

            if (dbManager != null)
            {
                dbManager.deleteOldNonceInDb(now.getTime() - MAX_AGE);
            }
        }
    }
}

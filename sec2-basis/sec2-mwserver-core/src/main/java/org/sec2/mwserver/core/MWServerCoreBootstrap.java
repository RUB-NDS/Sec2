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
package org.sec2.mwserver.core;

import org.sec2.exceptions.BootstrapException;
import org.sec2.managers.ManagerProvider;

/**
 * This class can be used to bootstrap the Sec2 MWServer Core module with the
 * default configurations.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 1, 2013
 */
public class MWServerCoreBootstrap {
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

    /** Constructor. */
    protected MWServerCoreBootstrap() { }

    /**
     * Initializes the module loading default configurations.
     *
     * @throws BootstrapException
     *      thrown if there is a problem initializing the module
     */
    public static synchronized void bootstrap() throws BootstrapException {
        if (!isInitialized()) {
                ManagerProvider.getInstance().setAppKeyManager(
                        AppKeyManagerImpl.getInstance());
            initialized = true;
        }
    }
}

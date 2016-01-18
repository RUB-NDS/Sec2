/*
 * Copyright 2012 Ruhr-University Bochum, Chair for Network and Data Security
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://nds.rub.de/research/projects/sec2/
 */
package org.sec2.mwserver.core.rest;

import junit.extensions.TestSetup;
import junit.framework.Test;
import org.sec2.managers.ManagerProvider;
import org.sec2.mwserver.core.AppKeyManagerImpl;
import org.sec2.mwserver.core.TestDbManager;
import org.sec2.mwserver.core.managerDummies.DummyGroupManagerImpl;
import org.sec2.mwserver.core.managerDummies.DummyUserManagerImpl;
import org.sec2.persistence.PersistenceManagerContainer;

/**
 * This testsetup is used to set the security provider on position 1 before
 * testing.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 28, 2012
 */
public class ManagerProvidingTestSetup extends TestSetup {

    /**
     * Create the test setup.
     *
     * @param test the test case
     */
    public ManagerProvidingTestSetup(final Test test) {
        super(test);
    }

    /**
     * Set the security provider on position 1 before testing.
     */
    @Override
    public void setUp() throws Exception {
        ManagerProvider.getInstance().setUserManager(
                DummyUserManagerImpl.getInstance());
        ManagerProvider.getInstance().setGroupManager(
                DummyGroupManagerImpl.getInstance());
        PersistenceManagerContainer.setPersistenceManager(new TestDbManager());
        ManagerProvider.getInstance().setAppKeyManager(
                AppKeyManagerImpl.getInstance());
    }

    /**
     * Remove the security provider after testing.
     */
    @Override
    public void tearDown() {
        ManagerProvider.getInstance().setGroupManager(null);
        ManagerProvider.getInstance().setUserManager(null);
        ManagerProvider.getInstance().setAppKeyManager(null);
    }
}

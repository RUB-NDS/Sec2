package org.sec2.backend.test;

import junit.framework.TestCase;

public abstract class DatabaseModifyingTestCase extends TestCase {

    private boolean restoreDatabaseAtTearDown;

    public void setRestoreDatabaseAtTearDown(boolean restoreDatabaseAtTearDown) {
        this.restoreDatabaseAtTearDown = restoreDatabaseAtTearDown;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.restoreDatabase();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (restoreDatabaseAtTearDown) {
            TestUtil.restoreDatabase();
        }
    }
}

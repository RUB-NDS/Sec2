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
package org.sec2.rest.backend;

import org.sec2.backend.IUserManagement;
import org.sec2.backend.impl.AbstractUserManagementFactory;

/**
 * Centralized access to the keyserver's backend.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * July 29, 2013
 */
public final class BackendHolder {

    /**
     * Utility class, no objects allowed.
     */
    private BackendHolder() { }

    /**
     * @return the keyserver's backend
     */
    public static IUserManagement getBackend() {
        return NestedBackendHolder.INSTANCE;
    }

    /**
     * Nested class holding Backend Singleton instance.
     */
    private static class NestedBackendHolder {
        /**
         * The singleton instance.
         */
        private static final IUserManagement INSTANCE =
                AbstractUserManagementFactory.createDefault();
    }
}

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
package org.sec2.managers;

/**
 * Centralized access to the middleware managers.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 1, 2013
 */
public final class ManagerProvider {

    /**
     * The group manager.
     */
    private IGroupManager groupManager;
    /**
     * The user manager.
     */
    private IUserManager userManager;
    /**
     * The user registration manager.
     */
    private IUserRegistrationManager userRegistrationManager;
    /**
     * The app key manager.
     */
    private IAppKeyManager appKeyManager;

    /**
     * @return the groupManager
     */
    public IGroupManager getGroupManager() {
        return groupManager;
    }

    /**
     * @param newGroupManager the groupManager to set
     */
    public void setGroupManager(final IGroupManager newGroupManager) {
        this.groupManager = newGroupManager;
    }

    /**
     * @return the userManager
     */
    public IUserManager getUserManager() {
        return userManager;
    }

    /**
     * @param newUserManager the userManager to set
     */
    public void setUserManager(final IUserManager newUserManager) {
        this.userManager = newUserManager;
    }

    /**
     * @return the appKeyManager
     */
    public IAppKeyManager getAppKeyManager() {
        return appKeyManager;
    }

    /**
     * @param newAppKeyManager the appKeyManager to set
     */
    public void setAppKeyManager(final IAppKeyManager newAppKeyManager) {
        this.appKeyManager = newAppKeyManager;
    }

    /**
     * Utility class, no objects allowed.
     */
    private ManagerProvider() {
    }

    /**
     * @return the ManagerProvider Singleton instance
     */
    public static ManagerProvider getInstance() {
        return ManagerProviderHolder.INSTANCE;
    }

    /**
     * @return the userRegistrationManager
     */
    public IUserRegistrationManager getUserRegistrationManager() {
        return userRegistrationManager;
    }

    /**
     * @param userRegistrationManager the userRegistrationManager to set
     */
    public void setUserRegistrationManager(
            final IUserRegistrationManager userRegistrationManager) {
        this.userRegistrationManager = userRegistrationManager;
    }

    /**
     * Nested class holding ManagerProvider Singleton instance.
     */
    private static class ManagerProviderHolder {

        /**
         * The singleton instance.
         */
        private static final ManagerProvider INSTANCE = new ManagerProvider();
    }
}

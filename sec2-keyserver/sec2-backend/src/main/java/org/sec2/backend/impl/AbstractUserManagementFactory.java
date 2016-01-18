package org.sec2.backend.impl;

import java.util.Properties;

/**
 * AbstractUserManagementFactory is a convenient way to create new
 * UserManagement instances.
 * 
 * @author Utimaco Safeware
 * 
 */
public abstract class AbstractUserManagementFactory {

    /**
     * Creates a new UserManagement instance from the default configuration
     * path.
     * 
     * @see AbstractConfigurationFactory.createDefault()
     * @return A new UserManagement instance initialized with the default
     *         configuration path
     */
    public static UserManagement createDefault() {
        return createFromConfiguration(ConfigurationFactory.DEFAULT_CONFIGURATION_PATH);
    }

    /**
     * Create a new UserManagement instance from the configuration at path.
     * 
     * @param path
     *            Path to the configuration
     * @see AbstractConfigurationFactory.create(String)
     * @return A new UserManagement instance initialized with the configuration
     *         at path
     */
    public static UserManagement createFromConfiguration(String path) {
        return new UserManagement(ConfigurationFactory.create(path));
    }

    /**
     * Create a new UserManagement instance from the configuration.
     * 
     * @param configuration
     *            The configuration used to create a new UserManagement instance
     * @return A new UserManagement instance initialized with the configuration
     */
    public static UserManagement createFromConfiguration(
            Properties configuration) {
        return new UserManagement(configuration);
    }
}

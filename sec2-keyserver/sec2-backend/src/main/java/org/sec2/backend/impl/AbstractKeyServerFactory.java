package org.sec2.backend.impl;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The AbstractKeyServerFactory is used to create new {@link KeyServer} objects.
 * 
 * @author Utimaco Safeware
 */
public abstract class AbstractKeyServerFactory {

    /**
     * The logger.
     */
    private static final Logger LOGGER = Logger
            .getLogger(AbstractKeyServerFactory.class.getName());

    /**
     * Creates a new {@link KeyServer} object with the configuration located at 
     *  {@link ConfigurationFactory#DEFAULT_CONFIGURATION_PATH}
     * 
     * @return A new {@link KeyServer} object
     * @see    ConfigurationFactory#DEFAULT_CONFIGURATION_PATH
     */
    public KeyServer createDefault() {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                AbstractKeyServerFactory.class.getName(),
                "createDefault"
            );
        }
        KeyServer keyServer = createFromPath(ConfigurationFactory.DEFAULT_CONFIGURATION_PATH);
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(
                AbstractKeyServerFactory.class.getName(),
                "createDefault",
                keyServer
            );
        }
        return keyServer;
    }

    /**
     * Creates a new {@link KeyServer} object with the configuration located at {@code path}.
     * 
     * @param path Path to the configuration property file
     * @return     A new {@link KeyServer} object
     */
    public KeyServer createFromPath(String path) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                AbstractKeyServerFactory.class.getName(),
                "createFromConfiguration", 
                path
            );
        }
        Properties properties = ConfigurationFactory.create(path);
        KeyServer keyServer = new KeyServer(new DatabaseServer(properties), properties);
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(
                AbstractKeyServerFactory.class.getName(),
                "createDefault",
                keyServer
            );
        }
        return keyServer;
    }
}

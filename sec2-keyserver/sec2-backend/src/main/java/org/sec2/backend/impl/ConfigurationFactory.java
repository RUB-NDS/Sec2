package org.sec2.backend.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory used to create/load configuration files.
 * 
 * @author Utimaco Safeware
 */
public class ConfigurationFactory {
    /**
     * The Logger.
     */
    private static final Logger LOGGER = Logger
            .getLogger(ConfigurationFactory.class.getName());
    
    /**
     * Deny instantiation.
     */
    private ConfigurationFactory(){}
    
    /**
     * The default configuration path.
     */
    public static final String DEFAULT_CONFIGURATION_PATH = "/configuration.xml";

    /**
     * Creates a configuration from the default path.
     * 
     * @return A Properties object that contains the configuration loaded from the default path.
     */
    public static Properties createDefault() {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.entering(ConfigurationFactory.class.getName(),
                    "createDefault");
        }
        Properties configuration = create(DEFAULT_CONFIGURATION_PATH);
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.exiting(ConfigurationFactory.class.getName(),
                    "createDefault", configuration);
        }
        return configuration;
    }

    /**
     * Creates a configuration from the file located at <param>path</param>.
     * 
     * @param path
     *            Path to the configuration
     * @return A Properties object that contains the configuration.
     */
    public static Properties create(String path) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(ConfigurationFactory.class.getName(),
                    "create", path);
        }
        Properties properties = null;
        try {
            if (LOGGER.isLoggable(Level.FINE)) {
                File f = new File(path);
                LOGGER.fine("Configuration path is: " + f.getAbsolutePath());
            }
            InputStream fis = ConfigurationFactory.class
                    .getResourceAsStream(path);
            properties = new Properties();
            properties.loadFromXML(fis);
            fis.close();
        }
        catch (FileNotFoundException e) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("ERROR: Configuration '" + path + "' not found!");
            }
        }
        catch (InvalidPropertiesFormatException e) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("ERROR: Configuration '" + path
                        + "' contains invalid properties!");
            }
        }
        catch (IOException e) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("ERROR: Configuration '" + path
                        + "' can't be opened!");
            }
        }
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(ConfigurationFactory.class.getName(),
                    "create", properties);
        }
        return properties;
    }
}

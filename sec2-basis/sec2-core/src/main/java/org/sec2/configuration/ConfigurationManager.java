/*
 * Copyright 2011 Sec2 Consortium
 * 
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit written
 * permission is prohibited.
 * For details on "Sec2" and its contributors visit
 * 
 *        http://www.sec2.org
 */
package org.sec2.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import org.sec2.configuration.exceptions.ExConfigurationInitializationFailure;
import org.sec2.configuration.exceptions.ExNoSuchProperty;
import org.sec2.configuration.exceptions.ExRestrictedPropertyAccess;

/**
 * Configuration manager.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 * Jun 10, 2013
 */
public final class ConfigurationManager {

    private static final String CONFIG_FILE = "sec2config.xml";
    private static final String CONFIG_PACKAGE_SCOPE = "org.sec2";
    private static ConfigurationManager instance;
    private Properties properties;

    /**
     * Singleton constructor - use getInstance instead!
     *
     * @param config Pre-initialized properties object.
     */
    private ConfigurationManager(final Properties config) {
        this.properties = config;

    }

    /**
     * Default constructor implementing finalizer protection pattern.
     *
     * @throws ExConfigurationInitializationFailure If the configuration could
     * not be initialized.
     */
    private ConfigurationManager() 
            throws ExConfigurationInitializationFailure {
        this(loadConfig());
    }

    /**
     * Loads the configuration file and initializes a properties object.
     *
     * @return Pre-initialized properties object.
     * @throws ExConfigurationInitializationFailure If the configuration could
     * not be initialized.
     */
    private static Properties loadConfig() throws
            ExConfigurationInitializationFailure {
        Properties result = new Properties();
        // try to load configuration
        InputStream resource = null;
        try {
            resource = ConfigurationManager.class.getResourceAsStream(
                    CONFIG_FILE);
            result.loadFromXML(resource);
        } catch (NullPointerException ex) {
            throw new ExConfigurationInitializationFailure();
        } catch (IOException ex) {
            throw new ExConfigurationInitializationFailure();
        } finally {
            if (resource != null) {
                try {
                    resource.close();
                } catch (IOException e) {
                    throw new ExConfigurationInitializationFailure();
                }
            }
        }

        return result;
    }

    /**
     * Obtain an instance of the manager.
     *
     * @return Instance of this class.
     * @throws ExConfigurationInitializationFailure If the configuration could
     * not be initialized.
     */
    public static ConfigurationManager getInstance()
            throws ExConfigurationInitializationFailure {
        if (instance == null) {
            instance = new ConfigurationManager();
        }

        return instance;
    }

    /**
     * Get a configuration property with the specified name.
     *
     * @param propertyName Name of the desired property.
     * @return Value of the specified property (if available).
     * @throws ExNoSuchProperty If the specified property is not available.
     * @throws ExRestrictedPropertyAccess If the specified property is not part
     * of the scope of this manager.
     */
    public String getConfigurationProperty(final String propertyName)
            throws ExNoSuchProperty, ExRestrictedPropertyAccess {
        String result = null;
        if (propertyName.startsWith(CONFIG_PACKAGE_SCOPE)) {
            Object hashTableValue = this.properties.get(propertyName);
            if (hashTableValue instanceof String) {
                result = (String) hashTableValue;
            } else {
                throw new ExNoSuchProperty();
            }
        } else {
            throw new ExRestrictedPropertyAccess();
        }

        return result;
    }

    public void setConfigurationProperty_INTERNAL(final String propertyName, final String propertyValue) {
        this.properties.setProperty(propertyName, propertyValue);
    }
    /**
     * Experimental routine for obtaining all registered configuration
     * properties.
     *
     * @return All registered configuration properties.
     */
    private List<String> getConfigurationProperties() {
        ArrayList<String> result;
        result = new ArrayList<String>(this.properties.size());

        Enumeration<Object> keys = this.properties.keys();
        while (keys.hasMoreElements()) {
            result.add((String) keys.nextElement());
        }

        return result;
    }
}

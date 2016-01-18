package org.sec2.configuration;

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
import junit.framework.TestCase;
import org.sec2.configuration.exceptions.ExConfigurationInitializationFailure;
import org.sec2.configuration.exceptions.ExNoSuchProperty;
import org.sec2.configuration.exceptions.ExRestrictedPropertyAccess;

/**
 * ConfigurationManager functionality test.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 * Jun 10, 2013
 */
public class ConfigurationManagerTests extends TestCase {

    private static final String VERSION_PROPERTY = "org.sec2.version";

    public void testCheckVersionProperty() throws ExNoSuchProperty,
            ExConfigurationInitializationFailure, ExRestrictedPropertyAccess {
        ConfigurationManager manager = ConfigurationManager.getInstance();
        String value = manager.getConfigurationProperty(VERSION_PROPERTY);

        assertNotNull(value);
        assertFalse(value.isEmpty());

        System.out.println("Value: " + value);
    }
}

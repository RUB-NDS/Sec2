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
package org.sec2.saml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Provides support to read an xml based config file.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 16, 2012
 */
public abstract class AbstractPropertyReadingConfig {

    /**
     * Reads an XML file and builds a properties object from it.
     * @param filename the file to read
     * @return the properties object
     */
    protected static Properties getPropertiesFromXML(final String filename) {
        Properties properties = new Properties();
        try {
            InputStream is = SAMLBaseConfig.class.getResourceAsStream(
                    "/" + filename);
            try {
                properties.loadFromXML(is);
            } finally {
                is.close();
            }
        } catch (IOException e) {
            throw new Error(e); // to have no config is fatal
        }
        return properties;
    }
}

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
package org.sec2.saml.client;

import java.util.Properties;

/**
 * Constants used in several tests.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * December 17, 2012
 */
public final class SAMLClientTestConfig extends SAMLClientConfig {

    /**
     * No instances allowed, utility class only.
     */
    private SAMLClientTestConfig() { }

    /**
     * Used to access the config file.
     */
    private static final Properties PROPERTIES = getPropertiesFromXML(
            "saml-client-config.xml");

    /**
     * The delay in seconds that is allowed between creation of a timestamp and
     * its test.
     */
    public static final int ALLOWED_TIMESTAMP_DELAY = Integer.parseInt(
            PROPERTIES.getProperty("saml.client.test.timestampDelay"));

    /**
     * The test protocol used by the test protocol handler.
     */
    public static final String TEST_PROTOCOL = PROPERTIES.getProperty(
            "saml.client.test.protocol");
}

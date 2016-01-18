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
package org.sec2.frontend;

import java.util.Properties;
import org.sec2.saml.SAMLBaseConfig;

/**
 * Contains often used constants.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * December 18, 2012
 */
public final class KeyserverFrontendConfig extends SAMLBaseConfig {

    /**
     * No instances allowed, utility class only.
     */
    private KeyserverFrontendConfig() { }

    /** Default error message. */
    public static final String DEFAULT_ERROR_MSG = "An error occurred while "
            + "processing your request. Please contact your helpdesk or user "
            + "ID office for assistance.";

    /**
     * The default error message if a request was denied due to
     * security considerations.
     */
    public static final String DEFAULT_SECURITY_MSG =
            "Processing aborted due to security considerations.";

    /**
     * Used to access the config file.
     */
    private static final Properties PROPERTIES = getPropertiesFromXML(
            "keyserver-frontend-config.xml");

    /**
     * Seconds a request's timestamp is allowed to differ from local clock.
     */
    public static final int ALLOWED_TIMESTAMP_OFFSET = Integer.parseInt(
            PROPERTIES.getProperty("keyserver.frontend.timestampOffset"));

    /**
     * Configuration of the recentIDs cache.
     */
    public static final String RECENT_IDS_CACHE_CONFIG = PROPERTIES.getProperty(
            "keyserver.frontend.recentIDsCacheConfig");

    /**
     * Seconds a response's assertion is declared to be valid.
     */
    public static final int ASSERTION_VALIDITY = Integer.parseInt(
            PROPERTIES.getProperty("keyserver.frontend.assertionValidity"));

    /**
     * Maximum number of users' certificates kept in RAM.
     */
    public static final String KEY_CACHE_CONFIG = PROPERTIES.getProperty(
            "keyserver.frontend.keyCacheConfig");

    /**
     * MDC key for the current user entry.
     */
    public static final String MDC_KEY_USER = "sec2.user";

    /**
     * MDC key for the current request entry.
     */
    public static final String MDC_KEY_REQUEST = "sec2.request";
}

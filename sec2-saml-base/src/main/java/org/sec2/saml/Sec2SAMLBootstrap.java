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

import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.security.BasicSecurityConfiguration;

/**
 * This class can be used to bootstrap the Sec2 SAML module with the default
 * configurations.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 20, 2012
 */
public class Sec2SAMLBootstrap extends DefaultBootstrap {

    /**
     * Tells if the library has been bootstrapped.
     */
    private static boolean initialized = false;

    /**
     * @return the initialized status
     */
    public static synchronized boolean isInitialized() {
        return initialized;
    }

    /**
     * Constructor.
     */
    protected Sec2SAMLBootstrap() {
    }
    /**
     * List of default XMLTooling configuration files.
     */
    private static String[] configFiles = {
        "/sec2saml-config.xml",};

    /**
     * Initializes the Sec2 SAML module and the OpenSAML library, loading
     * default configurations.
     *
     * @throws ConfigurationException thrown if there is a problem initializing
     * the OpenSAML library
     */
    public static synchronized void bootstrap() throws ConfigurationException {
        if (!isInitialized()) {
            initializeXMLSecurity();
            initializeXMLTooling();
            //initializeArtifactBuilderFactories();
            initializeGlobalSecurityConfiguration();
            initializeParserPool();
            //initializeESAPI();
            Sec2SAMLBootstrap.initializeXMLTooling(configFiles);
            // set digest method
            BasicSecurityConfiguration config = (BasicSecurityConfiguration) Configuration.getGlobalSecurityConfiguration();
            config.setSignatureReferenceDigestMethod(
                    SAMLBaseConfig.XML_SIGNATURE_DIGEST_METHOD_NS);
            Configuration.setGlobalSecurityConfiguration(config);
            initialized = true;
        }
    }
}

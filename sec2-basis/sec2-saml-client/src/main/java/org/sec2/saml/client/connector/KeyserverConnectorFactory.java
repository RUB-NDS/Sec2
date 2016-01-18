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
package org.sec2.saml.client.connector;

import org.sec2.saml.client.SAMLClientConfig;
import org.sec2.saml.exceptions.SAMLEngineException;

/**
 * Factory class that simplifies access to a KeyserverConnector.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 01, 2012
 */
public final class KeyserverConnectorFactory {

    /**
     * The IKeyserverConnector instance.
     */
    private static IKeyserverConnector instance;

    /**
     * @return the KeyserverConnector
     * @throws SAMLEngineException if the registered user cannot be
     *          determined or if the root certificate cannot be set
     */
    public static synchronized IKeyserverConnector getKeyserverConnector()
            throws SAMLEngineException {
        if (instance == null) {
            instance = new KeyserverConnectorImpl(
                    SAMLClientConfig.SEC2_KEYSERVER_URL);
        }
        return instance;
    }

    /**
     * No instances allowed, utility class only.
     */
    private KeyserverConnectorFactory() {
    }
}

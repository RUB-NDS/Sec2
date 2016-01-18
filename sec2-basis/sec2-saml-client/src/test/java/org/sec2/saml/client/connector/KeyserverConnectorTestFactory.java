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

import org.sec2.saml.exceptions.SAMLEngineException;

/**
 * Factory class that injects a special URL for testing purposes.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 06, 2012
 */
public final class KeyserverConnectorTestFactory {

    /**
     * A special URL for testing. Only the scheme matters, the rest is
     * irrelevant.
     */
    private static final String LOCAL_URL = "sec2test://sec2.org/keyserver";

    /**
     * The IKeyserverConnector instance.
     */
    private static KeyserverConnectorImpl instance;

    /**
     * @return the KeyserverConnector
     * @param urlPart Appendix of the url to manipulate the mock keyserver's
     *          reaction
     * @throws SAMLEngineException if the registered user cannot be
     *          determined or if the root certificate cannot be set
     */
    public static synchronized IKeyserverConnector getKeyserverConnector(
            final String urlPart)
            throws SAMLEngineException {
        if (instance == null || !instance.getKeyserverURL().contains(urlPart)) {
            instance = new KeyserverConnectorImpl(LOCAL_URL + "#" + urlPart);
        }
        return instance;
    }

    /**
     * No instances allowed, utility class only.
     */
    private KeyserverConnectorTestFactory() {
    }
}

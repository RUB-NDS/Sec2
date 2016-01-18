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
package org.sec2.saml.client.engine;

import java.security.Security;
import org.sec2.exceptions.EntityUnknownException;
import org.sec2.securityprovider.mobileclient.MobileClientProvider;

/**
 * Insert description here.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 05, 2012
 */
//FIXME: Remove class for final version
public final class SecurityProviderConnectorFactory {

    /**
     * ...
     */
    private SecurityProviderConnectorFactory() { }

    /**
     * ...
     * @return ...
     * @throws EntityUnknownException ...
     */
    public static ISecurityProviderConnector getSecurityProviderConnector()
            throws EntityUnknownException {
        if (Security.getProviders()[0].getName().
                    equals(MobileClientProvider.PROVIDER_NAME)) {
            //TODO: real connector, then remove the whole thing ;)
            return SecurityProviderConnectorDummy.getInstance();
        } else {
            return SecurityProviderConnectorDummy.getInstance();
        }
    }
}

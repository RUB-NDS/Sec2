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
package org.sec2.saml.client.connector.protocolhandler;

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import junit.extensions.TestSetup;
import junit.framework.Test;
import org.sec2.saml.client.SAMLClientTestConfig;

/**
 * This testsetup is used to register a special testing protocol handler
 * before testing.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 06, 2012
 */
public class ProtocolHandlerTestSetup extends TestSetup {

    /**
     * Create the test setup.
     *
     * @param test the test case
     */
    public ProtocolHandlerTestSetup(final Test test) {
        super(test);
    }

    /**
     * Register the special testing protocol handler.
     */
    @Override
    public void setUp() {
        URL.setURLStreamHandlerFactory(new Sec2URLStreamHandlerFactory());
    }

    /**
     * Factory that registers the sec2test-protocol.
     */
    static class Sec2URLStreamHandlerFactory
                    implements URLStreamHandlerFactory {
        /**
         * @param protocol The protocol to register
         * @return A handler if the protocol matches or null if not
         */
        @Override
        public URLStreamHandler createURLStreamHandler(final String protocol) {
            if (protocol.equalsIgnoreCase(SAMLClientTestConfig.TEST_PROTOCOL)) {
                return new Handler();
            } else {
                return null;
            }
        }
    }
}

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
import junit.extensions.TestSetup;
import junit.framework.Test;
import org.sec2.securityprovider.exceptions.IllegalPostInstantinationModificationException;
import org.sec2.securityprovider.mobileclient.MobileClientProvider;
import org.sec2.securityprovider.serviceparameter.TokenType;
import org.sec2.token.TokenConstants;
import org.spongycastle.jce.provider.BouncyCastleProvider;

/**
 * This testsetup is used to set the security provider on position 1
 * before testing.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 28, 2012
 */
public class SecurityProviderTestSetup extends TestSetup {

    /**
     * Create the test setup.
     *
     * @param test the test case
     */
    public SecurityProviderTestSetup(final Test test) {
        super(test);
    }

    /**
     * Set the security provider on position 1 before testing.
     */
    @Override
    public void setUp() throws IllegalPostInstantinationModificationException {
        try {
            Security.insertProviderAt(MobileClientProvider.getInstance(), 1);
            Security.addProvider(new BouncyCastleProvider()); //for AES-GCM
        } catch (IllegalStateException e) {
            MobileClientProvider.setType(TokenType.SOFTWARE_TEST_TOKEN_USER_1);
            MobileClientProvider.getInstance(TokenConstants.DEFAULT_PIN);
            setUp();
        }
    }

    /**
     * Remove the security provider after testing.
     */
    @Override
    public void tearDown() {
        Security.removeProvider(MobileClientProvider.PROVIDER_NAME);
    }
}

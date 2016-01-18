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
package org.sec2.securityprovider.testSuites;

import org.sec2.securityprovider.mobileclient.SecretKeyFactoryTests;
import org.sec2.securityprovider.mobileclient.KeyStoreTests;
import org.sec2.securityprovider.mobileclient.SignatureTests;
import org.sec2.securityprovider.mobileclient.CipherTests;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.sec2.securityprovider.mobileclient.MobileClientProvider;

/**
 * Test suite for the security provider of the mobile client.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1                MobileClientProvider.forTestDestructInstance();

 *
 * Jun 10, 2011
 */
public class TSMobileClientProviderHardware extends TestCase {

    public TSMobileClientProviderHardware(String testName) {
        super(testName);
    }

    public static Test suite() {
        MobileClientProvider.forTestDestructInstance();

        TestSuite suite = new TestSuite();
    
        System.out.println("+++ Starting Hardware Token JCE-Tests");
        //Tests on Hardware Token
        suite.addTestSuite(SignatureTests.class);   
        suite.addTestSuite(KeyStoreTests.class);    
        suite.addTestSuite(CipherTests.class);
   
        suite.addTestSuite(SecretKeyFactoryTests.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}

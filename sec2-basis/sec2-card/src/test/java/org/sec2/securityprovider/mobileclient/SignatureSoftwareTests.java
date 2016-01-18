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
package org.sec2.securityprovider.mobileclient;

import org.sec2.securityprovider.MobileClientCommons;
import org.sec2.securityprovider.ReferenceCommons;
import org.sec2.securityprovider.TestCommons;
import org.sec2.securityprovider.mobileclient.MobileClientProvider;
import org.sec2.securityprovider.serviceparameter.TokenType;

/**
 * Concrete instantiation of the signature tests.
 * @author  Jan Temme - Jan.Temme@rub.de
 * @version 0.1
 * May 10, 2012
 */
public class SignatureSoftwareTests extends SignatureTestsImpl {

    /**
     * Initialize the test class.
     */
    public SignatureSoftwareTests() {
        super(MobileClientCommons.getInstance(TokenType.SOFTWARE_TOKEN).getProvider(),
                ReferenceCommons.getInstance().getProvider(),
                TestCommons.getSignatureAlgorithms());
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * Copyright 2012 Sec2 Consortium
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit written
 * permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://www.sec2.org
 */
package org.sec2.securityprovider.mobileClient;

import org.sec2.securityprovider.MobileClientCommons;
import org.sec2.securityprovider.TestCommons;

/**
 *
 * @author Jan
 * 09.08.2012
 */
public class SecretKeyFactoryTests extends SecretKeyFactoryTestImpl{

    public SecretKeyFactoryTests() {
        super(MobileClientCommons.getInstance().getProvider(),
                TestCommons.getCipherAlgorithms());
    }
}

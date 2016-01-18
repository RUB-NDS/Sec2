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
package org.sec2.securityprovider;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Commonly used objects for the reference provider.
 * @author  Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 * Jul 25, 2011
 */
public final class ReferenceCommons extends TestCommons {
    /**
     * Singleton reference.
     */
    private static ReferenceCommons selfInstance = null;

    /**
     * Private constructor - utitly classes should remain static.
     */
    private ReferenceCommons() {
            this.setProvider(new BouncyCastleProvider());
    }

    /**
     * Getter for the instance.
     * @return Instance of this class.
     */
    public static ReferenceCommons getInstance() {
        if(selfInstance == null) {
            selfInstance = new ReferenceCommons();
        }

        return selfInstance;
    }

}

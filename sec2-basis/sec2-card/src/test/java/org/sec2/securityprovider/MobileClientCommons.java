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

import org.sec2.securityprovider.exceptions.IllegalPostInstantinationModificationException;
import org.sec2.securityprovider.mobileclient.MobileClientProvider;
import org.sec2.securityprovider.serviceparameter.TokenType;
import org.sec2.token.ReturnCodes;
import org.sec2.token.TokenConstants;

/**
 * Commonly used objects by the mobile client unit tests.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 * Jul 25, 2011
 */
public final class MobileClientCommons extends TestCommons {

    /**
     * Singleton reference.
     */
    private static MobileClientCommons selfInstance = null;
    /**
     * Test PIN for the crypto device.
     */
    byte[] pin = TokenConstants.DEFAULT_PIN;

    /**
     * Private constructor - utitly classes should remain static.
     */
    private MobileClientCommons() {
        this.setProvider(MobileClientProvider.getInstance(pin));
    }

    /**
     * Private constructor - utitly classes should remain static.
     */
    private MobileClientCommons(TokenType type) {
        try {
            MobileClientProvider.setType(type);
        } catch (IllegalPostInstantinationModificationException ex) {
            System.out.println("Coudln't set Tokentype because it"
                    + "is already instantinated.");
        }
        this.setProvider(MobileClientProvider.getInstance(pin));


    }

    /**
     * Getter for the instance.
     *
     * @return Instance of this class.
     */
    public static MobileClientCommons getInstance() {
        if (selfInstance == null) {
            selfInstance = new MobileClientCommons();
        }

        return selfInstance;
    }

    /** Singleton-Way to get access to Mobile Client PRovider.
     *
     * @param type The
     * @return the instance of provider, but creates a new on not-matching
     * token-type
     */
    public static MobileClientCommons getInstance(TokenType type) {

        if (selfInstance == null) {
            selfInstance = new MobileClientCommons(type);

        } else {
            if (type != TokenType.valueOf(selfInstance.getProvider().getProperty(TokenType.TOKEN_TYPE_IDENTIFIER))) {
                MobileClientProvider.forTestDestructInstance();
                selfInstance = new MobileClientCommons(type);

            }



        }

        return selfInstance;

    }
}

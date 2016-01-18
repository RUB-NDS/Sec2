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
package org.sec2.saml.engine.mockups;

import java.security.NoSuchAlgorithmException;
import org.sec2.saml.engine.CipherEngine;
import org.sec2.saml.engine.PreparedElementGenerator;

/**
 * A mockup for a PreparedElementGenerator that simply extends the
 * abstract super class.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * December 05, 2012
 */
public final class MockupPreparedElementGenerator
        extends PreparedElementGenerator {

    /**
     * Constructor.
     * @param cipherEngine Engine that is used for encryption of attributes
     * @throws NoSuchAlgorithmException if the ID-generator cannot be created
     */
    public MockupPreparedElementGenerator(final CipherEngine cipherEngine)
            throws NoSuchAlgorithmException {
        super(cipherEngine);
    }
}

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
package org.sec2.saml.xml.validator;

import org.opensaml.xml.Configuration;
import org.opensaml.xml.validation.ValidatorSuite;
import org.sec2.saml.AbstractXMLTests;

/**
 * Abstract test framework for testing validators.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 17, 2012
 */
public abstract class AbstractSchemaValidatorTests extends AbstractXMLTests {

    /**
     * The validator suite that is tested.
     */
    private ValidatorSuite validator;

    /**
     * Set up: Get validator and prepare prefix and suffix.
     * (Called before every test case method.)
     */
    @Override
    public void setUp() {
        super.setUp();
        validator =
                Configuration.getValidatorSuite("sec2saml-schema-validator");
    }

    /**
     * Tear down: Delete references.
     * (Called after every test case method.)
     */
    @Override
    public void tearDown() {
        super.tearDown();
        validator = null;
    }

    /**
     * @return the validator
     */
    protected ValidatorSuite getValidator() {
        return validator;
    }
}

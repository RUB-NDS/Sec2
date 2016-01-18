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

import javax.xml.namespace.QName;
import org.opensaml.xml.validation.ValidationException;
import org.sec2.saml.XMLProcessingTestHelper;
import org.sec2.saml.xml.EmailAddress;

/**
 * Tests for the validator for the {@link org.sec2.saml.xml.EmailAddress}
 * element.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 08, 2012
 */
public final class EmailAddressSchemaValidatorTests
                    extends AbstractSchemaValidatorTests {
    /** {@inheritDoc }. */
    @Override
    protected QName getElementQName() {
        return EmailAddress.DEFAULT_ELEMENT_NAME;
    }

    /**
     * Tests a correct address.
     */
    public void testValidEmailAddress() {
        this.getStringBuilder().append("test.BLA_123%qq+a1-@DOM.w-.sec2.oRg");
        this.getStringBuilder().append(this.getSuffix());
        EmailAddress emailAddress = XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(), EmailAddress.class);
        try {
            this.getValidator().validate(emailAddress);
        } catch (ValidationException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests an invalid address.
     */
    public void testInvalidAddress() {
        this.getStringBuilder().append("test@sec2");
        this.getStringBuilder().append(this.getSuffix());
        EmailAddress emailAddress = XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(), EmailAddress.class);
        try {
            this.getValidator().validate(emailAddress);
            fail("Should have failed with a ValidationException");
        } catch (ValidationException e) {
            assertNotNull(e);
        }
    }

    /**
     * Tests an empty address.
     */
    public void testEmptyAddress() {
        this.getStringBuilder().append(this.getSuffix());
        EmailAddress emailAddress = XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(), EmailAddress.class);
        try {
            this.getValidator().validate(emailAddress);
            fail("Should have failed with a ValidationException");
        } catch (ValidationException e) {
            assertNotNull(e);
        }
    }
}

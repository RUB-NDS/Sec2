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
import org.sec2.saml.xml.UpdateUser;
import org.sec2.saml.xml.UserRequestType;

/**
 * Tests for the validator for the
 * {@link org.sec2.saml.xml.UserRequestType} element.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 08, 2012
 */
public final class UserRequestTypeSchemaValidatorTests
                    extends AbstractSchemaValidatorTests {
    /** {@inheritDoc }. */
    @Override
    protected QName getElementQName() {
        return UpdateUser.DEFAULT_ELEMENT_NAME;
    }

    /**
     * Tests a correct element.
     */
    public void testValidUserRequestType() {
        this.getStringBuilder().append(USERID);
        this.getStringBuilder().append(
                "<emailAddress>test@sec2.org</emailAddress>");
        this.getStringBuilder().append(this.getSuffix());
        UserRequestType userRequestType =
                XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                UserRequestType.class);
        try {
            this.getValidator().validate(userRequestType);
        } catch (ValidationException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests a missing UserID.
     */
    public void testNoUserID() {
        this.getStringBuilder().append(
                "<emailAddress>test@sec2.org</emailAddress>");
        this.getStringBuilder().append(this.getSuffix());
        UserRequestType element = XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                UserRequestType.class);
        try {
            this.getValidator().validate(element);
            fail("Should have failed with a ValidationException");
        } catch (ValidationException e) {
            assertNotNull(e);
        }
    }

    /**
     * Tests a missing EmailAddress.
     */
    public void testNoEmailAddress() {
        this.getStringBuilder().append(USERID);
        this.getStringBuilder().append(this.getSuffix());
        UserRequestType element = XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                UserRequestType.class);
        try {
            this.getValidator().validate(element);
            fail("Should have failed with a ValidationException");
        } catch (ValidationException e) {
            assertNotNull(e);
        }
    }
}

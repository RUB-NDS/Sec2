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
import org.sec2.saml.xml.CreateGroup;
import org.sec2.saml.xml.GroupRequestType;

/**
 * Tests for the validator for the
 * {@link org.sec2.saml.xml.GroupRequestType} element.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 08, 2012
 */
public final class GroupRequestTypeSchemaValidatorTests
                    extends AbstractSchemaValidatorTests {
    /** {@inheritDoc }. */
    @Override
    protected QName getElementQName() {
        return CreateGroup.DEFAULT_ELEMENT_NAME;
    }

    /**
     * Tests a correct element.
     */
    public void testValidGroupRequestType() {
        this.getStringBuilder().append("<groupName>test</groupName>");
        this.getStringBuilder().append("<groupOwnerID>");
        this.getStringBuilder().append(BASE64DUMMY);
        this.getStringBuilder().append("</groupOwnerID>");
        this.getStringBuilder().append(this.getSuffix());
        GroupRequestType groupRequest = XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                GroupRequestType.class);
        try {
            this.getValidator().validate(groupRequest);
        } catch (ValidationException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests a missing GroupName.
     */
    public void testNoGroupName() {
        this.getStringBuilder().append("<groupOwnerID>");
        this.getStringBuilder().append(BASE64DUMMY);
        this.getStringBuilder().append("</groupOwnerID>");
        this.getStringBuilder().append(this.getSuffix());
        GroupRequestType element = XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                GroupRequestType.class);
        try {
            this.getValidator().validate(element);
            fail("Should have failed with a ValidationException");
        } catch (ValidationException e) {
            assertNotNull(e);
        }
    }

    /**
     * Tests a missing GroupOwnerID.
     */
    public void testNoGroupOwnerID() {
        this.getStringBuilder().append("<groupName>test</groupName>");
        this.getStringBuilder().append(this.getSuffix());
        GroupRequestType element = XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                GroupRequestType.class);
        try {
            this.getValidator().validate(element);
            fail("Should have failed with a ValidationException");
        } catch (ValidationException e) {
            assertNotNull(e);
        }
    }

    /**
     * Tests an empty GroupName.
     */
    public void testEmptyGroupName() {
        this.getStringBuilder().append("<groupName>  </groupName>");
        this.getStringBuilder().append("<groupOwnerID>");
        this.getStringBuilder().append(BASE64DUMMY);
        this.getStringBuilder().append("</groupOwnerID>");
        this.getStringBuilder().append(this.getSuffix());
        GroupRequestType element = XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                GroupRequestType.class);
        try {
            this.getValidator().validate(element);
            fail("Should have failed with a ValidationException");
        } catch (ValidationException e) {
            assertNotNull(e);
        }
    }
}

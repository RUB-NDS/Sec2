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
import org.sec2.saml.xml.AddUsersToGroup;
import org.sec2.saml.xml.GroupUsersRelationType;

/**
 * Tests for the validator for the
 * {@link org.sec2.saml.xml.GroupUsersRelationType} element.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * May 18, 2013
 */
public final class GroupUsersRelationTypeSchemaValidatorTests
                    extends AbstractSchemaValidatorTests {
    /** {@inheritDoc }. */
    @Override
    protected QName getElementQName() {
        return AddUsersToGroup.DEFAULT_ELEMENT_NAME;
    }

    /**
     * Tests a correct element.
     */
    public void testValidGroupUsersRelationType() {
        this.getStringBuilder().append(GROUPNAME);
        this.getStringBuilder().append(USERID);
        this.getStringBuilder().append(USERID);
        this.getStringBuilder().append(this.getSuffix());
        GroupUsersRelationType element =
                XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                GroupUsersRelationType.class);
        try {
            this.getValidator().validate(element);
        } catch (ValidationException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests a nulled groupName.
     */
    public void testNulledGroupName() {
        this.getStringBuilder().append("<groupName />");
        this.getStringBuilder().append(USERID);
        this.getStringBuilder().append(this.getSuffix());
        GroupUsersRelationType element =
                XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                GroupUsersRelationType.class);
        try {
            this.getValidator().validate(element);
            fail("Should have failed with a ValidationException");
        } catch (ValidationException e) {
            assertNotNull(e);
        }
    }

    /**
     * Tests a message without a group.
     */
    public void testNoGroupName() {
        this.getStringBuilder().append(USERID);
        this.getStringBuilder().append(this.getSuffix());
        GroupUsersRelationType element =
                XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                GroupUsersRelationType.class);
        try {
            this.getValidator().validate(element);
            fail("Should have failed with a ValidationException");
        } catch (ValidationException e) {
            assertNotNull(e);
        }
    }

    /**
     * Tests a message without a user.
     */
    public void testNoUserID() {
        this.getStringBuilder().append(GROUPNAME);
        this.getStringBuilder().append(this.getSuffix());
        GroupUsersRelationType element =
                XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                GroupUsersRelationType.class);
        try {
            this.getValidator().validate(element);
            fail("Should have failed with a ValidationException");
        } catch (ValidationException e) {
            assertNotNull(e);
        }
    }
}

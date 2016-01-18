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
import org.sec2.saml.xml.UserList;

/**
 * Tests for the validator for the {@link org.sec2.saml.xml.UserList}
 * element.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * March 16, 2013
 */
public final class UserListTypeSchemaValidatorTests
                    extends AbstractSchemaValidatorTests {
    /** {@inheritDoc }. */
    @Override
    protected QName getElementQName() {
        return UserList.DEFAULT_ELEMENT_NAME;
    }

    /**
     * Tests a correct list.
     */
    public void testValidUserList() {
        this.getStringBuilder().append(USERID);
        this.getStringBuilder().append(USERID);
        this.getStringBuilder().append(this.getSuffix());
        UserList userList = XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(), UserList.class);
        try {
            this.getValidator().validate(userList);
        } catch (ValidationException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests an empty element.
     */
    public void testEmptyElement() {
        this.getStringBuilder().append(this.getSuffix());
        UserList userList = XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(), UserList.class);
        try {
            this.getValidator().validate(userList);
        } catch (ValidationException e) {
            fail(e.getMessage());
        }
    }
}

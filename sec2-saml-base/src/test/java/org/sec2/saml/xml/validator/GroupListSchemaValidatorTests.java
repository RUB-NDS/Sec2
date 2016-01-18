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
import org.sec2.saml.xml.GroupList;

/**
 * Tests for the validator for the
 * {@link org.sec2.saml.xml.GroupList} element.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * May 18, 2013
 */
public final class GroupListSchemaValidatorTests
                    extends AbstractSchemaValidatorTests {
    /** {@inheritDoc }. */
    @Override
    protected QName getElementQName() {
        return GroupList.DEFAULT_ELEMENT_NAME;
    }

    /**
     * Tests a correct element.
     */
    public void testValidGroupList() {
        this.getStringBuilder().append(GROUPNAME);
        this.getStringBuilder().append(GROUPNAME);
        this.getStringBuilder().append(this.getSuffix());
        GroupList groupList = XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                GroupList.class);
        try {
            this.getValidator().validate(groupList);
        } catch (ValidationException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests an empty element.
     */
    public void testNoGroupName() {
        this.getStringBuilder().append(this.getSuffix());
        GroupList element = XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                GroupList.class);
        try {
            this.getValidator().validate(element);
        } catch (ValidationException e) {
            fail(e.getMessage());
        }
    }
}

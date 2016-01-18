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
import org.sec2.saml.xml.GroupInfo;
import org.sec2.saml.xml.GroupResponseType;

/**
 * Tests for the validator for the
 * {@link org.sec2.saml.xml.GroupResponseType} element.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * May 18, 2013
 */
public final class GroupResponseTypeSchemaValidatorTests
                    extends AbstractSchemaValidatorTests {
    /**
     * Some dummy ds:KeyInfo content.
     */
    private static final String KEYINFODUMMY = "<xenc:EncryptedData xmlns:xenc="
            + "\"http://www.w3.org/2001/04/xmlenc#\" xmlns:ds="
            + "\"http://www.w3.org/2000/09/xmldsig#\" Type=\"http://www."
            + "w3.org/2001/04/xmlenc#Element\"><xenc:EncryptionMethod "
            + "Algorithm=\"http://www.w3.org/2001/04/xmlenc#aes128-cbc\" />"
            + "<ds:KeyInfo><xenc:EncryptedKey><xenc:EncryptionMethod "
            + "Algorithm=\"http://www.w3.org/2001/04/xmlenc#rsa-1_5\" />"
            + "<ds:KeyInfo><ds:KeyName>Client</ds:KeyName></ds:KeyInfo>"
            + "<xenc:CipherData><xenc:CipherValue>W1ZlcnNjaGz8c3NlbHRlciBF"
            + "bnRzY2hs/HNzZWx1bmdzLVNjaGz8c3NlbF0=</xenc:CipherValue>"
            + "</xenc:CipherData></xenc:EncryptedKey></ds:KeyInfo>"
            + "<xenc:CipherData><xenc:CipherValue>VzFabGNuTmphR3o4YzNObGJI"
            + "UmxjaUJUWTJocy9ITnpaV3hk</xenc:CipherValue>"
            + "</xenc:CipherData></xenc:EncryptedData>";

    /** {@inheritDoc }. */
    @Override
    protected QName getElementQName() {
        return GroupInfo.DEFAULT_ELEMENT_NAME;
    }

    /**
     * Tests a correct element without GroupMemberList.
     */
    public void testValidGroupResponseType1() {
        this.getStringBuilder().append(GROUPNAME);
        this.getStringBuilder().append("<groupOwnerID>");
        this.getStringBuilder().append(BASE64DUMMY);
        this.getStringBuilder().append("</groupOwnerID>");
        this.getStringBuilder().append("<groupKey>");
        this.getStringBuilder().append(KEYINFODUMMY);
        this.getStringBuilder().append("</groupKey>");
        this.getStringBuilder().append(this.getSuffix());
        GroupResponseType groupResponse =
                XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                GroupResponseType.class);
        try {
            this.getValidator().validate(groupResponse);
        } catch (ValidationException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests a correct element with GroupMemberList.
     */
    public void testValidGroupResponseType2() {
        this.getStringBuilder().append(GROUPNAME);
        this.getStringBuilder().append("<groupOwnerID>");
        this.getStringBuilder().append(BASE64DUMMY);
        this.getStringBuilder().append("</groupOwnerID>");
        this.getStringBuilder().append("<groupKey>");
        this.getStringBuilder().append(KEYINFODUMMY);
        this.getStringBuilder().append("</groupKey>");
        this.getStringBuilder().append("<groupMemberList>");
        this.getStringBuilder().append(USERID);
        this.getStringBuilder().append(USERID);
        this.getStringBuilder().append("</groupMemberList>");
        this.getStringBuilder().append(this.getSuffix());
        GroupResponseType groupResponse =
                XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                GroupResponseType.class);
        try {
            this.getValidator().validate(groupResponse);
        } catch (ValidationException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests a missing GroupOwnerID.
     */
    public void testNoGroupOwnerID() {
        this.getStringBuilder().append(GROUPNAME);
        this.getStringBuilder().append("<groupKey>");
        this.getStringBuilder().append(BASE64DUMMY);
        this.getStringBuilder().append("</groupKey>");
        this.getStringBuilder().append(this.getSuffix());
        GroupResponseType element = XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                GroupResponseType.class);
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
        this.getStringBuilder().append("<groupName>   </groupName>");
        this.getStringBuilder().append("<groupOwnerID>");
        this.getStringBuilder().append(BASE64DUMMY);
        this.getStringBuilder().append("</groupOwnerID>");
        this.getStringBuilder().append("<groupKey>");
        this.getStringBuilder().append(BASE64DUMMY);
        this.getStringBuilder().append("</groupKey>");
        this.getStringBuilder().append(this.getSuffix());
        GroupResponseType element = XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                GroupResponseType.class);
        try {
            this.getValidator().validate(element);
            fail("Should have failed with a ValidationException");
        } catch (ValidationException e) {
            assertNotNull(e);
        }
    }

    /**
     * Tests a missing groupkey.
     */
    public void testNoGroupKey() {
        this.getStringBuilder().append(GROUPNAME);
        this.getStringBuilder().append("<groupOwnerID>");
        this.getStringBuilder().append(BASE64DUMMY);
        this.getStringBuilder().append("</groupOwnerID>");
        this.getStringBuilder().append(this.getSuffix());
        GroupResponseType element = XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                GroupResponseType.class);
        try {
            this.getValidator().validate(element);
            fail("Should have failed with a ValidationException");
        } catch (ValidationException e) {
            assertNotNull(e);
        }
    }

    /**
     * Tests a nulled GroupName.
     */
    public void testNulledGroupName() {
        this.getStringBuilder().append("<groupName />");
        this.getStringBuilder().append("<groupOwnerID>");
        this.getStringBuilder().append(BASE64DUMMY);
        this.getStringBuilder().append("</groupOwnerID>");
        this.getStringBuilder().append("<groupKey>");
        this.getStringBuilder().append(BASE64DUMMY);
        this.getStringBuilder().append("</groupKey>");
        this.getStringBuilder().append(this.getSuffix());
        GroupResponseType element = XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                GroupResponseType.class);
        try {
            this.getValidator().validate(element);
            fail("Should have failed with a ValidationException");
        } catch (ValidationException e) {
            assertNotNull(e);
        }
    }
}

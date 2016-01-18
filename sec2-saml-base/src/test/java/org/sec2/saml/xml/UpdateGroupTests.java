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
package org.sec2.saml.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

/**
 * Tests for the UpdateGroup element.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * May 18, 2013
 */
public final class UpdateGroupTests
                    extends AbstractXMLElementTests<UpdateGroup> {
    /** {@inheritDoc }. */
    @Override
    protected QName getElementQName() {
        return UpdateGroup.DEFAULT_ELEMENT_NAME;
    }

    /** {@inheritDoc }. */
    @Override
    protected UpdateGroup getElementForUnmarshalling() {
        UpdateGroup xml = this.getBuilder().
                buildObject(this.getElementQName());
        xml.setGroupName(this.getXsGenerator().
                buildXSString(GroupName.DEFAULT_ELEMENT_NAME));
        xml.getGroupName().setValue("testGroup");
        xml.setGroupOwnerID(this.getXsGenerator().
                buildXSBase64Binary(GroupOwnerID.DEFAULT_ELEMENT_NAME));
        xml.getGroupOwnerID().setValue("AAECAwQFBgcICQoLDA0ODw==");
        xml.setGroupName(this.getXsGenerator().
                buildXSString(GroupName.DEFAULT_ELEMENT_NAME));
        xml.getGroupName().setValue("testgroup");
        return xml;
    }

    /** {@inheritDoc }. */
    @Override
    protected List<String> getInvalidElements() {
        List<String> list = new ArrayList<String>();

        // double GroupOwnerID
        this.getStringBuilder().append("<groupName>test</groupName>");
        this.getStringBuilder().append("<groupOwnerID>");
        this.getStringBuilder().append(BASE64DUMMY);
        this.getStringBuilder().append("</groupOwnerID>");
        this.getStringBuilder().append("<groupOwnerID>");
        this.getStringBuilder().append(BASE64DUMMY);
        this.getStringBuilder().append("</groupOwnerID>");
        this.getStringBuilder().append("<groupName>1</groupName>");
        this.getStringBuilder().append(this.getSuffix());
        list.add(this.getStringBuilder().toString());

        this.resetStringBuilder();

        // double GroupName
        this.getStringBuilder().append("<groupName>test</groupName>");
        this.getStringBuilder().append("<groupName>test</groupName>");
        this.getStringBuilder().append("<groupOwnerID>");
        this.getStringBuilder().append(BASE64DUMMY);
        this.getStringBuilder().append("</groupOwnerID>");
        this.getStringBuilder().append(this.getSuffix());
        list.add(this.getStringBuilder().toString());

        this.resetStringBuilder();

        return list;
    }
}

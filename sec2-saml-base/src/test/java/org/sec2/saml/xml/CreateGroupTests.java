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
 * Tests for the CreateGroup element.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 09, 2012
 */
public final class CreateGroupTests
                    extends AbstractXMLElementTests<CreateGroup> {
    /** {@inheritDoc }. */
    protected QName getElementQName() {
        return CreateGroup.DEFAULT_ELEMENT_NAME;
    }

    /** {@inheritDoc }. */
    protected CreateGroup getElementForUnmarshalling() {
        CreateGroup xml = this.getBuilder().
                buildObject(this.getElementQName());
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

        return list;
    }
}

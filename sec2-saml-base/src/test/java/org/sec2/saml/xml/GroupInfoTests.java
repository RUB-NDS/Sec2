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
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.signature.KeyInfo;
import org.sec2.saml.engine.SAMLEngine;

/**
 * Tests for the GroupInfo element.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * May 18, 2012
 */
public final class GroupInfoTests
                    extends AbstractXMLElementTests<GroupInfo> {
    /** {@inheritDoc }. */
    @Override
    protected QName getElementQName() {
        return GroupInfo.DEFAULT_ELEMENT_NAME;
    }

    /** {@inheritDoc }. */
    @Override
    protected GroupInfo getElementForUnmarshalling() {
        GroupInfo xml = this.getBuilder().
                buildObject(this.getElementQName());
        xml.setGroupName(this.getXsGenerator().
                buildXSString(GroupName.DEFAULT_ELEMENT_NAME));
        xml.getGroupName().setValue("testgroup");
        xml.setGroupKey(SAMLEngine.getXMLBuilder(KeyInfo.class,
                KeyInfo.DEFAULT_ELEMENT_NAME).
                buildObject(GroupKey.DEFAULT_ELEMENT_NAME));
        XMLObjectBuilder<GroupMemberList> gmlBuilder =
                SAMLEngine.getXMLBuilder(GroupMemberList.class,
                GroupMemberList.DEFAULT_ELEMENT_NAME);
        xml.setGroupMemberList(gmlBuilder.buildObject(
                GroupMemberList.DEFAULT_ELEMENT_NAME));
        xml.getGroupMemberList().getUserIDs();
        return xml;
    }

    /** {@inheritDoc }. */
    @Override
    protected List<String> getInvalidElements() {
        List<String> list = new ArrayList<String>();

        // double GroupName
        this.getStringBuilder().append(GROUPNAME);
        this.getStringBuilder().append(GROUPNAME);
        this.getStringBuilder().append("<groupOwnerID>");
        this.getStringBuilder().append(BASE64DUMMY);
        this.getStringBuilder().append("</groupOwnerID>");
        this.getStringBuilder().append("<groupKey>");
        this.getStringBuilder().append(BASE64DUMMY);
        this.getStringBuilder().append("</groupKey>");
        this.getStringBuilder().append(this.getSuffix());
        list.add(this.getStringBuilder().toString());

        this.resetStringBuilder();

        // double GroupKey
        this.getStringBuilder().append(GROUPNAME);
        this.getStringBuilder().append("<groupOwnerID>");
        this.getStringBuilder().append(BASE64DUMMY);
        this.getStringBuilder().append("</groupOwnerID>");
        this.getStringBuilder().append("<groupKey>");
        this.getStringBuilder().append(BASE64DUMMY);
        this.getStringBuilder().append("</groupKey>");
        this.getStringBuilder().append("<groupKey>");
        this.getStringBuilder().append(BASE64DUMMY);
        this.getStringBuilder().append("</groupKey>");
        this.getStringBuilder().append(this.getSuffix());
        list.add(this.getStringBuilder().toString());

        this.resetStringBuilder();

        // double GroupMemberList
        this.getStringBuilder().append(GROUPNAME);
        this.getStringBuilder().append("<groupOwnerID>");
        this.getStringBuilder().append(BASE64DUMMY);
        this.getStringBuilder().append("</groupOwnerID>");
        this.getStringBuilder().append("<groupKey>");
        this.getStringBuilder().append(BASE64DUMMY);
        this.getStringBuilder().append("</groupKey>");
        this.getStringBuilder().append("<groupMemberList>");
        this.getStringBuilder().append(USERID);
        this.getStringBuilder().append(USERID);
        this.getStringBuilder().append("</groupMemberList>");
        this.getStringBuilder().append("<groupMemberList>");
        this.getStringBuilder().append(USERID);
        this.getStringBuilder().append(USERID);
        this.getStringBuilder().append("</groupMemberList>");
        this.getStringBuilder().append(this.getSuffix());
        list.add(this.getStringBuilder().toString());

        return list;
    }
}

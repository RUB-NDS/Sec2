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

import javax.xml.namespace.QName;

/**
 * Tests for the GroupList element.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * May 18, 2013
 */
public final class GroupListTests
                    extends AbstractXMLElementTests<GroupList> {
    /** {@inheritDoc }. */
    @Override
    protected QName getElementQName() {
        return GroupList.DEFAULT_ELEMENT_NAME;
    }

    /** {@inheritDoc }. */
    @Override
    protected GroupList getElementForUnmarshalling() {
        GroupList xml = this.getBuilder().
                buildObject(this.getElementQName());
        xml.getGroups().add(this.getXsGenerator().
                buildXSString(GroupName.DEFAULT_ELEMENT_NAME));
        return xml;
    }
}

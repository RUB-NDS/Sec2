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
 * Tests for the UserList element.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 17, 2012
 */
public final class UserListTests
                    extends AbstractXMLElementTests<UserList> {
    /** {@inheritDoc }. */
    @Override
    protected QName getElementQName() {
        return UserList.DEFAULT_ELEMENT_NAME;
    }

    /** {@inheritDoc }. */
    @Override
    protected UserList getElementForUnmarshalling() {
        UserList xml = this.getBuilder().
                buildObject(this.getElementQName());
        xml.getUserIDs().add(this.getXsGenerator().
                buildXSBase64Binary(UserID.DEFAULT_ELEMENT_NAME));
        return xml;
    }
}

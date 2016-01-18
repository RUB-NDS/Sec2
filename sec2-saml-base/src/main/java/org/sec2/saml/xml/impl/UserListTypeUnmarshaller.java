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
package org.sec2.saml.xml.impl;

import javax.xml.namespace.QName;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.schema.XSBase64Binary;
import org.sec2.saml.xml.UserID;
import org.sec2.saml.xml.UserListType;

/**
 * Thread-safe unmarshaller of {@link org.sec2.saml.xml.UserListType}
 * objects.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 17, 2012
 */
public class UserListTypeUnmarshaller
            extends AbstractSec2Unmarshaller {

    /** {@inheritDoc} */
    @Override
    protected void processChildElement(final XMLObject parentXMLObject,
            final XMLObject childXMLObject) throws UnmarshallingException {
        UserListType userList = (UserListType) parentXMLObject;
        QName childQName = childXMLObject.getElementQName();

        if (childQName.getLocalPart().equals(
                UserID.DEFAULT_ELEMENT_LOCAL_NAME)
                && childQName.getNamespaceURI().equals(
                UserID.DEFAULT_ELEMENT_NAME.getNamespaceURI())) {
            userList.getUserIDs().add((XSBase64Binary) childXMLObject);
        }
    }
}

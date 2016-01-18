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
import org.opensaml.xml.schema.XSString;
import org.sec2.saml.xml.GroupBaseType;
import org.sec2.saml.xml.GroupName;
import org.sec2.saml.xml.GroupOwnerID;

/**
 * Thread-safe unmarshaller of {@link org.sec2.saml.xml.GroupBaseType}
 * objects.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * May 18, 2013
 */
public class GroupBaseTypeUnmarshaller extends AbstractSec2Unmarshaller {

    /** {@inheritDoc} */
    @Override
    protected void processChildElement(final XMLObject parentXMLObject,
            final XMLObject childXMLObject) throws UnmarshallingException {
        GroupBaseType groupBase = (GroupBaseType) parentXMLObject;
        QName childQName = childXMLObject.getElementQName();

        if (childQName.getLocalPart().equals(
                GroupName.DEFAULT_ELEMENT_LOCAL_NAME)
                && childQName.getNamespaceURI().equals(
                GroupName.DEFAULT_ELEMENT_NAME.getNamespaceURI())) {
            if (groupBase.getGroupName() == null) {
                groupBase.setGroupName((XSString) childXMLObject);
            } else {
                throw new UnmarshallingException("Only one GroupName allowed!");
            }
        } else if (childQName.getLocalPart().equals(
                GroupOwnerID.DEFAULT_ELEMENT_LOCAL_NAME)
                && childQName.getNamespaceURI().equals(
                GroupOwnerID.DEFAULT_ELEMENT_NAME.getNamespaceURI())) {
            if (groupBase.getGroupOwnerID() == null) {
                groupBase.setGroupOwnerID((XSBase64Binary) childXMLObject);
            } else {
                throw new UnmarshallingException(
                        "Only one GroupOwnerID allowed!");
            }
        }
    }
}

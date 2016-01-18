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
import org.opensaml.xml.schema.XSBase64Binary;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.validation.ValidatingXMLObject;
import org.sec2.saml.SAMLBaseConfig;

/**
 * A base interface for an XML message containing information about a group.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 02, 2012
 */
public interface GroupBaseType extends ValidatingXMLObject {
    /** Local name of the XSI type. */
    String TYPE_LOCAL_NAME = "groupBaseType";

    /** QName of the XSI type. */
    QName TYPE_NAME = new QName(SAMLBaseConfig.SEC2_SAML_NS, TYPE_LOCAL_NAME,
            SAMLBaseConfig.SEC2_SAML_PREFIX);

    /**
     * @param groupName The name to set
     */
    void setGroupName(XSString groupName);

    /**
     * @return The group's name
     */
    XSString getGroupName();

    /**
     * @param groupOwnerID The ID of the owner of the group to set
     */
    void setGroupOwnerID(XSBase64Binary groupOwnerID);

    /**
     * @return The ID of the owner of the group
     */
    XSBase64Binary getGroupOwnerID();
}

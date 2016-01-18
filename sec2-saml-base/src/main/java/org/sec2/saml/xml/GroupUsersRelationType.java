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

import java.util.List;
import javax.xml.namespace.QName;
import org.opensaml.xml.schema.XSBase64Binary;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.validation.ValidatingXMLObject;
import org.sec2.saml.SAMLBaseConfig;

/**
 * A base interface for an XML message containing a relation between one group
 * and many users.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * May 18, 2013
 */
public interface GroupUsersRelationType extends ValidatingXMLObject {
    /** Local name of the XSI type. */
    String TYPE_LOCAL_NAME = "groupUsersRelationType";

    /** QName of the XSI type. */
    QName TYPE_NAME = new QName(SAMLBaseConfig.SEC2_SAML_NS, TYPE_LOCAL_NAME,
            SAMLBaseConfig.SEC2_SAML_PREFIX);

    /**
     * @param newGroupName The name of the single group
     */
    void setGroupName(XSString newGroupName);

    /**
     * @return The name of the single group
     */
    XSString getGroupName();

    /**
     * @return A list of the IDs of all users
     */
    List<XSBase64Binary> getUsers();
}

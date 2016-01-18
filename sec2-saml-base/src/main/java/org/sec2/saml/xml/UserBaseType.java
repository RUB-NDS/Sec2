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
import org.opensaml.xml.validation.ValidatingXMLObject;
import org.sec2.saml.SAMLBaseConfig;

/**
 * A base interface for an XML message containing information about a user.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 02, 2012
 */
public interface UserBaseType extends ValidatingXMLObject {
    /** Local name of the XSI type. */
    String TYPE_LOCAL_NAME = "userBaseType";

    /** QName of the XSI type. */
    QName TYPE_NAME = new QName(SAMLBaseConfig.SEC2_SAML_NS, TYPE_LOCAL_NAME,
            SAMLBaseConfig.SEC2_SAML_PREFIX);

    /**
     * @return The ID of the user
     */
    XSBase64Binary getUserID();

    /**
     * @param userID The ID of the user to set
     */
    void setUserID(XSBase64Binary userID);

    /**
     * @return The user's email-address
     */
    EmailAddressType getEmailAddress();

    /**
     * @param emailAddress The user's email-address to set
     */
    void setEmailAddress(EmailAddressType emailAddress);
}

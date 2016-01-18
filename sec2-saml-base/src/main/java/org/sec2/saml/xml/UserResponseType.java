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
import org.sec2.saml.SAMLBaseConfig;

/**
 * An interface for an XML request message containing information about a user.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 01, 2012
 */
//CHECKSTYLE.OFF: InterfaceIsType - OpenSAML software design convention
public interface UserResponseType extends UserBaseType {
    /** Local name of the XSI type. */
    String TYPE_LOCAL_NAME = "userResponseType";

    /** QName of the XSI type. */
    QName TYPE_NAME = new QName(SAMLBaseConfig.SEC2_SAML_NS, TYPE_LOCAL_NAME,
            SAMLBaseConfig.SEC2_SAML_PREFIX);
}
//CHECKSTYLE.ON: InterfaceIsType

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
import org.opensaml.xml.validation.ValidatingXMLObject;
import org.sec2.saml.SAMLBaseConfig;

/**
 * An interface for an XML type containing a list of users.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 17, 2012
 */
public interface UserListType extends ValidatingXMLObject {
    /** Local name of the XSI type. */
    String TYPE_LOCAL_NAME = "userListType";

    /** QName of the XSI type. */
    QName TYPE_NAME = new QName(SAMLBaseConfig.SEC2_SAML_NS, TYPE_LOCAL_NAME,
            SAMLBaseConfig.SEC2_SAML_PREFIX);

    /**
     * @return A list of the user's IDs
     */
    List<XSBase64Binary> getUserIDs();
}

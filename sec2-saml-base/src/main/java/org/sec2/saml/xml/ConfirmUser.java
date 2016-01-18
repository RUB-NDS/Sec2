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
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.signature.SignableXMLObject;
import org.opensaml.xml.validation.ValidatingXMLObject;
import org.sec2.saml.SAMLBaseConfig;

/**
 * An interface for an XML element containing a user's signed challenge.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * March 16, 2013
 */
public interface ConfirmUser
            extends SignableXMLObject, ValidatingXMLObject, Sec2RequestMessage {
    /** Local name of the ConfirmUser element. */
    String DEFAULT_ELEMENT_LOCAL_NAME = "confirmUser";

    /** Default element name. */
    QName DEFAULT_ELEMENT_NAME = new QName(SAMLBaseConfig.SEC2_SAML_NS,
            DEFAULT_ELEMENT_LOCAL_NAME, SAMLBaseConfig.SEC2_SAML_PREFIX);

    /** ID attribute name. */
    String ID_ATTRIB_NAME = "ID";

    /**
     * Get the challenge child element.
     *
     * @return the challenge child element
     */
    XSString getChallenge();

    /**
     * @param newChallenge the challenge to set
     */
    void setChallenge(XSString newChallenge);

    /**
     * Sets the ID of this element.
     *
     * @return the ID of this element
     */
    String getID();

    /**
     * Sets the ID of this element.
     *
     * @param newID the ID of this element
     */
    void setID(String newID);
}

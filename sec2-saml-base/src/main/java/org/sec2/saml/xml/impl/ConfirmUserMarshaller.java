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

import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.sec2.saml.xml.ConfirmUser;
import org.w3c.dom.Element;

/**
 * A thread-safe Marshaller for {@link org.sec2.saml.xml.ConfirmUser}.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * March 16, 2013
 */
public class ConfirmUserMarshaller extends Sec2Marshaller {
    /** {@inheritDoc} */
    @Override
    protected void marshallAttributes(final XMLObject xmlObject,
            final Element domElement) throws MarshallingException {
        ConfirmUser confirmUser = (ConfirmUser) xmlObject;
        if (confirmUser.getID() != null) {
            domElement.setAttributeNS(null, ConfirmUser.ID_ATTRIB_NAME,
                    confirmUser.getID());
            domElement.setIdAttributeNS(null, ConfirmUser.ID_ATTRIB_NAME, true);
        }
    }
}

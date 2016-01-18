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
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.signature.Signature;
import org.sec2.saml.xml.Challenge;
import org.sec2.saml.xml.ConfirmUser;
import org.sec2.saml.xml.SignatureCertificate;
import org.w3c.dom.Attr;

/**
 * Thread-safe unmarshaller of {@link org.sec2.saml.xml.ConfirmUser}
 * objects.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * March 16, 2013
 */
public class ConfirmUserUnmarshaller extends AbstractSec2Unmarshaller {

    /** {@inheritDoc} */
    @Override
    protected void processChildElement(final XMLObject parentXMLObject,
            final XMLObject childXMLObject) throws UnmarshallingException {
        ConfirmUser confirmUser = (ConfirmUser) parentXMLObject;
        QName childQName = childXMLObject.getElementQName();

        if (childQName.getLocalPart().equals(
                Challenge.DEFAULT_ELEMENT_LOCAL_NAME)
                && childQName.getNamespaceURI().equals(
                SignatureCertificate.DEFAULT_ELEMENT_NAME.getNamespaceURI())) {
            if (confirmUser.getChallenge() == null) {
                confirmUser.setChallenge(
                    (XSString) childXMLObject);
            } else {
                throw new UnmarshallingException(
                        "Only one Challenge allowed!");
            }
        } else if (childXMLObject instanceof Signature) {
            confirmUser.setSignature((Signature) childXMLObject);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected final void processAttribute(final XMLObject xmlObject,
            final Attr attribute) throws UnmarshallingException {
        ConfirmUser confirmUser = (ConfirmUser) xmlObject;

        if (attribute.getLocalName().equals(ConfirmUser.ID_ATTRIB_NAME)) {
            confirmUser.setID(attribute.getValue());
            attribute.getOwnerElement().setIdAttributeNode(attribute, true);
        } else {
            super.processAttribute(xmlObject, attribute);
        }
    }
}

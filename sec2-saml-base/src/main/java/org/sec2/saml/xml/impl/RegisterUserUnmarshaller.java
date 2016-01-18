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
import org.sec2.saml.xml.EncryptionCertificate;
import org.sec2.saml.xml.RegisterUser;
import org.sec2.saml.xml.SignatureCertificate;

/**
 * Thread-safe unmarshaller of {@link org.sec2.saml.xml.RegisterUser}
 * objects.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * February 04, 2013
 */
public class RegisterUserUnmarshaller
            extends AbstractSec2Unmarshaller {

    /** {@inheritDoc} */
    @Override
    protected void processChildElement(final XMLObject parentXMLObject,
            final XMLObject childXMLObject) throws UnmarshallingException {
        RegisterUser registerUser = (RegisterUser) parentXMLObject;
        QName childQName = childXMLObject.getElementQName();

        if (childQName.getLocalPart().equals(
                SignatureCertificate.DEFAULT_ELEMENT_LOCAL_NAME)
                && childQName.getNamespaceURI().equals(
                SignatureCertificate.DEFAULT_ELEMENT_NAME.getNamespaceURI())) {
            if (registerUser.getSignatureCertificate() == null) {
                registerUser.setSignatureCertificate(
                    (XSBase64Binary) childXMLObject);
            } else {
                throw new UnmarshallingException(
                        "Only one SignatureCertificate allowed!");
            }
        } else if (childQName.getLocalPart().equals(
                EncryptionCertificate.DEFAULT_ELEMENT_LOCAL_NAME)
                && childQName.getNamespaceURI().equals(
                EncryptionCertificate.DEFAULT_ELEMENT_NAME.getNamespaceURI())) {
            if (registerUser.getEncryptionCertificate() == null) {
                registerUser.setEncryptionCertificate(
                    (XSBase64Binary) childXMLObject);
            } else {
                throw new UnmarshallingException(
                        "Only one EncryptionCertificate allowed!");
            }
        }
    }
}

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSBase64Binary;
import org.opensaml.xml.validation.AbstractValidatingXMLObject;
import org.sec2.saml.xml.RegisterUser;

/**
 * A concrete implementation of
 * {@link org.sec2.saml.xml.RegisterUser}.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * February 04, 2013
 */
public class RegisterUserImpl
            extends AbstractValidatingXMLObject implements RegisterUser {
    /**
     * The user's signature certificate.
     */
    private XSBase64Binary signatureCertificate;

    /**
     * The user's encryption certificate.
     */
    private XSBase64Binary encryptionCertificate;

    /**
     * Constructor.
     *
     * @param namespaceURI the namespace the element is in
     * @param elementLocalName the local name of the XML element this
     * Object represents
     * @param namespacePrefix the prefix for the given namespace
     */
    protected RegisterUserImpl(final String namespaceURI,
            final String elementLocalName, final String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    /**
     * @return the signatureCertificate
     */
    @Override
    public XSBase64Binary getSignatureCertificate() {
        return signatureCertificate;
    }

    /**
     * @param newSignatureCertificate the signatureCertificate to set
     */
    @Override
    public void setSignatureCertificate(
            final XSBase64Binary newSignatureCertificate) {
        this.signatureCertificate = newSignatureCertificate;
    }

    /**
     * @return the encryptionCertificate
     */
    @Override
    public XSBase64Binary getEncryptionCertificate() {
        return encryptionCertificate;
    }

    /**
     * @param newEncryptionCertificate the encryptionCertificate to set
     */
    @Override
    public void setEncryptionCertificate(
            final XSBase64Binary newEncryptionCertificate) {
        this.encryptionCertificate = newEncryptionCertificate;
    }

    /** {@inheritDoc} */
    @Override
    public List<XMLObject> getOrderedChildren() {
        ArrayList<XMLObject> children = new ArrayList<XMLObject>();
        children.add(this.signatureCertificate);
        children.add(this.encryptionCertificate);
        return Collections.unmodifiableList(children);
    }
}

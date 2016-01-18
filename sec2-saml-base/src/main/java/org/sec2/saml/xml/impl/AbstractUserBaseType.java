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
import org.sec2.saml.xml.EmailAddressType;
import org.sec2.saml.xml.UserBaseType;

/**
 * An abstract implementation of {@link org.sec2.saml.xml.UserBaseType}.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 02, 2012
 */
public abstract class AbstractUserBaseType
            extends AbstractValidatingXMLObject implements UserBaseType {
    /**
     * The user's ID.
     */
    private XSBase64Binary userID;

    /**
     * The user's e-mail-address.
     */
    private EmailAddressType emailAddress;

    /**
     * Constructor.
     *
     * @param namespaceURI the namespace the element is in
     * @param elementLocalName the local name of the XML element this
     * Object represents
     * @param namespacePrefix the prefix for the given namespace
     */
    protected AbstractUserBaseType(final String namespaceURI,
            final String elementLocalName, final String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    /** {@inheritDoc} */
    @Override
    public XSBase64Binary getUserID() {
        return this.userID;
    }

    /** {@inheritDoc} */
    @Override
    public void setUserID(final XSBase64Binary newUserID) {
        this.userID = prepareForAssignment(this.userID, newUserID);
    }

    /** {@inheritDoc} */
    @Override
    public EmailAddressType getEmailAddress() {
        return this.emailAddress;
    }

    /** {@inheritDoc} */
    @Override
    public void setEmailAddress(final EmailAddressType newEmailAddress) {
        this.emailAddress =
                prepareForAssignment(this.emailAddress, newEmailAddress);
    }

    /** {@inheritDoc} */
    @Override
    public List<XMLObject> getOrderedChildren() {
        ArrayList<XMLObject> children = new ArrayList<XMLObject>();
        children.add(this.userID);
        children.add(this.emailAddress);
        return Collections.unmodifiableList(children);
    }
}

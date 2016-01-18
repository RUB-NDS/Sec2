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
import org.opensaml.xml.AbstractValidatingSignableXMLObject;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.signature.Signature;
import org.sec2.saml.xml.ConfirmUser;

/**
 * A concrete implementation of {@link org.sec2.saml.xml.ConfirmUser}.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * March 16, 2013
 */
public class ConfirmUserImpl
            extends AbstractValidatingSignableXMLObject implements ConfirmUser {
    /**
     * The user's challenge.
     */
    private XSString challenge;

    /** ID of the element. */
    private String id;

    /**
     * Constructor.
     *
     * @param namespaceURI the namespace the element is in
     * @param elementLocalName the local name of the XML element this
     * Object represents
     * @param namespacePrefix the prefix for the given namespace
     */
    protected ConfirmUserImpl(final String namespaceURI,
            final String elementLocalName, final String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    /**
     * @return the challenge
     */
    @Override
    public XSString getChallenge() {
        return challenge;
    }

    /**
     * @param newChallenge the challenge to set
     */
    @Override
    public void setChallenge(final XSString newChallenge) {
        this.challenge = newChallenge;
    }

    /** {@inheritDoc} */
    @Override
    public List<XMLObject> getOrderedChildren() {
        ArrayList<XMLObject> children = new ArrayList<XMLObject>();
        children.add(challenge);
        if (getSignature() != null) {
            children.add(getSignature());
        }
        return Collections.unmodifiableList(children);
    }

    /** {@inheritDoc} */
    @Override
    public String getID() {
        return id;
    }

    /** {@inheritDoc} */
    @Override
    public void setID(final String newID) {
        String oldID = this.id;
        this.id = prepareForAssignment(this.id, newID);
        registerOwnID(oldID, this.id);
    }

    /**
     * {@inheritDoc}
     *
     * When a signature is added, a default content reference that uses the ID
     * of this object will be created and added to the signature at the time of
     * signing.
     */
    @Override
    public void setSignature(final Signature newSignature) {
        if (newSignature != null) {
            newSignature.getContentReferences().add(
                    new XMLObjectContentReference(this));
        }
        super.setSignature(newSignature);
    }

    /**
     * Gets the value of the ID attribute for this XML object which will be used
     * as its signature reference.
     *
     * @return the value of this XMLObject ID attribute
     */
    public String getSignatureReferenceID() {
        return id;
    }
}

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
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.validation.AbstractValidatingXMLObject;
import org.sec2.saml.xml.GroupBaseType;

/**
 * An abstract implementation of {@link org.sec2.saml.xml.GroupBaseType}.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 02, 2012
 */
public abstract class AbstractGroupBaseType
            extends AbstractValidatingXMLObject implements GroupBaseType {

    /**
     * The group's name.
     */
    private XSString groupName;

    /**
     * The group's owner's ID.
     */
    private XSBase64Binary groupOwnerID;

    /**
     * Constructor.
     *
     * @param namespaceURI the namespace the element is in
     * @param elementLocalName the local name of the XML element this
     * Object represents
     * @param namespacePrefix the prefix for the given namespace
     */
    protected AbstractGroupBaseType(final String namespaceURI,
            final String elementLocalName, final String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    /** {@inheritDoc} */
    @Override
    public XSString getGroupName() {
        return this.groupName;
    }

    /** {@inheritDoc} */
    @Override
    public void setGroupName(final XSString newName) {
        this.groupName = prepareForAssignment(this.groupName, newName);
    }

    /** {@inheritDoc} */
    @Override
    public XSBase64Binary getGroupOwnerID() {
        return this.groupOwnerID;
    }

    /** {@inheritDoc} */
    @Override
    public void setGroupOwnerID(final XSBase64Binary newGroupOwnerID) {
        this.groupOwnerID = prepareForAssignment(
                this.groupOwnerID, newGroupOwnerID);
    }

    /** {@inheritDoc} */
    @Override
    public List<XMLObject> getOrderedChildren() {
        ArrayList<XMLObject> children = new ArrayList<XMLObject>();
        children.add(this.groupName);
        children.add(this.groupOwnerID);
        return Collections.unmodifiableList(children);
    }
}

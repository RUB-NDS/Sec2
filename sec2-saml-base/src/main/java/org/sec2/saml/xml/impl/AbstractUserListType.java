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
import org.opensaml.xml.util.XMLObjectChildrenList;
import org.opensaml.xml.validation.AbstractValidatingXMLObject;
import org.sec2.saml.xml.UserListType;

/**
 * An abstract implementation of {@link org.sec2.saml.xml.UserListType}.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 17, 2012
 */
public abstract class AbstractUserListType
                    extends AbstractValidatingXMLObject
                    implements UserListType {
    /**
     * A list of IDs of users.
     */
    private XMLObjectChildrenList<XSBase64Binary> users;

    /**
     * Constructor.
     *
     * @param namespaceURI the namespace the element is in
     * @param elementLocalName the local name of the XML element this
     * Object represents
     * @param namespacePrefix the prefix for the given namespace
     */
    protected AbstractUserListType(final String namespaceURI,
            final String elementLocalName, final String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
        this.users = new XMLObjectChildrenList<XSBase64Binary>(this);
    }

    /** {@inheritDoc} */
    @Override
    public List<XSBase64Binary> getUserIDs() {
        return this.users;
    }

    /** {@inheritDoc} */
    @Override
    public List<XMLObject> getOrderedChildren() {
        ArrayList<XMLObject> children = new ArrayList<XMLObject>();
        children.addAll(this.users);
        return Collections.unmodifiableList(children);
    }
}

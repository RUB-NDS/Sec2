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
import org.opensaml.xml.signature.KeyInfo;
import org.sec2.saml.xml.GroupMemberList;
import org.sec2.saml.xml.GroupResponseType;

/**
 * An abstract implementation of {@link org.sec2.saml.xml.GroupResponseType}.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * May 18, 2012
 */
public abstract class AbstractGroupResponseType
            extends AbstractGroupBaseType implements GroupResponseType {
    /**
     * The group's key.
     */
    private KeyInfo groupKey;

    /**
     * The group's member element.
     */
    private GroupMemberList memberList;

    /**
     * Constructor.
     *
     * @param namespaceURI the namespace the element is in
     * @param elementLocalName the local name of the XML element this
     * Object represents
     * @param namespacePrefix the prefix for the given namespace
     */
    protected AbstractGroupResponseType(final String namespaceURI,
            final String elementLocalName, final String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    /** {@inheritDoc} */
    @Override
    public KeyInfo getGroupKey() {
        return this.groupKey;
    }

    /** {@inheritDoc} */
    @Override
    public void setGroupKey(final KeyInfo newGroupKey) {
        this.groupKey = prepareForAssignment(this.groupKey, newGroupKey);
    }

    /** {@inheritDoc} */
    @Override
    public GroupMemberList getGroupMemberList() {
        return this.memberList;
    }

    /** {@inheritDoc} */
    @Override
    public void setGroupMemberList(final GroupMemberList members) {
        this.memberList = prepareForAssignment(this.memberList, members);
    }

    /** {@inheritDoc} */
    @Override
    public List<XMLObject> getOrderedChildren() {
        ArrayList<XMLObject> children = new ArrayList<XMLObject>();
        children.addAll(super.getOrderedChildren());
        children.add(this.groupKey);
        children.add(this.memberList);
        return Collections.unmodifiableList(children);
    }
}

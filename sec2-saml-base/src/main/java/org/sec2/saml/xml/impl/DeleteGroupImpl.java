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

import org.opensaml.xml.schema.impl.XSStringImpl;
import org.sec2.saml.xml.DeleteGroup;

/**
 * A concrete implementation of {@link org.sec2.saml.xml.DeleteGroup}.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * May 29, 2013
 */
public class DeleteGroupImpl
                    extends XSStringImpl implements DeleteGroup {
    /**
     * Constructor.
     *
     * @param namespaceURI the namespace the element is in
     * @param elementLocalName the local name of the XML element this
     * Object represents
     * @param namespacePrefix the prefix for the given namespace
     */
    protected DeleteGroupImpl(final String namespaceURI,
            final String elementLocalName, final String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }
}

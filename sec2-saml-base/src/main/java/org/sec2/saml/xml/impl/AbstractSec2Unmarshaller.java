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
import org.opensaml.xml.io.AbstractXMLObjectUnmarshaller;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Attr;

/**
 * Thread-safe abstract base unmarshaller of all xml objects in the sec2saml
 * namespace.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 03, 2012
 */
public abstract class AbstractSec2Unmarshaller
            extends AbstractXMLObjectUnmarshaller {
    /** {@inheritDoc} */
    @Override
    protected void processAttribute(final XMLObject xmlObject,
            final Attr attribute) throws UnmarshallingException {
        throw new UnmarshallingException("The XML element "
                + xmlObject.getElementQName().toString() + "has the "
                + "attribute" + attribute.getName()
                + ". The sec2saml xml messages do not use attributes."
                + "Something must be wrong!");
    }

    /** {@inheritDoc} */
    @Override
    protected final void processElementContent(final XMLObject xmlObject,
            final String elementContent) {
        // no content
    }
}

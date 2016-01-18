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
import org.opensaml.xml.io.AbstractXMLObjectMarshaller;
import org.opensaml.xml.io.MarshallingException;
import org.w3c.dom.Element;

/**
 * Thread-safe marshaller for all sec2 xml objects. Saves writing separate stub
 * marshallers for all classes. Only xml elements with content or attributes
 * need their own marshallers. Therefore, only &lt;confirmUser&gt; has its own
 * marshaller.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * March 16, 2013
 */
public class Sec2Marshaller extends AbstractXMLObjectMarshaller {

    /** {@inheritDoc} */
    @Override
    protected void marshallAttributes(final XMLObject xmlObject,
            final Element domElement) throws MarshallingException {
        // no attributes
    }

    /** {@inheritDoc} */
    @Override
    protected final void marshallElementContent(final XMLObject xmlObject,
            final Element domElement) throws MarshallingException {
        // no content
    }
}

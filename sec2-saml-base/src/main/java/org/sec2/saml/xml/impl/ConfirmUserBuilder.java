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

import org.opensaml.xml.AbstractXMLObjectBuilder;
import org.sec2.saml.xml.ConfirmUser;

/**
 * Builder for {@link org.sec2.saml.xml.ConfirmUser} objects.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * February 04, 2013
 */
public class ConfirmUserBuilder
                    extends AbstractXMLObjectBuilder<ConfirmUser> {
    /** {@inheritDoc} */
    @Override
    public ConfirmUser buildObject(final String namespaceURI,
            final String localName, final String namespacePrefix) {
        return new ConfirmUserImpl(
                namespaceURI, localName, namespacePrefix);
    }
}

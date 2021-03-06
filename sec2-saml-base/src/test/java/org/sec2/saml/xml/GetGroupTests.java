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
package org.sec2.saml.xml;

import javax.xml.namespace.QName;

/**
 * Tests for the GetGroup element.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 09, 2012
 */
public final class GetGroupTests
                    extends AbstractXMLElementTests<GetGroup> {
    /** {@inheritDoc }. */
    @Override
    protected QName getElementQName() {
        return GetGroup.DEFAULT_ELEMENT_NAME;
    }

    /** {@inheritDoc }. */
    @Override
    protected GetGroup getElementForUnmarshalling() {
        GetGroup xml = this.getBuilder().
                buildObject(this.getElementQName());
        return xml;
    }
}

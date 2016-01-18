/*
 * Copyright 2013 Ruhr-University Bochum, Chair for Network and Data Security
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
 * Tests for the GetUserInfoByMail element.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * July 24, 2013
 */
public final class GetUserInfoByMailTests
                    extends AbstractXMLElementTests<GetUserInfoByMail> {
    /** {@inheritDoc }. */
    @Override
    protected QName getElementQName() {
        return GetUserInfoByMail.DEFAULT_ELEMENT_NAME;
    }

    /** {@inheritDoc }. */
    @Override
    protected GetUserInfoByMail getElementForUnmarshalling() {
        GetUserInfoByMail xml = this.getBuilder().
                buildObject(this.getElementQName());
        return xml;
    }
}

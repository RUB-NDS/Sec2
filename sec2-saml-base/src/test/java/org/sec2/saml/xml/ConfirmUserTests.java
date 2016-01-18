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

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

/**
 * Tests for the ConfirmUser element.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * March 16, 2013
 */
public final class ConfirmUserTests
                    extends AbstractXMLElementTests<ConfirmUser> {
    /** {@inheritDoc }. */
    @Override
    protected QName getElementQName() {
        return ConfirmUser.DEFAULT_ELEMENT_NAME;
    }

    /** {@inheritDoc }. */
    @Override
    protected ConfirmUser getElementForUnmarshalling() {
        ConfirmUser xml = this.getBuilder().
                buildObject(this.getElementQName());
        xml.setID("id_abc");
        xml.setChallenge(this.getXsGenerator().
                buildXSString(Challenge.DEFAULT_ELEMENT_NAME));
        xml.getChallenge().setValue(BASE64DUMMY);
        return xml;
    }

    /** {@inheritDoc }. */
    @Override
    protected List<String> getInvalidElements() {
        List<String> list = new ArrayList<String>();

        // double challenge
        this.getStringBuilder().append("<challenge>"
                + BASE64DUMMY + "</challenge>");
        this.getStringBuilder().append("<challenge>"
                + BASE64DUMMY + "</challenge>");
        this.getStringBuilder().append(this.getSuffix());
        list.add(this.getStringBuilder().toString());

        return list;
    }
}

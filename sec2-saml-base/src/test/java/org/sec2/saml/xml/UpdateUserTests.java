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
 * Tests for the UpdateUser element.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 09, 2012
 */
public final class UpdateUserTests
                    extends AbstractXMLElementTests<UpdateUser> {
    /** {@inheritDoc }. */
    @Override
    protected QName getElementQName() {
        return UpdateUser.DEFAULT_ELEMENT_NAME;
    }

    /** {@inheritDoc }. */
    @Override
    protected UpdateUser getElementForUnmarshalling() {
        UpdateUser xml = this.getBuilder().
                buildObject(this.getElementQName());
        return xml;
    }

    /** {@inheritDoc }. */
    @Override
    protected List<String> getInvalidElements() {
        List<String> list = new ArrayList<String>();

        // double UserID
        this.getStringBuilder().append(USERID);
        this.getStringBuilder().append(USERID);
        this.getStringBuilder().append(
                "<emailAddress>test@sec2.org</emailAddress>");
        this.getStringBuilder().append(this.getSuffix());
        list.add(this.getStringBuilder().toString());

        this.resetStringBuilder();

        // double EmailAddress
        this.getStringBuilder().append(USERID);
        this.getStringBuilder().append(
                "<emailAddress>test@sec2.org</emailAddress>");
        this.getStringBuilder().append(
                "<emailAddress>test@sec2.org</emailAddress>");
        this.getStringBuilder().append(this.getSuffix());
        list.add(this.getStringBuilder().toString());

        return list;
    }
}

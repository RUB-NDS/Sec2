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
 * Tests for the RegisterUser element.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * February 04, 2013
 */
public final class RegisterUserTests
                    extends AbstractXMLElementTests<RegisterUser> {
    /** {@inheritDoc }. */
    @Override
    protected QName getElementQName() {
        return RegisterUser.DEFAULT_ELEMENT_NAME;
    }

    /** {@inheritDoc }. */
    @Override
    protected RegisterUser getElementForUnmarshalling() {
        RegisterUser xml = this.getBuilder().
                buildObject(this.getElementQName());
        xml.setSignatureCertificate(this.getXsGenerator().
                buildXSBase64Binary(UserID.DEFAULT_ELEMENT_NAME));
        xml.getSignatureCertificate().setValue(BASE64DUMMY);
        xml.setEncryptionCertificate(this.getXsGenerator().
                buildXSBase64Binary(UserID.DEFAULT_ELEMENT_NAME));
        xml.getEncryptionCertificate().setValue(BASE64DUMMY);
        return xml;
    }

    /** {@inheritDoc }. */
    @Override
    protected List<String> getInvalidElements() {
        List<String> list = new ArrayList<String>();

        // double signatureCertificate
        this.getStringBuilder().append("<signatureCertificate>"
                + BASE64DUMMY + "</signatureCertificate>");
        this.getStringBuilder().append("<signatureCertificate>"
                + BASE64DUMMY + "</signatureCertificate>");
        this.getStringBuilder().append("<encryptionCertificate>"
                + BASE64DUMMY + "</encryptionCertificate>");
        this.getStringBuilder().append(this.getSuffix());
        list.add(this.getStringBuilder().toString());

        this.resetStringBuilder();

        // double encryptionCertificate
        this.getStringBuilder().append("<signatureCertificate>"
                + BASE64DUMMY + "</signatureCertificate>");
        this.getStringBuilder().append("<encryptionCertificate>"
                + BASE64DUMMY + "</encryptionCertificate>");
        this.getStringBuilder().append("<encryptionCertificate>"
                + BASE64DUMMY + "</encryptionCertificate>");
        this.getStringBuilder().append(this.getSuffix());
        list.add(this.getStringBuilder().toString());

        return list;
    }
}

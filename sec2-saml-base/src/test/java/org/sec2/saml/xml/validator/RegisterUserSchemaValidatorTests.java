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
package org.sec2.saml.xml.validator;

import javax.xml.namespace.QName;
import org.opensaml.xml.validation.ValidationException;
import org.sec2.saml.XMLProcessingTestHelper;
import org.sec2.saml.xml.RegisterUser;

/**
 * Tests for the validator for the
 * {@link org.sec2.saml.xml.RegisterUser} element.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * February 04, 2013
 */
public final class RegisterUserSchemaValidatorTests
                    extends AbstractSchemaValidatorTests {
    /** {@inheritDoc }. */
    @Override
    protected QName getElementQName() {
        return RegisterUser.DEFAULT_ELEMENT_NAME;
    }

    /**
     * Tests a correct element.
     */
    public void testValidRegisterUser() {
        this.getStringBuilder().append("<signatureCertificate>"
                + BASE64DUMMY + "</signatureCertificate>");
        this.getStringBuilder().append("<encryptionCertificate>"
                + BASE64DUMMY + "</encryptionCertificate>");
        this.getStringBuilder().append(this.getSuffix());
        RegisterUser registerUser = XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                RegisterUser.class);
        try {
            this.getValidator().validate(registerUser);
        } catch (ValidationException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests a missing signatureCertificate element.
     */
    public void testNoSignatureCertificate() {
        this.getStringBuilder().append("<encryptionCertificate>"
                + BASE64DUMMY + "</encryptionCertificate>");
        this.getStringBuilder().append(this.getSuffix());
        RegisterUser element = XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                RegisterUser.class);
        try {
            this.getValidator().validate(element);
            fail("Should have failed with a ValidationException");
        } catch (ValidationException e) {
            assertNotNull(e);
        }
    }

    /**
     * Tests a missing encryptionCertificate element.
     */
    public void testNoEncryptionCertificate() {
        this.getStringBuilder().append("<signatureCertificate>"
                + BASE64DUMMY + "</signatureCertificate>");
        this.getStringBuilder().append(this.getSuffix());
        RegisterUser element = XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                RegisterUser.class);
        try {
            this.getValidator().validate(element);
            fail("Should have failed with a ValidationException");
        } catch (ValidationException e) {
            assertNotNull(e);
        }
    }
}

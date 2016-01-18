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
import org.sec2.saml.xml.ConfirmUser;

/**
 * Tests for the validator for the
 * {@link org.sec2.saml.xml.ConfirmUser} element.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * March 16, 2013
 */
public final class ConfirmUserSchemaValidatorTests
                    extends AbstractSchemaValidatorTests {
    /**
     * Some dummy signature.
     */
    private static final String SIGNATUREDUMMY =
              "<ds:Signature xmlns:ds=\"http://www.w3."
            + "org/2000/09/xmldsig#\"><ds:SignedInfo><ds:CanonicalizationMe"
            + "thod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/>"
            + "<ds:SignatureMethod Algorithm=\"http://www.w3.org/2001/04/xm"
            + "ldsig-more#rsa-sha256\"/><ds:Reference URI=\"\"><ds:Transfor"
            + "ms><ds:Transform Algorithm=\"http://www.w3.org/2000/09/xmlds"
            + "ig#enveloped-signature\"/><ds:Transform Algorithm=\"http://w"
            + "ww.w3.org/2001/10/xml-exc-c14n#\"/></ds:Transforms><ds:Diges"
            + "tMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#s"
            + "ha384\"/><ds:DigestValue>" + BASE64DUMMY + "</ds:DigestValue"
            + "></ds:Reference></ds:SignedInfo><ds:SignatureValue>"
            + BASE64DUMMY + "</ds:SignatureValue></ds:Signature>";

    /** {@inheritDoc }. */
    @Override
    protected QName getElementQName() {
        return ConfirmUser.DEFAULT_ELEMENT_NAME;
    }

    /**
     * Tests a correct element.
     */
    public void testValidConfirmUser() {
        this.getStringBuilder().deleteCharAt(
                this.getStringBuilder().length() - 1);
        this.getStringBuilder().append(" ID='id_abc'>");
        this.getStringBuilder().append("<challenge>"
                + BASE64DUMMY + "</challenge>");
        this.getStringBuilder().append(SIGNATUREDUMMY);
        this.getStringBuilder().append(this.getSuffix());
        ConfirmUser confirmUser = XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                ConfirmUser.class);
        try {
            this.getValidator().validate(confirmUser);
        } catch (ValidationException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests a missing challenge element.
     */
    public void testNoChallenge() {
        this.getStringBuilder().deleteCharAt(
                this.getStringBuilder().length() - 1);
        this.getStringBuilder().append(" ID='id_abc'>");
        this.getStringBuilder().append(SIGNATUREDUMMY);
        this.getStringBuilder().append(this.getSuffix());
        ConfirmUser element = XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                ConfirmUser.class);
        try {
            this.getValidator().validate(element);
            fail("Should have failed with a ValidationException");
        } catch (ValidationException e) {
            assertNotNull(e);
        }
    }

    /**
     * Tests a missing Signature element.
     */
    public void testNoSignature() {
        this.getStringBuilder().deleteCharAt(
                this.getStringBuilder().length() - 1);
        this.getStringBuilder().append(" ID='id_abc'>");
        this.getStringBuilder().append("<challenge>"
                + BASE64DUMMY + "</challenge>");
        this.getStringBuilder().append(this.getSuffix());
        ConfirmUser element = XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                ConfirmUser.class);
        try {
            this.getValidator().validate(element);
            fail("Should have failed with a ValidationException");
        } catch (ValidationException e) {
            assertNotNull(e);
        }
    }

    /**
     * Tests a missing ID.
     */
    public void testMissingID() {
        this.getStringBuilder().append("<challenge>"
                + BASE64DUMMY + "</challenge>");
        this.getStringBuilder().append(SIGNATUREDUMMY);
        this.getStringBuilder().append(this.getSuffix());
        ConfirmUser element = XMLProcessingTestHelper.parseXMLElement(
                this.getStringBuilder().toString(),
                ConfirmUser.class);
        try {
            this.getValidator().validate(element);
            fail("Should have failed with a ValidationException");
        } catch (ValidationException e) {
            assertNotNull(e);
        }
    }
}

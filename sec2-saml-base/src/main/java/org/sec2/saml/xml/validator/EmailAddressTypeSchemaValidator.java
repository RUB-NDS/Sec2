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

import java.util.regex.Matcher;
import org.opensaml.xml.schema.validator.XSStringSchemaValidator;
import org.opensaml.xml.validation.ValidationException;
import org.sec2.saml.xml.EmailAddressType;

/**
 * Checks {@link org.sec2.saml.xml.EmailAddressType} for schema compliance.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 06, 2012
 *
 * @param <T> the type to be validated
 */
public class EmailAddressTypeSchemaValidator<T extends EmailAddressType>
                    extends XSStringSchemaValidator<T> {
    /**
     * Constructor.
     */
    public EmailAddressTypeSchemaValidator() {
        super(false);
    }

    /** {@inheritDoc} */
    @Override
    public void validate(final T xmlObject) throws ValidationException {
        validateStringContent(xmlObject);
        validateEmailAddress(xmlObject);
    }

    /**
     * Validates the content of the EmailAddressType object.
     *
     * @param xmlObject the object to evaluate
     * @throws ValidationException thrown if the email address is invalid
     */
    public void validateEmailAddress(final T xmlObject)
            throws ValidationException {
        Matcher matcher = EmailAddressType.EMAIL_ADDRESS_PATTERN.matcher(
                xmlObject.getValue().trim());
        if (!matcher.matches()) {
            throw new ValidationException("Email address is invalid");
        }
    }
}

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

import org.opensaml.xml.validation.ValidationException;
import org.opensaml.xml.validation.Validator;
import org.sec2.saml.xml.UserBaseType;

/**
 * Checks {@link org.sec2.saml.xml.UserBaseType} for schema compliance.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 06, 2012
 *
 * @param <T> the type to be validated
 */
public abstract class UserBaseTypeSchemaValidator<T extends UserBaseType>
                    implements Validator<T> {
    /** {@inheritDoc} */
    @Override
    public void validate(final T xmlObject) throws ValidationException {
        validateUserID(xmlObject);
        validateEmailAddress(xmlObject);
    }

    /**
     * Checks the userID.
     *
     * @param xmlObject the XMLObject to check
     * @throws ValidationException if no UserID is found
     */
    public void validateUserID(final T xmlObject) throws ValidationException {
        if (xmlObject.getUserID() == null) {
            throw new ValidationException("UserID is required");
        }
    }

    /**
     * Checks the email address.
     *
     * @param xmlObject the XMLObject to check
     * @throws ValidationException if no email address is found
     */
    public void validateEmailAddress(final T xmlObject)
            throws ValidationException {
        if (xmlObject.getEmailAddress() == null) {
            throw new ValidationException("EmailAddress is required");
        }
    }
}

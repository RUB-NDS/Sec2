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
import org.sec2.saml.xml.ConfirmUser;

/**
 * Checks {@link org.sec2.saml.xml.ConfirmUser} for schema compliance.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * March 16, 2013
 *
 * @param <T> the type to be validated
 */
public class ConfirmUserSchemaValidator<T extends ConfirmUser>
                    implements Validator<T> {
    /** {@inheritDoc} */
    @Override
    public void validate(final T xmlObject) throws ValidationException {
        validateChallenge(xmlObject);
        validateID(xmlObject);
        validateSignature(xmlObject);
    }

    /**
     * Checks the Challenge.
     *
     * @param xmlObject the XMLObject to check
     * @throws ValidationException if no Challenge is found
     */
    public void validateChallenge(final T xmlObject)
            throws ValidationException {
        if (xmlObject.getChallenge() == null) {
            throw new ValidationException("Challenge is required");
        }
    }

    /**
     * Checks the ID.
     *
     * @param xmlObject the XMLObject to check
     * @throws ValidationException if no ID is found
     */
    public void validateID(final T xmlObject)
            throws ValidationException {
        if (xmlObject.getID() == null) {
            throw new ValidationException("ID is required");
        }
    }

    /**
     * Checks that a signature is present, not if it is valid.
     *
     * @param xmlObject the XMLObject to check
     * @throws ValidationException if no Signature is found
     */
    public void validateSignature(final T xmlObject)
            throws ValidationException {
        if (xmlObject.getSignature() == null) {
            throw new ValidationException("Signature is required");
        }
    }
}

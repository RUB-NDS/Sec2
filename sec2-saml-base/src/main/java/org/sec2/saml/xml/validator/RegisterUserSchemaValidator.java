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
import org.sec2.saml.xml.RegisterUser;

/**
 * Checks {@link org.sec2.saml.xml.RegisterUser} for schema compliance.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * February 04, 2013
 *
 * @param <T> the type to be validated
 */
public class RegisterUserSchemaValidator<T extends RegisterUser>
                    implements Validator<T> {
    /** {@inheritDoc} */
    @Override
    public void validate(final T xmlObject) throws ValidationException {
        validateSignatureCertificate(xmlObject);
        validateEncryptionCertificate(xmlObject);
    }

    /**
     * Checks the SignatureCertificate.
     *
     * @param xmlObject the XMLObject to check
     * @throws ValidationException if no SignatureCertificate is found
     */
    public void validateSignatureCertificate(final T xmlObject)
            throws ValidationException {
        if (xmlObject.getSignatureCertificate() == null) {
            throw new ValidationException("SignatureCertificate is required");
        }
    }

    /**
     * Checks the EncryptionCertificate.
     *
     * @param xmlObject the XMLObject to check
     * @throws ValidationException if no EncryptionCertificate is found
     */
    public void validateEncryptionCertificate(final T xmlObject)
            throws ValidationException {
        if (xmlObject.getEncryptionCertificate() == null) {
            throw new ValidationException("EncryptionCertificate is required");
        }
    }
}

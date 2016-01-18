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
import org.sec2.saml.xml.GroupUsersRelationType;

/**
 * Checks {@link org.sec2.saml.xml.GroupUsersRelationType}
 * for schema compliance.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * December 18, 2012
 *
 * @param <T> the type to be validated
 */
public class GroupUsersRelationTypeSchemaValidator
            <T extends GroupUsersRelationType> implements Validator<T> {
    /** {@inheritDoc} */
    @Override
    public void validate(final T xmlObject) throws ValidationException {
        validateGroupName(xmlObject);
        validateUserIDs(xmlObject);
    }

    /**
     * Checks that a group name is present.
     *
     * @param xmlObject the XMLObject to check
     * @throws ValidationException if no GroupName is found
     */
    public void validateGroupName(final T xmlObject)
            throws ValidationException {
        if (xmlObject.getGroupName() == null) {
            throw new ValidationException("GroupName is required");
        }
        if (xmlObject.getGroupName().getValue() == null) {
            throw new ValidationException("GroupName is null");
        }
    }

    /**
     * Checks that at least one user ID is present.
     *
     * @param xmlObject the XMLObject to check
     * @throws ValidationException if no UserID is found
     */
    public void validateUserIDs(final T xmlObject)
            throws ValidationException {
        if (xmlObject.getUsers().isEmpty()) {
            throw new ValidationException("At least one userID is required");
        }
    }
}

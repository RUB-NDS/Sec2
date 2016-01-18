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
import org.sec2.saml.xml.GroupResponseType;

/**
 * Checks {@link org.sec2.saml.xml.GroupResponseType} for schema compliance.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * May 18, 2012
 *
 * @param <T> the type to be validated
 */
public class GroupResponseTypeSchemaValidator<T extends GroupResponseType>
                    extends GroupBaseTypeSchemaValidator<T> {
    /** {@inheritDoc} */
    @Override
    public void validate(final T xmlObject) throws ValidationException {
        super.validate(xmlObject);
        validateGroupName(xmlObject);
        validateGroupKey(xmlObject);
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
     * Checks the group's group key.
     *
     * @param xmlObject the XMLObject to check
     * @throws ValidationException if no group key is found
     */
    public void validateGroupKey(final T xmlObject)
            throws ValidationException {
        if (xmlObject.getGroupKey() == null) {
            throw new ValidationException("GroupKey is required");
        }
    }
}

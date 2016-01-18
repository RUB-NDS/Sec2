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

import org.opensaml.xml.util.DatatypeHelper;
import org.opensaml.xml.validation.ValidationException;
import org.opensaml.xml.validation.Validator;
import org.sec2.saml.xml.GroupBaseType;

/**
 * Checks {@link org.sec2.saml.xml.GroupBaseType} for schema compliance.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 06, 2012
 *
 * @param <T> the type to be validated
 */
public abstract class GroupBaseTypeSchemaValidator<T extends GroupBaseType>
                    implements Validator<T> {
    /** {@inheritDoc} */
    @Override
    public void validate(final T xmlObject) throws ValidationException {
        validateGroupName(xmlObject);
        validateGroupOwnerID(xmlObject);
    }

    /**
     * Checks the GroupName.
     *
     * @param xmlObject the XMLObject to check
     * @throws ValidationException if no GroupName is found or if it is empty
     */
    public void validateGroupName(final T xmlObject)
            throws ValidationException {
        if (xmlObject.getGroupName() == null) {
            throw new ValidationException("GroupName is required");
        }
        if (DatatypeHelper.isEmpty(xmlObject.getGroupName().getValue())) {
            throw new ValidationException("GroupName is empty");
        }
    }

    /**
     * Checks the group's owner's id.
     *
     * @param xmlObject the XMLObject to check
     * @throws ValidationException if no group owner ID is found
     */
    public void validateGroupOwnerID(final T xmlObject)
            throws ValidationException {
        if (xmlObject.getGroupOwnerID() == null) {
            throw new ValidationException("GroupOwnerID is required");
        }
    }
}

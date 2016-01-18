package org.sec2.mwserver.core.validators;

import org.sec2.mwserver.core.exceptions.HandleRequestException;

/**
 * Interface for validating values of HTML header fields.
 * @author schuessler
 *
 */
public interface IHeaderValidator
{
    /**
     * Validates the value of an HTML header field. If the value is not valid,
     * a HandleRequestException is thrown.
     *
     * @param key - The key of the header field to be validated
     * @param value - The value of the header field to be validated
     * @throws HandleRequestException if the value is not valid
     */
    public void validate(String key, String value)
            throws HandleRequestException;
}

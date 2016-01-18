package org.sec2.mwserver.core.validators.impl;

import org.sec2.mwserver.core.exceptions.HandleRequestException;
import org.sec2.mwserver.core.validators.IHeaderValidator;

/**
 * A default implementation of the IHeaderValidator interface. A
 * HandleRequestException is only thrown if the header value is NULL or empty.
 *
 * @author schuessler
 */
public class DefaultHeaderValidator implements IHeaderValidator
{
    /* (non-Javadoc)
     * @see org.sec2.desktop.servers.util.IHeaderValidator#validate(
     * java.lang.String)
     */
    @Override
    public void validate(final String key, final String value)
            throws HandleRequestException
            {
        if (value == null || value.isEmpty())
        {
            throw new HandleRequestException(
                    "Ungültiger Request: Header-Feld \"" + key
                    + "\" fehlt oder ist leer.");
        }
            }
}

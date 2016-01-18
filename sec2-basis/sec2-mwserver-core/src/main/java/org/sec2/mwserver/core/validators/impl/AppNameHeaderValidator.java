package org.sec2.mwserver.core.validators.impl;

import java.util.regex.Pattern;

import org.sec2.mwserver.core.exceptions.HandleRequestException;

/**
 * Implementation of the IHeaderValidator interface. Throws a
 * HandleRequestException if the value of the header field
 * doesn't match the regex-pattern [a-zA-Z0-9\.]+
 *
 * @author schuessler
 */
public class AppNameHeaderValidator extends DefaultHeaderValidator
{
    /* (non-Javadoc)
     * @see org.sec2.desktop.servers.util.IHeaderValidator#validate(
     * java.lang.String)
     */
    @Override
    public void validate(final String key, final String value)
            throws HandleRequestException
            {
        super.validate(key, value);
        if (!Pattern.matches("[a-zA-Z0-9\\.]+", value.trim()))
        {
            throw new HandleRequestException(this.getClass().getName()
                    + ": Wert \"" + value + "\" für Header-Field \"" + key
                    + "\" nicht gültig!");
        }
            }
}

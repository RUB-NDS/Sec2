package org.sec2.mwserver.core.validators.impl;

import java.util.regex.Pattern;

import org.sec2.mwserver.core.exceptions.HandleRequestException;

/**
 * Implementation of the IHeaderValidator interface. Throws a
 * HandleRequestException if the value of the header field doesn't describe an
 * TCP/IP port. That means that the value has to be an integer within the range
 * of allowed port numbers. Otherwise a HandleRequestException is thrown.
 *
 * @author schuessler
 */
public class PortHeaderValidator extends DefaultHeaderValidator
{
    /* (non-Javadoc)
     * @see org.sec2.desktop.servers.util.IHeaderValidator#validate(
     * java.lang.String)
     */
    @Override
    public void validate(final String key, final String value)
            throws HandleRequestException
            {
        int port;

        super.validate(key, value);
        if (!Pattern.matches("\\d{1,5}", value.trim()))
        {
            throwException(key, value);
        }
        port = Integer.parseInt(value);
        if (port < 1 || port > 65535)
        {
            throwException(key, value);
        }
            }

    private void throwException(final String key, final String value)
            throws HandleRequestException
            {
        throw new HandleRequestException(this.getClass().getName()
                + ": Wert \"" + value + "\" für Header-Field \"" + key
                + "\" nicht gültig!");
            }
}
